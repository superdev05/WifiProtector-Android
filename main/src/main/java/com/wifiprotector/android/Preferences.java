package com.wifiprotector.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wifiprotector.android.api.LocationInfo;
import com.wifiprotector.android.model.LicenceType;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.blinkt.openvpn.R;

public class Preferences {
    private static final String DEVICE_IP_KEY = "deviceIp";

    private static final String PREF_CERT_EXPIRATION_DATE = "certificateValidUntil";
    private static final String PREF_VPN_PROFILE = "vpnProfileTemplate";
    private static final String PREF_CLIENT_OVPN = "clientOvpn";
    private static final String PREF_CLIENT_OVPN_HAS_SERVERS = "clientOvpnHasServers";
    private static final String PREF_LICENCE_TYPE = "licenceType";
    private static final String PREF_LICENCE_KEY = "licenceKey";
    private static final String PREF_LICENCE_KEY_EXPIRATION_DATE = "licenceKeyExpirationDate";
    private static final String PREF_TRACKED_TIME = "trackedTime";
    private static final String PREF_TRACKED_DATE = "trackedDate";
    private static final String PREF_IS_SUBSCRIPTION_ACTIVE = "isSubscriptionActive";

    private static final String PROFILE_ORIGIN_IP_KEY = "profileOriginIp";
    private static final String WAS_VPN_INTRODUCTION_DISPLAYED = "wasVpnIntroductionDisplayed";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//    public static String getLicenceType() {
//        return getPreferences().getString(PREF_LICENCE_TYPE, FREE_LICENSE);
//    }
//
//    public static void setLicenceType(String type) {
//        getPreferences().edit().putString(PREF_LICENCE_TYPE, type).apply();
//    }
//

//
//    public static String getLicenceKey() {
//        return getPreferences().getString(PREF_LICENCE_KEY, null);
//    }

//    public static void setLicenceKeyExpirationDate(Date date) {
//        getPreferences().edit().putString(PREF_LICENCE_KEY_EXPIRATION_DATE, date != null ? TIME_FORMAT.format(date) : null).apply();
//    }

//    private static Date getLicenceKeyExpirationDate() {
//        try {
//            String date = getPreferences().getString(PREF_LICENCE_KEY_EXPIRATION_DATE, null);
//            return date != null ? TIME_FORMAT.parse(date) : null;
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//    public static boolean isLicenceKeyValid() {
//        Date date = getLicenceKeyExpirationDate();
//        if (date == null) {
//            return true;
//        } else {
//            return date.after(new Date());
//        }
//    }

    public static LocationInfo getDeviceLocation() {
        String ip = getPreferences().getString(DEVICE_IP_KEY, null);

        if (ip == null) {
            return null;
        } else {
            return new LocationInfo(ip);
        }
    }

    public static void setDeviceLocation(LocationInfo info) {
        if (info == null) {
            getPreferences().edit().putString(DEVICE_IP_KEY, null).apply();
        } else {
            getPreferences().edit().putString(DEVICE_IP_KEY, info.getIp()).apply();
        }
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(VPNApplication.getContext());
    }

    public static void introductionDisplayed(boolean b) {
        getPreferences().edit().putBoolean(WAS_VPN_INTRODUCTION_DISPLAYED, b).apply();
    }

    public static boolean introductionDisplayed() {
        return !VPNApplication.isDebug() && getPreferences().getBoolean(WAS_VPN_INTRODUCTION_DISPLAYED, false);
    }

    public static class WifiProtector {

        private static final int FREE_LICENSE = 1;
        private static final int PAID_LICENSE = 2;

        public static boolean hasData() {
            return clientOvpn() != null && !clientOvpn().isEmpty() && hasServers();
        }

        public static void invalidateData() {
            clientOvpn(null);
        }

        public static void vpnProfile(String profile) {
            getPreferences().edit().putString(PREF_VPN_PROFILE, profile).apply();
        }

