package com.wifiprotector.android.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.wifiprotector.android.VPNApplication;

public class NetworkUtils {

    public static String getMacAddressSeparatedByHyphens() {
        WifiManager wifiManager = (WifiManager)
                VPNApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        return info.getMacAddress().replace(":", "-");
    }
}
