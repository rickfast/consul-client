package com.orbitz.consul;

import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.Check;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.junit.Rule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Map;
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
        Consul client = Consul.newClient();
        Agent agent = client.agentClient().getAgent();

        assertNotNull(agent);
        assertEquals("127.0.0.1", agent.getConfig().getClientAddr());
    }

    @Test
    @ConsulRunning
    public void shouldRegister() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId);

        boolean found = false;

        for(ServiceHealth health : client.healthClient().getServiceHealth(serviceName)) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test(expected = ConsulException.class)
    @ConsulRunning
    public void shouldFailOnDuplicateRegistration() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId);
        client.agentClient().register(8080, 10000L, serviceName, serviceId);
    }

    @Test
    @ConsulRunning
    public void shouldGetChecks() {
        Consul client = Consul.newClient();
        String id = UUID.randomUUID().toString();
        client.agentClient().register(8080, 20L, UUID.randomUUID().toString(), id);

        boolean found = false;

        for(Map.Entry<String, Check> check : client.agentClient().getChecks().entrySet()) {
            if(check.getValue().getCheckId().equals("service:" + id)) {
                found = true;
            };
        }

        assertTrue(found);
    }

    @Test
    @ConsulRunning
    public void shouldGetServices() {
        Consul client = Consul.newClient();
        String id = UUID.randomUUID().toString();
        client.agentClient().register(8080, 20L, UUID.randomUUID().toString(), id);

        boolean found = false;

        for(Map.Entry<String, Service> service : client.agentClient().getServices().entrySet()) {
            if(service.getValue().getId().equals(id)) {
                found = true;
            };
        }

        assertTrue(found);
    }
}
