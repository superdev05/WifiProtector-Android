package com.wifiprotector.android.api;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wifiprotector.android.log.SecuredLogManager;
import com.wifiprotector.android.utils.CertificateValidityExtractor;
import com.wifiprotector.android.utils.NetworkUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;

public class GetConnectionInfoEx2Request extends BaseRequest<ConnectionInfoEx2> {

    private String caCrt;
    private String taKey;
    private String clientCrt;
    private String clientKey;
    private String clientOvpn;
    private String ipAddress;
    private Date validNotBefore;
    private Date validNotAfter;

    public GetConnectionInfoEx2Request(String ipAddress) {
        super(ConnectionInfoEx2.class);
        this.ipAddress = ipAddress;
    }

    @Override
    protected String getServiceName() {
        return "GetConnectionInfoEx2";
    }

    @Override
    protected Map<String, String> postParams() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("uniqueId", getDeviceId());
        map.put("mac", NetworkUtils.getMacAddressSeparatedByHyphens());
        map.put("origIP", ipAddress);
        return map;
    }

    @Override
    public ConnectionInfoEx2 loadDataFromNetwork() throws Exception {
        Request request = getOkHttpRequest();

        SecuredLogManager.getLogManager().writeLog(4, "WFProctector", String.format("[request] %s", request.toString()));

        Response response = getOkHttpClient().newCall(request).execute();
        readZipResponse(response.body().byteStream());

        return new ConnectionInfoEx2(caCrt, taKey, clientCrt, clientKey, clientOvpn, validNotBefore, validNotAfter);
    }

    private void readZipResponse(InputStream inputStream) throws Exception {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    byteOutputStream.write(buffer, 0, count);
                }
                String fileContent = byteOutputStream.toString("UTF-8");
                String filename = zipEntry.getName();
                if (filename.equals("ca.crt")) {
                    caCrt = fileContent;
                } else if (filename.equals("ta.key")) {
                    taKey = fileContent;
                } else if (filename.equals("client.crt")) {
                    CertificateValidityExtractor validityExtractor = new CertificateValidityExtractor(fileContent);
                    validNotAfter = validityExtractor.getValidNotAfter();
                    validNotBefore = validityExtractor.getValidNotBefore();
                    clientCrt = extractClientCrt(fileContent);
                } else if (filename.equals("client.key")) {
                    clientKey = fileContent;
                } else if (filename.equals("client.ovpn")) {
                    clientOvpn = fileContent;
                }
            }
        } finally {
            zis.close();
        }
    }

    private String extractClientCrt(String text) {
        final int beginOfIndex = text.indexOf("-----");
        return text.substring(beginOfIndex);
    }
}
