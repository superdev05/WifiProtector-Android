package com.wifiprotector.android.api;

import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.log.SecuredLogManager;
import com.wifiprotector.android.model.LicenceType;
import com.wifiprotector.android.model.RegisterUserStatus;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class RegisterUser3Response {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private RegisterUserStatus registerUserStatus;

    public RegisterUser3Response(String activationCode, String value, String message) {
        try {
            registerUserStatus = RegisterUserStatus.fromCode(Integer.parseInt(value));
        } catch (Exception e) {
            registerUserStatus = RegisterUserStatus.KEY_NOT_VALID;
        }

        if (registerUserStatus == RegisterUserStatus.LICENCE_VALID) {
            Preferences.WifiProtector.licenceType(LicenceType.PAID);
            Preferences.WifiProtector.licenceKey(activationCode);
            Preferences.WifiProtector.licenceKeyExpirationDate(extractExpirationDate(message));
        }
    }

    private Date extractExpirationDate(String message) {
        String date = StringUtils.substringBetween(message, "expirationDate=[", "]");
        if (date == null || date.equals("")) {
            return new Date(Long.MAX_VALUE);
        } else {
            try {
                return DATE_FORMAT.parse(date);
            } catch (ParseException e) {
                SecuredLogManager.getLogManager().writeLog(6, "WFProctector", e.getMessage());
            }
        }

        return new Date(Long.MIN_VALUE);
    }


    public RegisterUserStatus getRegisterStatus() {
        return registerUserStatus;
    }
}

