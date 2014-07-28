package com.orbitz.consul;

import com.orbitz.consul.model.health.Check;
import com.orbitz.consul.model.health.ServiceHealth;
import org.junit.Rule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HealthTests {

    @Rule
    public ConsulRule consulRule = new ConsulRule();

    @Test
    @ConsulRunning
    public void shouldFetchPassingNode() throws UnknownHostException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass();

        Consul client2 = Consul.newClient();
        String serviceId2 = UUID.randomUUID().toString();

        client2.agentClient().register(8080, 20L, serviceName, serviceId2);

        boolean found = false;
        List<ServiceHealth> nodes = client2.healthClient().getHealthyNodes(serviceName);

        assertEquals(1, nodes.size());

        for(ServiceHealth health : nodes) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
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
                List<Check> checks = health.getChecks();

                found = true;

                assertEquals(serviceId, health.getService().getId());
                assertEquals(state, checks.get(0).getStatus());
                assertEquals(note, checks.get(0).getNotes());
            }
        }

        assertTrue(found);
    }
}
