package com.zeglabs.mohit.helloworld2.activity;

/**
 * Created by mohit on 28/8/16.
 */
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.app.MyApplication;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.ValidationHelper;
import com.zeglabs.mohit.helloworld2.login.OTPActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SmsActivity {
    private static String TAG = SmsActivity.class.getSimpleName();

    private PrefManager pref;

    /**
     * Validating user details form
     */
    public static boolean isValidatePhone(String inputMobile) {
        String mobile = inputMobile.toString().trim();

        // validating empty name and email
        if (!ValidationHelper.isValidPhoneNumber(inputMobile)) {
            //Toast.makeText(getApplicationContext(), "Please enter your details", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Method initiates the SMS request on the server
     *  @param name   user name
     * @param email  user email address
     * @param mobile user valid mobile number
     */
    public static void requestForSMS(final Context context, final String name, final String email, final String mobile, final String imageurl) {
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

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (!error) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context,
                                "Error: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                    OTPActivity.dialog.dismiss();

                } catch (JSONException e) {
                    Toast.makeText(context,
                            "Error: " + response.toString(),
                            Toast.LENGTH_LONG).show();
                    OTPActivity.dialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                OTPActivity.dialog.dismiss();
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
                params.put("mobile", mobile);
                params.put("imageurl", imageurl);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    /**
     * sending the OTP to server and activating the user
     */
    public static void verifyOtp(final Context context, final String otp) {
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

                    if (!error) {
                        // parsing the user profile information
                        JSONObject profileObj = responseObj.getJSONObject("profile");

                        String name = profileObj.getString("name");
                        String email = profileObj.getString("email");
                        String mobile = profileObj.getString("mobile");


                        /*Intent intent = new Intent(HttpService.this, OTPActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);*/

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(context,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("otp", otp);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

}
