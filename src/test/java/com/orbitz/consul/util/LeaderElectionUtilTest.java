package com.orbitz.consul.util;

import com.orbitz.consul.Consul;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LeaderElectionUtilTest {

    @Test
    public void testGetLeaderInfoForService() throws Exception {
        Consul client = Consul.newClient();
        LeaderElectionUtil leutil = new LeaderElectionUtil(client);
        final String serviceName = "myservice100";
        final String serviceInfo = "serviceinfo";

        leutil.releaseLockForService(serviceName);
        assertNull(leutil.getLeaderInfoForService(serviceName));
        assertEquals(serviceInfo, leutil.electNewLeaderForService(serviceName, serviceInfo));
        assertTrue(leutil.releaseLockForService(serviceName));
    }


}