        public static String vpnProfile() {
            return getPreferences().getString(PREF_VPN_PROFILE, "");
        }

        public static String clientOvpn() {
            return getPreferences().getString(PREF_CLIENT_OVPN, null);
        }

        public static void clientOvpn(String clientOvpn) {
            getPreferences().edit().putString(PREF_CLIENT_OVPN, clientOvpn).apply();
        }

        public static void licenceType(LicenceType licenceType) {
            int type = FREE_LICENSE;
            if (licenceType == LicenceType.PAID) {
                type = PAID_LICENSE;
            }

            getPreferences().edit().putInt(PREF_LICENCE_TYPE, type).apply();
        }

        public static LicenceType licenceType() {
            int type = getPreferences().getInt(PREF_LICENCE_TYPE, FREE_LICENSE);

            return type == FREE_LICENSE ? LicenceType.FREE : LicenceType.PAID;
        }

        public static void licenceKey(String key) {
            getPreferences().edit().putString(PREF_LICENCE_KEY, key).apply();
        }

        public static String licenceKey() {
            return getPreferences().getString(PREF_LICENCE_KEY, "");
        }

        public static boolean licenceKeyExpired() {
            return licenceKeyExpirationDate().before(new Date());
        }

        public static void licenceKeyExpirationDate(Date expirationDate) {
            getPreferences().edit().putLong(PREF_LICENCE_KEY_EXPIRATION_DATE, expirationDate.getTime()).apply();
        }

        public static Date licenceKeyExpirationDate() {
            return new Date(getPreferences().getLong(PREF_LICENCE_KEY_EXPIRATION_DATE, Long.MIN_VALUE));
        }

        public static boolean hasLicenceKey() {
            return licenceKey() != null && !licenceKey().isEmpty();
        }

        public static void hasServers(boolean b) {
            getPreferences().edit().putBoolean(PREF_CLIENT_OVPN_HAS_SERVERS, b).apply();
        }

        private static boolean hasServers() {
            return getPreferences().getBoolean(PREF_CLIENT_OVPN_HAS_SERVERS, false);
        }

        public static void clearLicenceKey() {
            getPreferences().edit().putString(PREF_LICENCE_KEY, "").apply();
            getPreferences().edit().putLong(PREF_LICENCE_KEY_EXPIRATION_DATE, -1).apply();
            licenceType(LicenceType.FREE);
        }
    }

    public static class Subscription {
        public static void isActive(boolean b) {
            getPreferences().edit().putBoolean(PREF_IS_SUBSCRIPTION_ACTIVE, b).apply();
        }

        public static boolean isActive() {
            return getPreferences().getBoolean(PREF_IS_SUBSCRIPTION_ACTIVE, false);
        }
    }

    public static class User {
        public static boolean isPaid() {
            return Subscription.isActive() || WifiProtector.licenceType() == LicenceType.PAID;
        }

        public static boolean exceededUsageLimit() {
            final boolean trackedToday = getLastTrackedDate() != null && getLastTrackedDate().equals(DATE_FORMAT.format(new Date()));
            return trackedToday && getTrackedTime() >= getAllowedTime();
        }

        public static long getAllowedTime() {
            return VPNApplication.getContext().getResources().getInteger(R.integer.allowed_vpn_time_for_free_users_in_seconds);
        }

        public static long getTrackedTime() {
            return getPreferences().getLong(PREF_TRACKED_TIME, 0);
        }

        public static void updateTrackedTime(long trackedTime) {
            getPreferences().edit().putLong(PREF_TRACKED_TIME, trackedTime).apply();
        }

        public static String getLastTrackedDate() {
            return getPreferences().getString(PREF_TRACKED_DATE, null);
        }

        public static void updateLastTrackedDate(String date) {
            getPreferences().edit().putString(PREF_TRACKED_DATE, date).apply();
        }
    }
}
