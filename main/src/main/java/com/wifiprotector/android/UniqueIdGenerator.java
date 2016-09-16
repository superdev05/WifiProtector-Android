package com.wifiprotector.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public class UniqueIdGenerator {

    private static final String PREF_DEVICE_ID = "uniqueDeviceId";

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deviceId = prefs.getString(PREF_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString(PREF_DEVICE_ID, deviceId).apply();
        }

        return deviceId;
    }
}
