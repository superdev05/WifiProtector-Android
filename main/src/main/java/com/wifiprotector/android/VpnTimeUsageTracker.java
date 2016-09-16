package com.wifiprotector.android;

import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VpnTimeUsageTracker {

    public interface Listener {
        void onUpdate(long trackedTime, boolean limitExceeded);
    }

    private static long DELAY_TIME = 1000;

    private Handler handler = new Handler();
    private long timeTrackedSoFarMs;
    private long lastUpdateTimeMs;
    private Listener listener;

    private final Runnable commitChangesTask = new Runnable() {
        @Override
        public void run() {
            final long currentTimeMs = System.currentTimeMillis();
            final long timeSinceLastUpdateMs = currentTimeMs - lastUpdateTimeMs;
            lastUpdateTimeMs = currentTimeMs;
            timeTrackedSoFarMs += timeSinceLastUpdateMs;
            notifyListener();
            saveTimeInPreferences();
            if (timeTrackedSoFarMs < 1000*Preferences.User.getAllowedTime()) {
                handler.postDelayed(commitChangesTask, DELAY_TIME);
            }
        }
    };

    private void notifyListener() {
        if (listener != null) {
            boolean limitExceeded = !Preferences.User.isPaid()
                    && timeTrackedSoFarMs >= 1000*Preferences.User.getAllowedTime();
            listener.onUpdate(timeTrackedSoFarMs, limitExceeded);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public VpnTimeUsageTracker() {
        timeTrackedSoFarMs = 1000 * Preferences.User.getTrackedTime();
    }

    public void startTracking() {
        if (shouldResetCounter()) {
            Preferences.User.updateTrackedTime(0);
            Preferences.User.updateLastTrackedDate(Preferences.DATE_FORMAT.format(new Date()));
            timeTrackedSoFarMs = 0;
        }

        lastUpdateTimeMs = System.currentTimeMillis();
        handler.postDelayed(commitChangesTask, DELAY_TIME);
    }

    public void stopTracking() {
        handler.removeCallbacks(commitChangesTask);
    }

    private void saveTimeInPreferences() {
        Preferences.User.updateTrackedTime(timeTrackedSoFarMs/1000);
    }

    private boolean shouldResetCounter() {
        String lastTrackedDate = Preferences.User.getLastTrackedDate();
        return lastTrackedDate == null || !lastTrackedDate.equals(Preferences.DATE_FORMAT.format(new Date()));
    }
}
