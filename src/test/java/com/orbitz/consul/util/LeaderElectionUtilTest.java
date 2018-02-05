package com.orbitz.consul.util;

import com.orbitz.consul.BaseIntegrationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeaderElectionUtilTest extends BaseIntegrationTest {

    @Test
    public void testGetLeaderInfoForService() throws Exception {
        LeaderElectionUtil leutil = new LeaderElectionUtil(client);
        final String serviceName = "myservice100";
        final String serviceInfo = "serviceinfo";

        leutil.releaseLockForService(serviceName);
        assertFalse(leutil.getLeaderInfoForService(serviceName).isPresent());
        assertEquals(serviceInfo, leutil.electNewLeaderForService(serviceName, serviceInfo).get());
        assertTrue(leutil.releaseLockForService(serviceName));
    }
}