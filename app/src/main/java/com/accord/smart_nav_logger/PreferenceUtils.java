package com.accord.smart_nav_logger;

import android.annotation.TargetApi;
import android.content.SharedPreferences;

public class PreferenceUtils {

    public static final String KEY_SERVICE_TRACKING_ENABLED = "tracking_foreground_location";


    /**
     * Returns true if service location tracking is active, and false if it is not
     * @return true if service location tracking is active, and false if it is not
     */
    public static boolean isTrackingStarted(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_SERVICE_TRACKING_ENABLED, false);
    }

    /**
     * Saves the provided value as the current service location tracking state
     * @param value true if service location tracking is active, and false if it is not
     */
    public static void saveTrackingStarted(boolean value, SharedPreferences prefs) {
        saveBoolean(KEY_SERVICE_TRACKING_ENABLED, value, prefs);
    }

    @TargetApi(9)
    public static void saveBoolean(String key, boolean value, SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

}
