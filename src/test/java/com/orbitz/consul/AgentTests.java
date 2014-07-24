package com.orbitz.consul;

import com.orbitz.consul.model.agent.Agent;
import org.junit.Rule;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgentTests {

    @Rule
    public ConsulRule consulRule = new ConsulRule();

    @Test
    @ConsulRunning
    public void shouldRetrieveAgentInformation() throws UnknownHostException {
        ConsulClient client = ConsulClient.newClient();
        Agent agent = client.getAgent();

        assertNotNull(agent);
        assertEquals("127.0.0.1", agent.getConfig().getClientAddr());
        assertEquals(Inet4Address.getLocalHost().getHostAddress(), agent.getConfig().getAdvertiseAddr());
    }

    @Test
    @ConsulRunning
    public void shouldRegister() throws UnknownHostException {
        ConsulClient client = ConsulClient.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.register(8080, 10000L, serviceName, serviceId);

        assertTrue(Arrays.asList(client.getServiceHealth(serviceName)).stream()
                .anyMatch((health) -> health.getService().getId().equals(serviceId)));
    }

    @Test(expected = ConsulException.class)
    @ConsulRunning
    public void shouldFailOnDuplicateRegistration() throws UnknownHostException {
        ConsulClient client = ConsulClient.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.register(8080, 10000L, serviceName, serviceId);

        client.register(8080, 10000L, serviceName, serviceId);
    }
}
