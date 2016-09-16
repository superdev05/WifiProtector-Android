package com.wifiprotector.android.activities;

        import android.app.NotificationManager;
        import android.content.BroadcastReceiver;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.ServiceConnection;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.support.v4.content.LocalBroadcastManager;

        import com.wifiprotector.android.Preferences;
        import com.wifiprotector.android.VPNApplication;
        import com.wifiprotector.android.VpnConnectionMonitor;
        import com.wifiprotector.android.api.RequestListenerAdapter;
        import com.wifiprotector.android.api.UpdateServerPrioritiesRequest;
        import com.wifiprotector.android.dialog.iap.TimeLimitExceededDialog;
        import com.wifiprotector.android.model.LicenceType;
        import com.wifiprotector.android.model.VPNServerList;
        import com.wifiprotector.android.model.exception.TimeoutException;
        import com.wifiprotector.android.services.WifiProtectorService;

        import de.blinkt.openvpn.LaunchVPN;
        import de.blinkt.openvpn.VpnProfile;
        import de.blinkt.openvpn.core.OpenVPNService;
        import de.blinkt.openvpn.core.ProfileManager;
        import hugo.weaving.DebugLog;

public abstract class BaseVpnActivity extends BaseBillingActivity implements
        VpnConnectionMonitor.Listener,
        WifiProtectorService.Receiver.Listener {

    public enum Status {
        WP_NOT_INITIALIZED,
        WP_INITIALIZING,
        WP_INITIALIZED,
        SERVERS_INITIALIZING,
        SERVERS_INITIALIZED,
        VPN_NOT_CONNECTED,
        VPN_BEFORE_CONNECTING,
        VPN_CONNECTING,
        VPN_CONNECTED
    }

    private class AsyncStartVpn extends AsyncTask<Void, Void, Void> {

        private VpnProfile vpnProfile;
        private String uuid;

        private AsyncStartVpn(VpnProfile vpnProfile) {
            this.vpnProfile = vpnProfile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProfileManager.getInstance(BaseVpnActivity.this).addProfile(vpnProfile);
            uuid = vpnProfile.getUUID().toString();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startVpn();
        }

        protected void startVpn() {
            Intent intent = new Intent(BaseVpnActivity.this, LaunchVPN.class);
            intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
            intent.putExtra(LaunchVPN.EXTRA_HIDELOG, false);
            intent.setAction(Intent.ACTION_MAIN);

            startActivity(intent);
        }
    }

    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private Status status = Status.WP_NOT_INITIALIZED;
    protected OpenVPNService openVpnService;

    BroadcastReceiver limitExceededReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopVpn();
            showTimeLimitExceededDialog();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(VPNApplication.NOTIFICATION_TIME_EXCEEDED_ID);
        }
    };

    private ServiceConnection openVpnServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            openVpnService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            openVpnService = null;
        }
    };

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        private Boolean wasOnline = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (wasOnline == null || wasOnline != isOnline()) {
                wasOnline = isOnline();
                onConnectivityChanged();
            }
        }
    };

    private VpnConnectionMonitor vpnConnectionMonitor;
    private WifiProtectorService.Receiver wifiProtectorReceiver = new WifiProtectorService.Receiver();

    private VPNServerList wifiProtectorServers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        vpnConnectionMonitor = new VpnConnectionMonitor(CONNECTION_TIMEOUT_MS, this);
        wifiProtectorReceiver.register(this, this);
        initWifiProtector();

    }

    protected abstract void initView();

    protected void initWifiProtector() {
        updateStatus(Status.WP_INITIALIZING);
        startService(new Intent(this, WifiProtectorService.class));
    }

    protected void reinitWifiProtector() {
        Intent intent = new Intent(this, WifiProtectorService.class);
        intent.putExtra(WifiProtectorService.EXTRA_REINIT, true);
        startService(intent);
    }

    @Override
    public void onDestroy() {
        wifiProtectorReceiver.unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        vpnConnectionMonitor.register(this, this);
        LocalBroadcastManager.getInstance(this).registerReceiver(limitExceededReceiver, new IntentFilter(VPNApplication.ACTION_LIMIT_EXCEEDED));

        if (Preferences.WifiProtector.hasLicenceKey() && Preferences.WifiProtector.licenceKeyExpired()) {
            reinitWifiProtector();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        vpnConnectionMonitor.unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(limitExceededReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, openVpnServiceConnection, Context.BIND_AUTO_CREATE);
        registerConnectivityReceiver();
    }

    private void registerConnectivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(openVpnServiceConnection);
        unregisterConnectivityReceiver();
    }

    private void unregisterConnectivityReceiver() {
        unregisterReceiver(connectivityReceiver);
    }

    protected void asyncStartVpn(VpnProfile vpnProfile) {
        new AsyncStartVpn(vpnProfile).execute();
    }

    protected void stopVpn() {
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        if (openVpnService != null && openVpnService.getManagement() != null)
            openVpnService.getManagement().stopVPN();

        updateStatus(Status.VPN_NOT_CONNECTED);
    }

    protected void showTimeLimitExceededDialog() {
        new TimeLimitExceededDialog().showAllowingStateLoss(getSupportFragmentManager());
    }

    protected abstract void onConnectivityChanged();

    @Override
    public void onWifiProtectorInitialized(VPNServerList servers, LicenceType licenceType) {
        wifiProtectorServers = servers;
        updateWifiProtectorServers();

        updateStatus(Status.WP_INITIALIZED);
    }

    protected void onWifiProtectorLicenceCodeExpired() {
        Preferences.WifiProtector.clearLicenceKey();
        reinitWifiProtector();
    }

    @DebugLog
    protected void updateWifiProtectorServers() {
        if (wifiProtectorServers != null && !wifiProtectorServers.isEmpty()) {
            updateStatus(Status.SERVERS_INITIALIZING);

            spiceManager.execute(new UpdateServerPrioritiesRequest(wifiProtectorServers),
                    new RequestListenerAdapter<VPNServerList>() {
                        @Override
                        public void onRequestSuccess(VPNServerList servers) {
                            servers.clearUnavailable();

                            onWifiProtectorServersUpdated(servers);
                        }
                    });
        }
    }

    protected void updateStatus(Status status) {
        this.status = status;

        onStatusUpdated();
    }

    protected abstract void onStatusUpdated();

    protected void onWifiProtectorServersUpdated(VPNServerList servers) {
        updateStatus(Status.SERVERS_INITIALIZED);
    }

    @Override
    public void onVpnConnecting() {
        updateStatus(Status.VPN_CONNECTING);
    }

    @Override
    public void onVpnConnected() {
        updateStatus(Status.VPN_CONNECTED);
    }

    @Override
    public void onVpnConnected(String serverIp) {
        onVpnConnected();
    }

    @Override
    public abstract void onVpnNotConnected(TimeoutException e);

    public boolean isConnected() {
        return status == Status.VPN_CONNECTED;
    }

    public boolean isTimeLimitExceeded() {
        return !Preferences.User.isPaid() && Preferences.User.exceededUsageLimit();
    }

    public Status getStatus() {
        return status;
    }
}
