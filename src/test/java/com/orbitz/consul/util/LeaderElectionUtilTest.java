package com.orbitz.consul.util;

import com.orbitz.consul.Consul;
import junit.framework.TestCase;
import org.junit.Test;

public class LeaderElectionUtilTest extends TestCase {

    @Test
    public void testGetLeaderInfoForService() throws Exception {
        Consul client = Consul.newClient();
        final String serviceName = "myservice100";
        final String serviceInfo = "serviceinfo";

        LeaderElectionUtil.releaseLockForService(client, serviceName);
        assertNull(LeaderElectionUtil.getLeaderInfoForService(client, serviceName));
        assertEquals(serviceInfo, LeaderElectionUtil.electNewLeaderForService(client, serviceName, serviceInfo));
        assertTrue(LeaderElectionUtil.releaseLockForService(client, serviceName));
    }


}