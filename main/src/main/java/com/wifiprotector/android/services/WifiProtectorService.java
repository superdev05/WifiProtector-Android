package com.wifiprotector.android.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.VPNApplication;
import com.wifiprotector.android.VpnProfileBuilder;
import com.wifiprotector.android.api.ConnectionInfoEx2;
import com.wifiprotector.android.api.GetConnectionInfoEx2Request;
import com.wifiprotector.android.api.LocationInfo;
import com.wifiprotector.android.api.SoapSpiceService;
import com.wifiprotector.android.api.UpdateDeviceLocationInfoRequest;
import com.wifiprotector.android.log.SecuredLogManager;
import com.wifiprotector.android.model.LicenceType;
import com.wifiprotector.android.model.VPNServerList;
import com.wifiprotector.android.utils.OvpnParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

public class WifiProtectorService extends Service implements RequestListener {

    public static final String EXTRA_REINIT = "extraReinit";

    public static class Receiver extends BroadcastReceiver {
        private Listener listener;

        public interface Listener {
            void onWifiProtectorInitialized(VPNServerList servers, LicenceType licenceType);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            VPNServerList servers = (VPNServerList) intent.getSerializableExtra(VPNApplication.EXTRA_SERVERS);
            LicenceType licenceType = (LicenceType) intent.getSerializableExtra(VPNApplication.EXTRA_LICENSE_TYPE);

            if (listener != null) {
                listener.onWifiProtectorInitialized(servers, licenceType);
            }
        }

        public void register(Context context, Listener listener) {
            this.listener = listener;
            LocalBroadcastManager.getInstance(context).registerReceiver(
                    this, new IntentFilter(VPNApplication.ACTION_WP_INITIALIZED));
        }

        public void unregister(Context context) {
            listener = null;
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }

    protected SpiceManager spiceManager = new SpiceManager(SoapSpiceService.class);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        spiceManager.start(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean invalidate = intent != null && intent.getBooleanExtra(EXTRA_REINIT, false);

        if (invalidate || !Preferences.WifiProtector.hasData()) {
            init();
        } else {
            initFromCache();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initFromCache() {
        broadcastResult(Preferences.WifiProtector.clientOvpn());
        stopSelf();
    }

    private void init() {
        spiceManager.execute(new UpdateDeviceLocationInfoRequest(), this);
    }

    @Override
    public void onDestroy() {
        spiceManager.shouldStop();
        super.onDestroy();
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {

    }

    @Override
    public void onRequestSuccess(Object response) {
        if (response instanceof LocationInfo) {

            fetchWifiProtectorData((LocationInfo) response);

        } else if (response instanceof ConnectionInfoEx2) {
            ConnectionInfoEx2 info = (ConnectionInfoEx2) response;

            Preferences.WifiProtector.vpnProfile(buildOvpnProfileTemplate(info));
            Preferences.WifiProtector.clientOvpn(info.getClientOvpn());
            Preferences.WifiProtector.hasServers(OvpnParser.hasServers(info.getClientOvpn()));

            broadcastResult(info.getClientOvpn());
            stopSelf();
        }
    }

    private String buildOvpnProfileTemplate(ConnectionInfoEx2 info) {
        return new VpnProfileBuilder(vpnProfileTemplate()).caCrt(info.getCaCrt())
                .clientCrt(info.getClientCrt())
                .clientKey(info.getClientKey())
                .taKey(info.getTaKey()).build();
    }

    public static String vpnProfileTemplate() {
        StringBuilder buf = new StringBuilder();
        InputStream inputStream;
        try {
            inputStream = VPNApplication.getContext().getAssets().open("profile_template.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
                buf.append("\n");
            }
            in.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't read profile template file", e);
        }

        return buf.toString();
    }

    private void fetchWifiProtectorData(LocationInfo locationInfo) {
        spiceManager.execute(new GetConnectionInfoEx2Request(locationInfo.getIp()), this);
    }

    private void broadcastResult(String clientOvpn) {
        OvpnParser parser = new OvpnParser(clientOvpn);
        VPNServerList servers = new VPNServerList();
        LicenceType licenceType = LicenceType.FREE;
        try {
            servers.addAll(parser.parseServers());
            licenceType = parser.parseLicenceType();
        } catch (Exception e) {
            SecuredLogManager.getLogManager().writeLog(6, "WFProctector",  e.getMessage());
        }

        Intent intent = new Intent(VPNApplication.ACTION_WP_INITIALIZED);
        intent.putExtra(VPNApplication.EXTRA_SERVERS, servers);
        intent.putExtra(VPNApplication.EXTRA_LICENSE_TYPE, licenceType);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
