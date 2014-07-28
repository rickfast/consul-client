package com.orbitz.consul;

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
        client2.agentClient().fail();

        boolean found = false;
        List<ServiceHealth> nodes = client2.healthClient().getHealthyNodes(serviceName);

        System.out.println(nodes.size());
        assertEquals(1, nodes.size());

        for(ServiceHealth health : nodes) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }
}
