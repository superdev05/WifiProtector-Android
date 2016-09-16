package com.wifiprotector.android.api;

import android.net.Uri;
import android.os.Build;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.UniqueIdGenerator;
import com.wifiprotector.android.VPNApplication;
import com.wifiprotector.android.log.SecuredLogManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import de.blinkt.openvpn.BuildConfig;
import okio.Buffer;
import okio.ByteString;
import roboguice.util.temp.Ln;
import timber.log.Timber;

public abstract class BaseRequest<T> extends OkHttpSpiceRequest<T> {

    public static final String FREE_LICENSE = "1";
    public static final String PAID_LICENSE = "2";

    private String deviceId;

    public BaseRequest(Class<T> clazz) {
        super(clazz);
        this.deviceId = UniqueIdGenerator.getDeviceId(VPNApplication.getContext());
    }

    protected String getDeviceId() {
        return deviceId;
    }

    protected String getServiceUrl() {
        return "https://services.wifiprotector.com/wifiprotwebservice.asmx/" + getServiceName();
    }

    protected abstract String getServiceName();

    protected Request getOkHttpRequest() {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(buildUrl());

        if (postParams() != null) {
            requestBuilder.post(buildPostParams());
        }

        return requestBuilder.build();
    }

    private String buildUrl() {
        Uri.Builder uriBuilder = Uri.parse(getServiceUrl()).buildUpon();

        for (Map.Entry<String, String> entry : getParams().entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        uriBuilder.appendQueryParameter("android", Build.VERSION.RELEASE);
        uriBuilder.appendQueryParameter("ClientVersion", VPNApplication.CLIENT_VERSION);
        uriBuilder.appendQueryParameter("licenseType", getLicenceType());

        return uriBuilder.build().toString();
    }

    protected String getLicenceType() {
        return Preferences.User.isPaid() ? PAID_LICENSE : FREE_LICENSE;
    }

    private RequestBody buildPostParams() {
        FormEncodingBuilder formBuilder = new FormEncodingBuilder();
        for (Map.Entry<String, String> entry : postParams().entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        return formBuilder.build();
    }

    protected Map<String, String> getParams() {
        return Collections.emptyMap();
    }

    protected Map<String, String> postParams() {
        return null;
    }

    protected Response executeRequest() throws IOException {
        Request request = getOkHttpRequest();
        SecuredLogManager.getLogManager().writeLog(4, "WFProctector", String.format( "[request] %s", request.toString()));

        Response response = getOkHttpClient().newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Buffer clonedBuffer = response.body().source().buffer().clone();
            SecuredLogManager.getLogManager().writeLog(4, "WFProctector", "[response] " + ByteString.of(clonedBuffer.readByteArray()).toString());
        }
        return response;
    }
}
