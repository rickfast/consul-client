package com.orbitz.consul;

import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptionsBuilder;
import com.orbitz.consul.option.QueryOptionsBuilder;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HealthTests {
    @Test
    public void shouldFetchPassingNode() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        Consul client2 = Consul.newClient();
        String serviceId2 = UUID.randomUUID().toString();

        client2.agentClient().register(8080, 20L, serviceName, serviceId2);
        client2.agentClient().fail(serviceId2);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client2.healthClient().getHealthyNodes(serviceName);
        assertHealth(serviceId, found, response);
    }

    @Test
    public void shouldFetchNode() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllNodes(serviceName);
        assertHealth(serviceId, found, response);
    }

    @Test
    public void shouldFetchNodeDatacenter() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllNodes(serviceName,
                CatalogOptionsBuilder.builder().datacenter("dc1").build());
        assertHealth(serviceId, found, response);
    }

    @Test
    public void shouldFetchNodeBlock() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);


        boolean found = false;
        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllNodes(serviceName,
                CatalogOptionsBuilder.builder().datacenter("dc1").build(),
                QueryOptionsBuilder.builder().blockSeconds(2, 0).build());
        assertHealth(serviceId, found, response);
    }

    @Test
    public void shouldFetchChecksForServiceBlock() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        Registration.Check check = new Registration.Check();
        check.setTtl(String.format("%ss", 5));
        Registration registration = new Registration();
        registration.setCheck(check);
        registration.setPort(8080);
        registration.setName(serviceName);
        registration.setId(serviceId);

        client.agentClient().register(registration);//8080, check, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<HealthCheck>> response = client.healthClient().getServiceChecks(serviceName,
                CatalogOptionsBuilder.builder().datacenter("dc1").build(),
                QueryOptionsBuilder.builder().blockSeconds(20, 0).build());

        List<HealthCheck> checks = response.getResponse();
        assertEquals(1, checks.size());
        for(HealthCheck ch : checks) {
            if(ch.getServiceId().equals(serviceId)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void shouldFetchByState() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().warn(serviceId);

        boolean found = false;
        ConsulResponse<List<HealthCheck>> response = client.healthClient().getChecksByState(State.WARN);

        for(HealthCheck healthCheck : response.getResponse()) {
            if(healthCheck.getServiceId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    private void assertHealth(String serviceId, boolean found, ConsulResponse<List<ServiceHealth>> response) {
        List<ServiceHealth> nodes = response.getResponse();

        assertEquals(1, nodes.size());

        for(ServiceHealth health : nodes) {
            if(health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }
}
