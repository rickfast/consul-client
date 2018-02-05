package com.orbitz.consul;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatusClientTests extends BaseIntegrationTest {

    private static Set<InetAddress> ips = new HashSet<>();

    @BeforeClass
    public static void getIps() throws RuntimeException {
        try {
            InetAddress[] externalIps = InetAddress.getAllByName(InetAddress.getLocalHost().getCanonicalHostName());
            ips.addAll(Arrays.asList(externalIps));
        } catch (UnknownHostException ex) {
            Logger.getLogger(StatusClientTests.class.getName()).log(Level.WARNING, "Could not determine fully qualified host name. Continuing.", ex);
        }
        Enumeration<NetworkInterface> netInts;
        try {
            netInts = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netInt : Collections.list(netInts)) {
                for (InetAddress inetAddress : Collections.list(netInt.getInetAddresses())) {
                    ips.add(inetAddress);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(StatusClientTests.class.getName()).log(Level.WARNING, "Could not access local network adapters. Continuing", ex);
        }
        if (ips.isEmpty()) {
            throw new RuntimeException("Unable to discover any local IP addresses");
        }
    }

    private boolean isLocalIp(String ipAddress) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAddress);
        return ips.contains(ip);
    }

    private static final String IP_PORT_DELIM = ":";

    private String getIp(String ipAndPort) {
        return ipAndPort.substring(0, ipAndPort.indexOf(IP_PORT_DELIM));
    }

    private int getPort(String ipAndPort) {
        return Integer.valueOf(ipAndPort.substring(ipAndPort.indexOf(IP_PORT_DELIM) + 1));
    }

    private void assertLocalIpAndCorrectPort(String ipAndPort) throws UnknownHostException {
        String ip = getIp(ipAndPort);
        int port = getPort(ipAndPort);
        assertTrue(isLocalIp(ip));
        assertEquals(8300, port);
    }

    @Test
    public void shouldGetLeader() throws UnknownHostException {
        String ipAndPort = client.statusClient().getLeader();
        assertLocalIpAndCorrectPort(ipAndPort);
    }

    @Test
    public void shouldGetPeers() throws UnknownHostException {
        List<String> peers = client.statusClient().getPeers();
        for (String ipAndPort : peers) {
            assertLocalIpAndCorrectPort(ipAndPort);
        }
    }
}
