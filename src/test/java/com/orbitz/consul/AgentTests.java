package com.orbitz.consul;

import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.junit.Rule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    @Test
    @ConsulRunning
    public void shouldDeregister() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId);
        client.agentClient().deregister();

        boolean found = false;

        for(ServiceHealth health : client.healthClient().getServiceHealth(serviceName)) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertFalse(found);
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

        for(Map.Entry<String, HealthCheck> check : client.agentClient().getChecks().entrySet()) {
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

    @Test
    @ConsulRunning
    public void shouldSetWarning() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().warn(note);

        verifyState("warning", client, serviceId, serviceName, note);
    }

    @Test
    @ConsulRunning
    public void shouldSetFailing() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().fail(note);

        verifyState("critical", client, serviceId, serviceName, note);
    }

    private void verifyState(String state, Consul client, String serviceId,
                             String serviceName, String note) throws UnknownHostException {
        List<ServiceHealth> nodes = client.healthClient().getServiceHealth(serviceName);
        boolean found = false;

        for(ServiceHealth health : nodes) {
            if(health.getService().getId().equals(serviceId)) {
                List<HealthCheck> checks = health.getChecks();

                found = true;

                assertEquals(serviceId, health.getService().getId());
                assertEquals(state, checks.get(0).getStatus());
                //assertEquals(note, checks.get(0).getNotes());
            }
        }

        assertTrue(found);
    }
}
