package com.wifiprotector.android.utils;

import com.wifiprotector.android.model.LicenceType;
import com.wifiprotector.android.model.VPNServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OvpnParser {

    private final String content;

    public OvpnParser(String ovpnContent) {
        this.content = ovpnContent;
    }

    public List<VPNServer> parseServers() throws Exception {
        List<VPNServer> servers = new ArrayList<VPNServer>();

        Scanner scanner = new Scanner(content);

        while (scanner.hasNextLine()) {
            String lineChunks[] = scanner.nextLine().split("\\s+");

            if (lineChunks.length == 0) {
                continue;
            }
            if (lineChunks[0].contains("srv2")) {
                String serverOrigin[] = lineChunks[2].split(",");

                VPNServer server = new VPNServer();
                server.setName(lineChunks[1]);
                server.setCountry(serverOrigin[0]);
                server.setContinent(serverOrigin[1]);

                servers.add(server);
            } else if (lineChunks[0].contains("remote2") && servers.size() > 0) {
                VPNServer lastServer = servers.get(servers.size() - 1);
                lastServer.addEndpoint(lineChunks[1], Integer.parseInt(lineChunks[2]));
            }
        }

        scanner.close();
        return servers;
    }

    public LicenceType parseLicenceType() {
        if (content.contains("# ! PAID !")) {
            return LicenceType.PAID;
        } else {
            return LicenceType.FREE;
        }
    }

    public static boolean hasServers(String clientOvpn) {
        OvpnParser parser = new OvpnParser(clientOvpn);
        try {
            return !parser.parseServers().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
