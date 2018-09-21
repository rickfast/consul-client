package com.orbitz.consul;

import com.google.common.collect.ImmutableList;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.catalog.ImmutableServiceWeights;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class AgentITest extends BaseIntegrationTest {

    private static final List<String> NO_TAGS = Collections.emptyList();
    private static final Map<String, String> NO_META = Collections.emptyMap();

    @Test
    public void shouldRetrieveAgentInformation() throws UnknownHostException {
        Agent agent = client.agentClient().getAgent();

        org.junit.Assume.assumeTrue(agent.getDebugConfig() != null);

        assertNotNull(agent);
        assertNotNull(agent.getConfig());
        assertNotNull(agent.getDebugConfig().getClientAddrs().get(0));

        // maybe we should not make any assertion on the actual value of the client address
        // as like when we run consul in a docker container we would have "0.0.0.0"
        assertThat(agent.getDebugConfig().getClientAddrs().get(0), anyOf(is("127.0.0.1"), is("0.0.0.0")));
    }

    @Test
    @Ignore
    public void shouldRegisterTtlCheck() throws UnknownHostException, InterruptedException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId, NO_TAGS, NO_META);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
                assertThat(health.getChecks().size(), is(2));
            }
        }

        assertTrue(found);
    }

    @Test
    @Ignore
    public void shouldRegisterHttpCheck() throws UnknownHostException, InterruptedException, MalformedURLException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, new URL("http://localhost:1337/health"), 1000L, serviceName, serviceId, NO_TAGS, NO_META);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
                assertThat(health.getChecks().size(), is(2));
            }
        }

        assertTrue(found);
    }

    @Test
    @Ignore
    public void shouldRegisterGrpcCheck() throws UnknownHostException, InterruptedException, MalformedURLException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        Registration registration = ImmutableRegistration.builder()
                .name(serviceName)
                .id(serviceId)
                .addChecks(ImmutableRegCheck.builder()
                    .grpc("localhost:12345")
                    .interval("10s")
                    .build())
                .build();
        client.agentClient().register(registration);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
                assertThat(health.getChecks().size(), is(2));
            }
        }

        assertTrue(found);
    }

    @Test
    @Ignore
    public void shouldRegisterMultipleChecks() throws UnknownHostException, InterruptedException, MalformedURLException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        List<Registration.RegCheck> regChecks = ImmutableList.of(
                Registration.RegCheck.args(Collections.singletonList("/usr/bin/echo \"sup\""), 10, 1, "Custom description."),
                Registration.RegCheck.http("http://localhost:8080/health", 10, 1, "Custom description."));

        client.agentClient().register(8080, regChecks, serviceName, serviceId, NO_TAGS, NO_META);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
                assertThat(health.getChecks().size(), is(3));
            }
        }

        assertTrue(found);
    }

    // This is apparently valid
    // to register a single "Check"
    // and multiple "Checks" in one call
    @Test
    @Ignore
    public void shouldRegisterMultipleChecks2() throws UnknownHostException, InterruptedException, MalformedURLException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        Registration.RegCheck single= Registration.RegCheck.args(Collections.singletonList("/usr/bin/echo \"sup\""), 10);

        List<Registration.RegCheck> regChecks = ImmutableList.of(
                Registration.RegCheck.http("http://localhost:8080/health", 10));

        Registration reg = ImmutableRegistration.builder()
                .check(single)
                .checks(regChecks)
                .address("localhost")
                .port(8080)
                .name(serviceName)
                .id(serviceId)
                .build();
        client.agentClient().register(reg);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
                assertThat(health.getChecks().size(), is(3));
            }
        }

        assertTrue(found);
    }

    @Test
    public void shouldDeregister() throws UnknownHostException, InterruptedException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId, NO_TAGS, NO_META);
        client.agentClient().deregister(serviceId);
        Synchroniser.pause(Duration.ofSeconds(1));
        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertFalse(found);
    }

    @Test
    public void shouldGetChecks() {
        String id = UUID.randomUUID().toString();
        client.agentClient().register(8080, 20L, UUID.randomUUID().toString(), id, NO_TAGS, NO_META);

        boolean found = false;

        for (Map.Entry<String, HealthCheck> check : client.agentClient().getChecks().entrySet()) {
            if (check.getValue().getCheckId().equals("service:" + id)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void shouldGetServices() {
        String id = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        List<String> tags = Collections.singletonList(UUID.randomUUID().toString());
        Map<String, String> meta = Collections.singletonMap(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        client.agentClient().register(8080, 20L, name, id, tags, meta);
        Synchroniser.pause(Duration.ofMillis(100));

        Service expectedService = ImmutableService.builder()
                .id(id)
                .service(name)
                .address("")
                .port(8080)
                .tags(tags)
                .meta(meta)
                .enableTagOverride(false)
                .weights(ImmutableServiceWeights.builder().warning(1).passing(1).build())
                .build();
        Service registeredService = null;
        for (Map.Entry<String, Service> service : client.agentClient().getServices().entrySet()) {
            if (service.getValue().getId().equals(id)) {
                registeredService = service.getValue();
            }
        }

        assertNotNull(String.format("Service \"%s\" not found", name), registeredService);
        assertEquals(expectedService, registeredService);
    }

    @Test
    public void shouldSetWarning() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId, Collections.emptyList(), Collections.emptyMap());
        client.agentClient().warn(serviceId, note);

        verifyState("warning", client, serviceId, serviceName, note);
    }

    @Test
    public void shouldSetFailing() throws UnknownHostException, NotRegisteredException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId, Collections.emptyList(), Collections.emptyMap());
        client.agentClient().fail(serviceId, note);

        verifyState("critical", client, serviceId, serviceName, note);
    }

    @Test
    public void shouldRegisterNodeScriptCheck() throws InterruptedException {
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", "/usr/bin/echo \"sup\"", 30);
        try {

            HealthCheck check = client.agentClient().getChecks().get(checkId);

            assertEquals(check.getCheckId(), checkId);
            assertEquals(check.getName(), "test-validate");
        }
        finally {
            client.agentClient().deregisterCheck(checkId);
        }
    }

    @Test
    public void shouldRegisterNodeHttpCheck() throws InterruptedException, MalformedURLException {
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", new URL("http://foo.local:1337/check"), 30);

        try {
            HealthCheck check = client.agentClient().getChecks().get(checkId);

            assertEquals(check.getCheckId(), checkId);
            assertEquals(check.getName(), "test-validate");
        }
        finally {
            client.agentClient().deregisterCheck(checkId);
        }
    }

    @Test
    public void shouldRegisterNodeTtlCheck() throws InterruptedException, MalformedURLException {
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", 30);
        try {
            HealthCheck check = client.agentClient().getChecks().get(checkId);

            assertEquals(check.getCheckId(), checkId);
            assertEquals(check.getName(), "test-validate");
        }
        finally {
            client.agentClient().deregisterCheck(checkId);
        }
    }

    @Test
    @Ignore
    public void shouldEnableMaintenanceMode() throws InterruptedException, MalformedURLException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String reason = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId, NO_TAGS, NO_META);
        client.agentClient().toggleMaintenanceMode(serviceId, true, reason);
    }


    private void verifyState(String state, Consul client, String serviceId,
                             String serviceName, String output) throws UnknownHostException {

        Map<String, HealthCheck> checks = client.agentClient().getChecks();
        HealthCheck check = checks.get("service:" + serviceId);

        assertNotNull(check);
        assertEquals(serviceId, check.getServiceId().get());
        assertEquals(serviceName, check.getServiceName().get());
        assertEquals(state, check.getStatus());

        if (output != null) {
            assertEquals(output, check.getOutput().get());
        }
    }
}
