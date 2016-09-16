package com.wifiprotector.android.model;

public class Endpoint {
    private final String ip;
    private final int port;

    public Endpoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    public String getProto() {
        if (port == 443) {
            return "tcp";
        } else {
            return "udp";
        }
    }
}
