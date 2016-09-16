package com.wifiprotector.android.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="LocationInfo")
public class LocationInfo {
    @Element(name="IP")
    private String ip;

    @Element(name="CountryName")
    private String countryName;

    @Element(name="CountryCode")
    private String countryCode;

    @Element(name="ContinentCode")
    private String continentCode;

    public LocationInfo() {
    }

    public LocationInfo(String ipAddress) {
        this.ip = ipAddress;
    }

    public String getIp() {
        return ip;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getContinentCode() {
        return continentCode;
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "ip='" + ip + '\'' +
                ", countryName='" + countryName + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", continentCode='" + continentCode + '\'' +
                '}';
    }
}
