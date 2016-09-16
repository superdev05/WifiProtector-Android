package com.wifiprotector.android.model;

import com.wifiprotector.android.log.SecuredLogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hugo.weaving.DebugLog;
import roboguice.util.temp.Ln;
import timber.log.Timber;

public class VPNServerList extends ArrayList<VPNServer> {

    public VPNServerList() {

    }

    public VPNServerList(List<VPNServer> servers) {
        this.addAll(servers);
    }

    public static void updatePriorities(Map<String, Integer> priorities, List<VPNServer> servers) {
        for (Map.Entry<String, Integer> entry : priorities.entrySet()) {
            VPNServer server = getServerByName(entry.getKey(), servers);
            if (server != null) {
                server.setPriority(entry.getValue());
            }
        }
    }

    public static VPNServer getServerByName(String name, List<VPNServer> servers) {
        for (VPNServer server : servers) {
            if (server.getName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public VPNServer findServerByEndpointIp(String ip) {
        for (VPNServer server : this) {
            for (Endpoint endpoint : server.getEndpoints()) {
                if (endpoint.getIp().equals(ip)) {
                    return server;
                }
            }
        }
        return null;
    }

    public VPNServer getBestServer() {
        return getBestServer(this);
    }

    public VPNServer getBestServerInCountry(String country) {
        List<VPNServer> serversInCountry = getServersInCountry(country);
        return getBestServer(serversInCountry);
    }

    public VPNServerList getServersInCountry(String country) {
        VPNServerList serversInCountry = new VPNServerList();
        for (VPNServer server : this) {
            if (server.getCountry().equals(country)) {
                serversInCountry.add(server);
            }
        }

        Collections.sort(serversInCountry, new PriorityComparator());

        return serversInCountry;
    }

    private VPNServer getBestServer(List<VPNServer> servers) {
        VPNServer best = null;
        int bestPriority = -1;
        for (VPNServer server : servers) {
            if (server.getPriority() > bestPriority) {
                bestPriority = server.getPriority();
                best = server;
            }
        }
        return best;
    }

    public List<VPNCountry> getServersGroupedByCountry() {
        Set<String> countrySet = new HashSet<String>();
        for (VPNServer server: this) {
            countrySet.add(server.getCountry());
        }

        LinkedList<VPNCountry> countries = new LinkedList<VPNCountry>();
        for (String countryCode: countrySet) {
            countries.add(new VPNCountry(countryCode, getServersInCountry(countryCode)));
        }

        Collections.sort(countries, new Comparator<VPNCountry>() {
            @Override
            public int compare(VPNCountry lhs, VPNCountry rhs) {
                return lhs.getDisplayName().compareTo(rhs.getDisplayName());
            }
        });
        return countries;
    }

    public void sortByPriority() {
        Collections.sort(this, new PriorityComparator());
    }

    public void sortByPriority(VPNServer firstServer) {
        sortByPriority();

        this.remove(firstServer);
        this.add(0, firstServer);
    }

    @DebugLog
    public void clearUnavailable() {
        List<VPNServer> unavailableServers = new ArrayList<VPNServer>();

        for (VPNServer server: this) {
            if (!server.hasPriority()) {
                unavailableServers.add(server);

                SecuredLogManager.getLogManager().writeLog(3, "WFProctector", String.format( "Unavailable server: %s", server.toString()));
            }
        }

        removeAll(unavailableServers);
    }

    public static class PriorityComparator implements Comparator<VPNServer> {
        @Override
        public int compare(VPNServer first, VPNServer second) {
            if (first.getPriority() < second.getPriority()) {
                return 1;
            } else if (first.getPriority() > second.getPriority()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
