package com.wifiprotector.android.model;

import java.util.List;
import java.util.Locale;

public class VPNCountry {
    private String countryCode;
    private String countryDisplayName;
    private VPNServerList servers;
    private boolean isAutoSelection = false;

    public VPNCountry(String countryCode, VPNServerList servers) {
        this.countryCode = countryCode;
        this.countryDisplayName = new Locale("", countryCode.toUpperCase()).getDisplayName();
        this.servers = servers;
    }

    public VPNCountry() {
        this.isAutoSelection = true;
    }

    public boolean isAutoSelection() {
        return isAutoSelection;
    }

    public String getCountryCode() {
        return countryCode;
    }
    public String getDisplayName() {
        return countryDisplayName;
    }
    public VPNServerList getServers() {
        return servers;
    }

    public static VPNCountry findByServer(List<VPNCountry> vpnCountries, VPNServer server) {
        for (VPNCountry vpnCountry : vpnCountries) {
            if (vpnCountry.getServers().contains(server)) {
                return vpnCountry;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof VPNCountry) {
            VPNCountry secondCountry = (VPNCountry) o;
            return secondCountry.getCountryCode().equals(countryCode);
        } else {
            return false;
        }
    }
}
