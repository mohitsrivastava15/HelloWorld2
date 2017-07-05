package com.zeglabs.mohit.helloworld2.application;

/**
 * Created by mohit on 30/10/16.
 */
import android.app.Application;

import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;

public class ObjectPreference extends Application {
    private static final String TAG = "ObjectPreference";
    private ComplexPreferences complexPrefenreces = null;

    @Override
    public void onCreate() {
        super.onCreate();
        complexPrefenreces = ComplexPreferences.getComplexPreferences(getBaseContext(), "complex_preferences", MODE_PRIVATE);
        android.util.Log.i(TAG, "Preference Created.");
    }

    public ComplexPreferences getComplexPreference() {
        if(complexPrefenreces != null) {
            return complexPrefenreces;
        }
        return null;
    } }