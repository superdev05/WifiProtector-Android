package com.wifiprotector.android.api;

import java.util.Date;

public class ConnectionInfoEx2 {
    private String caCrt;
    private String taKey;
    private String clientCrt;
    private String clientKey;
    private String clientOvpn;
    private Date validNotBefore;
    private Date validNotAfter;

    public ConnectionInfoEx2(String caCrt, String taKey, String clientCrt, String clientKey,
                             String clientOvpn, Date validNotBefore, Date validNotAfter) {
        this.caCrt = caCrt;
        this.taKey = taKey;
        this.clientCrt = clientCrt;
        this.clientKey = clientKey;
        this.clientOvpn = clientOvpn;
        this.validNotBefore = validNotBefore;
        this.validNotAfter = validNotAfter;
    }

    public String getCaCrt() {
        return caCrt;
    }

    public String getTaKey() {
        return taKey;
    }

    public String getClientCrt() {
        return clientCrt;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getClientOvpn() {
        return clientOvpn;
    }

    public Date getValidNotBefore() {
        return validNotBefore;
    }

    public Date getValidNotAfter() {
        return validNotAfter;
    }
}
