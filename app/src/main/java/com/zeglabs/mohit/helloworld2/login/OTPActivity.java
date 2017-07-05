package com.zeglabs.mohit.helloworld2.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.DownloadImageFromInternet;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.ValidationHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OTPActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView otp;
    private String name = "";
    private String email = "";
    private String id = "";
    private String photourl = "";
    private String phone = "";

    private TextView inputOtp;
    private Button btnVerifyOtp;
    private DownloadImageFromInternet downloadImage;

    public static ProgressDialog dialog;

    private Context appContext;
    private static String TAG = LoginActivity.class.getSimpleName();

    private ProgressDialog mProgressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        this.appContext = getApplicationContext();

        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        otp = (TextView) findViewById(R.id.inputOtp);
        // Checking if user session
        // if not logged in, take user to sms screen
        if (!PrefManager.getFromPrefs(getApplicationContext(), PrefManager.IS_LOGGED_IN_KEY, false)) {
            //Add code to log user out.
        }
        //this.downloadImage = (DownloadImageFromInternet) getIntent().getSerializableExtra(Config.DOWNLOAD_IMAGE);

        this.name = PrefManager.getFromPrefs(getApplicationContext(), PrefManager.NAME_KEY, "");
        this.email = PrefManager.getFromPrefs(getApplicationContext(), PrefManager.EMAIL_KEY, "");
        this.phone = PrefManager.getFromPrefs(getApplicationContext(), PrefManager.PHONE_KEY, "");

        this.photourl = PrefManager.getFromPrefs(getApplicationContext(), PrefManager.PHOTO_KEY, "");
        // Displaying user information from shared preferences

        //requestOTP();

        this.inputOtp = (EditText) findViewById(R.id.inputOtp);

        this.btnVerifyOtp = (Button) findViewById(R.id.btn_verify_otp);

        this.btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = inputOtp.getText().toString().trim();
                boolean isValidOtp = ValidationHelper.isValidOtp(otp);

                if(!isValidOtp) {
                    Toast.makeText(getApplicationContext(), "Please enter valid OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                sendVerifyOTPRequest();

            }
        });


    }

    private void sendVerifyOTPRequest() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_VERIFY_OTP, new Response.Listener<String>() {

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
                    if (errorCode == 0 || errorCode == 5) {
                        Toast.makeText(appContext, "OTP verified for mobile="+phone+"!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(appContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);

                    } else if (errorCode == 3) {
                        Toast.makeText(appContext, "Error: " + message, Toast.LENGTH_LONG).show();

                        /*
                        TODO: show the hidden element to re-initiate the OTP request.
                         */

                    } else {
                        Toast.makeText(appContext, "Inconsistent Database. Please check the error=" + message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(appContext, "Error: " + response.toString(), Toast.LENGTH_LONG).show();
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
                params.put("email", email);
                params.put("otp", inputOtp.getText().toString().trim());

                Log.e(TAG, "Posting params to URL="+Config.URL_VERIFY_OTP+" : " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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

    private void requestOTP() {
        dialog = new ProgressDialog(this);
        /*dialog.setMessage("Sending OTP to "+this.phone);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setProgress(0);
        dialog.show();*/
        dialog = ProgressDialog.show(OTPActivity.this, "", "Sending OTP to "+this.phone, true);

        SmsActivity.requestForSMS(this, this.name, this.email, this.phone, this.photourl);

    }

    /**
     * Logging out user
     * will clear all user shared preferences and navigate to
     * sms activation screen
     */
    private void logout() {
        /*pref.clearSession();

        Intent intent = new Intent(OTPActivity.this, SmsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

        finish();*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_logout) {
            logout();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
