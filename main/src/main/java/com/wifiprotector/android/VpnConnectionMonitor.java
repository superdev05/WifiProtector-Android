package com.wifiprotector.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;

import com.wifiprotector.android.model.exception.TimeoutException;

import de.blinkt.openvpn.core.VpnStatus;

public class VpnConnectionMonitor extends BroadcastReceiver {

    public interface Listener {
        void onVpnConnected();

        void onVpnNotConnected(TimeoutException e);

        void onVpnConnecting();

        void onVpnConnected(String serverIp);
    }


    private VpnStatus.ConnectionStatus lastConnectionStatus;
    private Listener listener;

    private long connectionTimeoutMs;
    private CountDownTimer timeoutTimer;


    public VpnConnectionMonitor(long connectionTimeout, final Listener listener) {
        this.connectionTimeoutMs = connectionTimeout;
        this.listener = listener;
        this.timeoutTimer = new CountDownTimer(connectionTimeoutMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (listener != null) {
                    VpnConnectionMonitor.this.listener.onVpnNotConnected(new TimeoutException(connectionTimeoutMs));
                }
            }
        };
    }

    public void register(Context context, Listener listener) {
        this.listener = listener;

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(VPNApplication.ACTION_VPN_STATE_CHANGED));
    }

    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

        listener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        lastConnectionStatus = (VpnStatus.ConnectionStatus)
                intent.getSerializableExtra(VPNApplication.EXTRA_VPN_CONNECTION_STATUS);

        if (listener != null) {
            switch (lastConnectionStatus) {
                case LEVEL_CONNECTING_SERVER_REPLIED:
                case LEVEL_CONNECTING_NO_SERVER_REPLY_YET: {
                    listener.onVpnConnecting();
                    timeoutTimer.start();

                    break;
                }
                case LEVEL_CONNECTED: {
                    timeoutTimer.cancel();
                    listener.onVpnConnected();

                    break;
                }
                case LEVEL_NOTCONNECTED: {
                    timeoutTimer.cancel();
                    listener.onVpnNotConnected(null);

                    break;
                }
            }
        }
    }

    public VpnStatus.ConnectionStatus getConnectionStatus() {
        return lastConnectionStatus;
    }

    public boolean isConnected() {
        return lastConnectionStatus == VpnStatus.ConnectionStatus.LEVEL_CONNECTED;
    }
}
