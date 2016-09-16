package com.wifiprotector.android;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import de.blinkt.openvpn.BuildConfig;
import de.blinkt.openvpn.R;
import timber.log.Timber;

public class VPNApplication extends Application {

    public static final String CLIENT_VERSION = "m.1.0.1";
    public static final int NOTIFICATION_TIME_EXCEEDED_ID = 2;
    public static final String ACTION_LIMIT_EXCEEDED = "limitExceeded";
    public static final String ACTION_VPN_STATE_CHANGED = "action.vpn.state_changed";
    public static final String ACTION_WP_INITIALIZED = "action.wp.initialized";
    public static final String EXTRA_VPN_STATE = "extraState";
    public static final String EXTRA_SERVERS = "extraServers";
    public static final String EXTRA_VPN_CONNECTION_STATUS = "extraConnectionStatus";
    public static final String EXTRA_LICENSE_TYPE = "extraLicenseType";
    public static final String EXTRA_SERVER_IP = "extraServerIp";

    private static VPNApplication instance;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (!isDebug()) {
            Crashlytics.start(this);
            Timber.plant(new CrashReportingTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-23644583-45");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);


    }

    public static Tracker getTracker(){
        return tracker;
    }


    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static String getPublicLicenceKey() {
        return getContext().getString(R.string.public_licence_key);
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void i(String message, Object... args) {
            Crashlytics.log(String.format(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            i(message, args); // Just add to the log.
        }

        @Override
        public void e(String message, Object... args) {
            i("ERROR: " + message, args); // Just add to the log.
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Crashlytics.logException(t);
        }
    }
}
