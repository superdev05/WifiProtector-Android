package com.wifiprotector.android.model;

public enum RegisterUserStatus {

    LICENCE_EXPIRED(-11), KEY_NOT_VALID(-1), LICENCE_VALID(0);

    private int code = 0;

    RegisterUserStatus(int code) {
        this.code = code;
    }

    public static RegisterUserStatus fromCode(int code) {
        if (code == -11) {
            return LICENCE_EXPIRED;
        } else if (code == 0) {
            return LICENCE_VALID;
        } else {
            return KEY_NOT_VALID;
        }
    }
}
