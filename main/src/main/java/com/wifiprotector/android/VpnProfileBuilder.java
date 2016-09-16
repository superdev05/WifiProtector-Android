package com.wifiprotector.android;

import com.wifiprotector.android.model.Endpoint;
import com.wifiprotector.android.model.VPNServer;

import java.util.LinkedList;
import java.util.List;

public class VpnProfileBuilder {

    List<Endpoint> endpoints = new LinkedList<Endpoint>();
    private String caCrt;
    private String taKey;
    private String clientCrt;
    private String clientKey;

    private String template;

    public VpnProfileBuilder(String template) {
        this.template = template;
    }

    public VpnProfileBuilder servers(List<VPNServer> servers) {
        for (VPNServer server : servers) {
            endpoints.addAll(server.getEndpoints());
        }
        return this;
    }

    public VpnProfileBuilder server(VPNServer server) {
        endpoints.addAll(server.getEndpoints());
        return this;
    }

    public VpnProfileBuilder caCrt(String caCrt) {
        this.caCrt = caCrt;
        return this;
    }

    public VpnProfileBuilder taKey(String taKey) {
        this.taKey = taKey;
        return this;
    }

    public VpnProfileBuilder clientCrt(String clientCrt) {
        this.clientCrt = clientCrt;
        return this;
    }

    public VpnProfileBuilder clientKey(String clientKey) {
        this.clientKey = clientKey;
        return this;
    }

    private boolean canInsertKeys() {
        return caCrt != null && taKey != null && clientCrt != null && clientKey != null;
    }

    public String build() {
        StringBuilder endpointsStringBuilder = new StringBuilder();
        for (Endpoint e : endpoints) {
            endpointsStringBuilder.append(String.format("remote %s %s %s\n", e.getIp(), e.getPort(), e.getProto()));
        }

        String content = canInsertKeys() ? String.format(template, caCrt, taKey, clientCrt, clientKey) : template;
        StringBuilder profileStringBuilder = new StringBuilder(content);
        profileStringBuilder.insert(0, endpointsStringBuilder);
        return profileStringBuilder.toString();
    }
}
