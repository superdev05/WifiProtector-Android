package com.wifiprotector.android.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import hugo.weaving.DebugLog;

public abstract class PingRequest<RESULT> extends BaseRequest<RESULT> {
    public static final int TIMEOUT = 600;

    public PingRequest(Class<RESULT> clazz) {
        super(clazz);
    }

    @DebugLog
    protected int ping(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://" + address).openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        long startTime = System.currentTimeMillis();
        connection.connect();
        long endTime = System.currentTimeMillis();

        return (int) (endTime - startTime);
    }
}
