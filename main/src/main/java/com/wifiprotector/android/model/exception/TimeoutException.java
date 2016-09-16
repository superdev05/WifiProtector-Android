package com.wifiprotector.android.model.exception;

import com.wifiprotector.android.model.VPNServer;

public class TimeoutException extends Exception {
    private VPNServer server;
    private long timeoutMs;

    public TimeoutException(long connectionTimeoutMs) {
        this.timeoutMs = connectionTimeoutMs;
    }

    public void setServer(VPNServer server) {
        this.server = server;
    }

    public VPNServer getServer() {
        return server;
    }

    @Override
    public String getMessage() {
        return String.format("Timeout(%dms) connecting to %s", timeoutMs, server);
    }
}
