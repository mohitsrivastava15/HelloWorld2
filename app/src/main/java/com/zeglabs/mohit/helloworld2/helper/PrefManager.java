package com.zeglabs.mohit.helloworld2.helper;

/**
 * Created by mohit on 28/8/16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;

import java.util.HashMap;

/**
 * Created by Ravi on 08/07/15.
 */
public class PrefManager {
    // Shared Preferences
    public static final String NAME_KEY = "__NAME__" ;
    public static final String EMAIL_KEY = "__EMAIL__" ;
    public static final String PHONE_KEY = "__PHONE__" ;
    public static final String PHOTO_KEY = "__PHOTO__" ;
    public static final String BITMAP_PHOTO_KEY = "__BITMAP__";
    public static final String ID_KEY = "__PHOTO__" ;
    public static final String IS_LOGGED_IN_KEY = "__IS__LOGGED_IN__";
    public static final String SENSOR_DETAILS = "__SENSOR_DETAILS";
    public static final String SENSOR_READINGS = "__SENSOR_READINGS__";
    public static final String SENSOR_FOUND_MAC = "__SENSOR_FOUND_MAC__";
    public static final String SENSORCREATE_ITEM = "__SENSORCREATE_ITME__";
    public static final String SENSORCREATE_MAC = "__SENSORCREATE_MAC__";
    public static final String SENSORCREATE_DEVICENAME = "__SENSORCREATE_DEVICENAME__";
    public static final String SENSORCREATE_MAXDISTANCE = "__SENSORCREATE_MAXDISTANCE__";
    public static final String SENSOR_CACHE = "__SENSOR_CACHE__";
    public static final String APP_FIRST_RUN = "__APP_FIRST_RUN__";
    public static final String SHOPPING_CART = "__SHOPPING_CART__";
    public static final String BLUETOOTH_DEVICE = "__BLUETOOTH_DEVICE__";

    private static final String PREF_NAME = "ZegLabs";

    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;


    public static LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }



    /**
    * Called to save supplied value in shared preferences against given key.
    * @param context Context of caller activity
    * @param key Key of value to save against
    * @param value Value to save
    */
    public static void saveToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * Called to save supplied value in shared preferences against given key.
     * @param context Context of caller activity
     * @param key Key of value to save against
     * @param value Value to save
     */
    public static void saveToPrefs(Context context, String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    /**
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     * @param context Context of caller activity
     * @param key Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static boolean getFromPrefs(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getBoolean(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}