package com.zeglabs.mohit.helloworld2.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.activity.MainActivity;
import com.zeglabs.mohit.helloworld2.activity.SmsActivity;
import com.zeglabs.mohit.helloworld2.app.MyApplication;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.DownloadImageFromInternet;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.SensorDetailsParser;
import com.zeglabs.mohit.helloworld2.helper.ValidationHelper;
import com.zeglabs.mohit.helloworld2.model.SensorDetails;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {
    String name = "";
    String email = "";
    String photourl = "";
    String id = "";
    private TextView mStatusTextView;
    private EditText mInputMobile;
    private Button btnMobileNext;
    private TextInputLayout inputLayoutPhone;
    DownloadImageFromInternet downloadImage;

    private Context appContext;
    private static String TAG = LoginActivity.class.getSimpleName();

    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        this.appContext = getApplicationContext();

        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.IS_LOGGED_IN_KEY, true);

        this.name = getIntent().getStringExtra("Name");
        //PrefManager.saveToPrefs(getApplicationContext(), PrefManager.NAME_KEY, this.name);
        this.email = getIntent().getStringExtra("Email");
        //PrefManager.saveToPrefs(getApplicationContext(), PrefManager.EMAIL_KEY, this.email);
        this.photourl = getIntent().getStringExtra("PhotoURL");
        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.PHOTO_KEY, this.photourl);
        this.id = getIntent().getStringExtra("Id");
        //PrefManager.saveToPrefs(getApplicationContext(), PrefManager.ID_KEY, this.id);

        mStatusTextView = (TextView) findViewById(R.id.user_name);
        mInputMobile = (EditText) findViewById(R.id.inputMobile);
        btnMobileNext = (Button) findViewById(R.id.mobileNext_button);

        mStatusTextView.setText(this.name);

        mInputMobile.setVisibility(View.INVISIBLE);
        btnMobileNext.setVisibility(View.INVISIBLE);

        this.showProgressDialog();
        /**
         * Send request to see if a mobile number exists. Otherwise set visibility of mInputMobile and btnMobileNext and let the user
         * input the mobile and then again call the function
         */
        this.sendRequest();

        btnMobileNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                String phone = mInputMobile.getText().toString().trim();
                boolean isPhoneValid = SmsActivity.isValidatePhone(phone);

                if(!isPhoneValid) {
                    Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                PrefManager.saveToPrefs(getApplicationContext(), PrefManager.PHONE_KEY, phone);

                Intent intent = new Intent(context, OTPActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        this.downloadImage = new DownloadImageFromInternet(getApplicationContext(), (CircleImageView) findViewById(R.id.user_image));
        this.downloadImage.execute(this.photourl);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.inputMobile:
                    validatePhone();
                    break;
            }
        }
    }

    private void sendRequest() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_REQUEST_SMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");
                    int errorCode = responseObj.getInt("code");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (errorCode == 2) {
                        Toast.makeText(appContext, "Mobile number exists and verified for User. Redirecting to Dashboard!", Toast.LENGTH_SHORT).show();

                        String mobile = responseObj.getString("mobile");
                        String sensorDetails = responseObj.getString("sensorDetails");
                        String sensorReadings = responseObj.getString("sensorReadings");

                        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.PHONE_KEY, mobile);
                        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.SENSOR_DETAILS, sensorDetails);
                        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.SENSOR_READINGS, sensorReadings);
                        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.APP_FIRST_RUN, true);

                        /**
                         * Save the cache in the intent variables
                         */
                        List<SensorDetails> sensors = SensorDetailsParser.getSensorDetails(sensorDetails, sensorReadings);
                        SensorDetailsMap cache = new SensorDetailsMap(sensors);

                        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(appContext, "", 0);
                        complexPreferences.putObject(PrefManager.SENSOR_CACHE, cache);
                        complexPreferences.commit();

                        Intent intent = new Intent(appContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                        appContext.startActivity(intent);
                    } else if (errorCode == 3) {
                        String mobile = responseObj.getString("mobile");

                        Toast.makeText(appContext, "Mobile number exists, sent a new OTP to "+mobile+"for verification " + message, Toast.LENGTH_LONG).show();

                        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.PHONE_KEY, mobile);

                        Intent intent = new Intent(appContext, OTPActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);

                    } else if (errorCode == 99) {
                        Toast.makeText(appContext, "Error: " + message, Toast.LENGTH_LONG).show();
                        mInputMobile.setVisibility(View.VISIBLE);
                        btnMobileNext.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(appContext, "Inconsistent Database. Please check the error=" + message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(appContext,
                            "Error: " + response.toString(),
                            Toast.LENGTH_LONG).show();
                } finally {
                    hideProgressDialog();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(appContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("mobile", "");
                params.put("imageurl", "");

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private boolean validatePhone() {
        String phone = mInputMobile.getText().toString().trim();
        boolean result = ValidationHelper.isValidPhoneNumber(phone);
        if (!result) {
            //inputLayoutPhone.setError("Enter a valid mobile phone!");
            inputLayoutPhone.setErrorEnabled(true);
            //requestFocus(mInputMobile);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

}
