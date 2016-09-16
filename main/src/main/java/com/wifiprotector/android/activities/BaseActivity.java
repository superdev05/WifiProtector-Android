package com.wifiprotector.android.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.octo.android.robospice.SpiceManager;
import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.api.SoapSpiceService;

public abstract class BaseActivity extends ActionBarActivity {

    protected SpiceManager spiceManager = new SpiceManager(SoapSpiceService.class);
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        spiceManager.start(this);
    }

    @Override
    protected void onDestroy() {
        spiceManager.shouldStop();
        super.onDestroy();
    }

    protected boolean isFreeUser() {
        return !Preferences.User.isPaid();
    }

    protected boolean isOnline() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
