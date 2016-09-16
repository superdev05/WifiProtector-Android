package com.wifiprotector.android.api;

import com.squareup.okhttp.Response;
import com.wifiprotector.android.model.Endpoint;
import com.wifiprotector.android.model.VPNServer;
import com.wifiprotector.android.model.VPNServerList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateServerPrioritiesRequest extends PingRequest<VPNServerList> {

    private List<VPNServer> servers;
    private Map<String, String> pings = new HashMap<String, String>();

    public UpdateServerPrioritiesRequest(List<VPNServer> servers) {
        super(VPNServerList.class);
        this.servers = new LinkedList<VPNServer>(servers);
    }

    @Override
    protected String getServiceName() {
        return "GetServerPriorities";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("uniqueID", getDeviceId());
        map.put("productID", "1");
        map.putAll(pings);
        return map;
    }

    @Override
    public VPNServerList loadDataFromNetwork() throws Exception {
        for (VPNServer server : servers) {
            try {
                Endpoint endpoint = server.getEndpoints().get(0);
                server.setPing(ping(endpoint.getIp()));
            } catch (Exception e) {
                server.setPing(9999);
            }
            pings.put(server.getName(), String.valueOf(server.getPing()));
        }

        Response response = getOkHttpClient().newCall(getOkHttpRequest()).execute();

        ServerPriorities priorities = parsePriorities(response.body().string());
        VPNServerList.updatePriorities(priorities, servers);

        return new VPNServerList(servers);
}

    private ServerPriorities parsePriorities(String response) {
        String[] splits = response.split("&");
        ServerPriorities priorities = new ServerPriorities();
        for (String split: splits) {
            String[] vals = split.split("=");
            String key = vals[0];
            if (key.equals("licenseType") || key.equals("productID")) {
                // skip
            } else {
                priorities.put(vals[0], Integer.parseInt(vals[1]));
            }
        }
        return priorities;
    }
}
