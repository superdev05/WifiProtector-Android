package com.wifiprotector.android.api;

import com.wifiprotector.android.Preferences;

public class UpdateDeviceLocationInfoRequest extends GetLocationInfoRequest {

    @Override
    public LocationInfo loadDataFromNetwork() throws Exception {
        LocationInfo info = super.loadDataFromNetwork();

        Preferences.setDeviceLocation(info);

        return info;
    }
}
