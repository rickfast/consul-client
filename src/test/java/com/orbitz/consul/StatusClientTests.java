package com.orbitz.consul;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class StatusClientTests {

    private static String ip;

    @BeforeClass
    public static void getIp() throws UnknownHostException {
        ip = String.format("%s:8300", InetAddress.getLocalHost().getHostAddress());
    }

    @Test
    public void shouldGetLeader() {
        assertEquals(ip, Consul.newClient().statusClient().getLeader());
    }

    @Test
    public void shouldGetPeers() {
        assertEquals(Arrays.asList(new String[] { ip }),
                Consul.newClient().statusClient().getPeers());
    }
}
