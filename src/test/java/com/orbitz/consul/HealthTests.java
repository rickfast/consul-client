package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static com.orbitz.consul.Consul.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HealthTests extends BaseIntegrationTest {

    @Test
    @Ignore
    public void shouldFetchPassingNode() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        Consul client2 = builder().withHostAndPort(HostAndPort.fromParts("localhost", 8500)).build();
        String serviceId2 = UUID.randomUUID().toString();

        client2.agentClient().register(8080, 20L, serviceName, serviceId2);
        client2.agentClient().fail(serviceId2);

        ConsulResponse<List<ServiceHealth>> response = client2.healthClient().getHealthyServiceInstances(serviceName);
        assertHealth(serviceId, response);

        client.agentClient().deregister(serviceId);
        client.agentClient().deregister(serviceId2);
    }

    @Test
    public void shouldFetchNode() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllServiceInstances(serviceName);
        assertHealth(serviceId, response);

        client.agentClient().deregister(serviceId);
    }

    @Test
    public void shouldFetchNodeDatacenter() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllServiceInstances(serviceName,
                ImmutableQueryOptions.builder().datacenter("dc1").build());
        assertHealth(serviceId, response);
        client.agentClient().deregister(serviceId);
    }

    @Test
    public void shouldFetchNodeBlock() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        ConsulResponse<List<ServiceHealth>> response = client.healthClient().getAllServiceInstances(serviceName,
                QueryOptions.blockSeconds(2, new BigInteger("0")).datacenter("dc1").build());
        assertHealth(serviceId, response);
        client.agentClient().deregister(serviceId);
    }

    @Test
    public void shouldFetchChecksForServiceBlock() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        Registration.RegCheck check = Registration.RegCheck.ttl(5);
        Registration registration = ImmutableRegistration
                .builder()
                .check(check)
                .port(8080)
                .name(serviceName)
                .id(serviceId)
                .build();

        client.agentClient().register(registration);
        client.agentClient().pass(serviceId);

        boolean found = false;
        ConsulResponse<List<HealthCheck>> response = client.healthClient().getServiceChecks(serviceName,
                QueryOptions.blockSeconds(20, new BigInteger("0")).datacenter("dc1").build());

        List<HealthCheck> checks = response.getResponse();
        assertEquals(1, checks.size());
        for(HealthCheck ch : checks) {
            if(ch.getServiceId().isPresent() && ch.getServiceId().get().equals(serviceId)) {
                found = true;
            }
        }
        assertTrue(found);
        client.agentClient().deregister(serviceId);
    }

    @Test
    public void shouldFetchByState() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().warn(serviceId);

        boolean found = false;
        ConsulResponse<List<HealthCheck>> response = client.healthClient().getChecksByState(State.WARN);

        for(HealthCheck healthCheck : response.getResponse()) {
            if(healthCheck.getServiceId().isPresent() && healthCheck.getServiceId().get().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
        client.agentClient().deregister(serviceId);
    }

    private void assertHealth(String serviceId, ConsulResponse<List<ServiceHealth>> response) {
        boolean found = false;
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
