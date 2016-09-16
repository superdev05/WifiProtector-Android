package com.wifiprotector.android.utils;

import com.wifiprotector.android.VpnProfileBuilder;
import com.wifiprotector.android.model.VPNServer;
import com.wifiprotector.android.model.VPNServerList;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;

public class OvpnProfile {
    public static VpnProfile create(String vpnProfile, VPNServer server) throws Exception {
        VpnProfileBuilder builder = new VpnProfileBuilder(vpnProfile);
        builder.server(server);

        ConfigParser cp = new ConfigParser();
        InputStreamReader isr;

        isr = new InputStreamReader(new ByteArrayInputStream(builder.build().getBytes()), "UTF-8");
        cp.parseConfig(isr);

        return cp.convertProfile();
    }
}
