package com.wifiprotector.android.model;

import com.wifiprotector.android.api.PingRequest;

import java.util.ArrayList;
import java.util.List;

public class VPNServer {
    private static final int MIN_PRIORITY = 0;

    private List<Endpoint> endpoints = new ArrayList<Endpoint>();
    private String name;
    private String country;
    private String continent;
    private int ping = Integer.MAX_VALUE;
    private int priority = -1;

    public void addEndpoint(String ip, int port) {
        endpoints.add(new Endpoint(ip, port));
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getContinent() {
        return continent;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "VPNServer{" +
                "endpoints=" + endpoints +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", continent='" + continent + '\'' +
                ", ping=" + ping +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof VPNServer) {
            VPNServer secondServer = (VPNServer) o;
            return secondServer.getName().equals(name);
        } else {
            return false;
        }
    }

    public String getIp() {
        if (endpoints != null && endpoints.size() > 0) {
            return getEndpoints().get(0).getIp();
        }
        return null;
    }

    public boolean hasTimeoutPing() {
        return ping > PingRequest.TIMEOUT;
    }

    public boolean hasPriority() {
        return priority > MIN_PRIORITY;
    }
}
