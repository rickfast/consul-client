package com.orbitz.consul;

import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgentTests {

    @Test
    public void shouldRetrieveAgentInformation() throws UnknownHostException {
        Consul client = Consul.newClient();
        Agent agent = client.agentClient().getAgent();

        assertNotNull(agent);
        assertEquals("127.0.0.1", agent.getConfig().getClientAddr());
    }

    @Test
    public void shouldRegister() throws UnknownHostException, InterruptedException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId);

        Thread.sleep(100);

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void shouldRegisterHttpCheck() throws UnknownHostException, InterruptedException, MalformedURLException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, new URL("http://localhost:1337/health"), 1000L, serviceName, serviceId);

        Thread.sleep(100);

        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(serviceName).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void shouldDeregister() throws UnknownHostException, InterruptedException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 10000L, serviceName, serviceId);
        client.agentClient().deregister(serviceId);
        Thread.sleep(1000L);
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
        Consul client = Consul.newClient();
        String id = UUID.randomUUID().toString();
        client.agentClient().register(8080, 20L, UUID.randomUUID().toString(), id);

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
        Consul client = Consul.newClient();
        String id = UUID.randomUUID().toString();
        client.agentClient().register(8080, 20L, UUID.randomUUID().toString(), id);

        boolean found = false;

        for (Map.Entry<String, Service> service : client.agentClient().getServices().entrySet()) {
            if (service.getValue().getId().equals(id)) {
                found = true;
            }
            ;
        }

        assertTrue(found);
    }

    @Test
    public void shouldSetWarning() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().warn(serviceId, note);

        verifyState("warning", client, serviceId, serviceName, note);
    }

    @Test
    public void shouldSetFailing() throws UnknownHostException, NotRegisteredException {
        Consul client = Consul.newClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String note = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().fail(serviceId, note);

        verifyState("critical", client, serviceId, serviceName, note);
    }

    @Test
    public void shouldRegisterNodeScriptCheck() throws InterruptedException {
        Consul client = Consul.newClient();
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", "/usr/bin/echo \"sup\"", 30);

        HealthCheck check = client.agentClient().getChecks().get(checkId);

        assertEquals(check.getCheckId(), checkId);
        assertEquals(check.getName(), "test-validate");

        client.agentClient().deregisterCheck(checkId);
    }


    @Test
    public void shouldRegisterNodeHttpCheck() throws InterruptedException, MalformedURLException {
        Consul client = Consul.newClient();
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", new URL("http://foo.local:1337/check"), 30);

        HealthCheck check = client.agentClient().getChecks().get(checkId);

        assertEquals(check.getCheckId(), checkId);
        assertEquals(check.getName(), "test-validate");

        client.agentClient().deregisterCheck(checkId);
    }

    @Test
    public void shouldRegisterNodeTtlCheck() throws InterruptedException, MalformedURLException {
        Consul client = Consul.newClient();
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, "test-validate", 30);

        HealthCheck check = client.agentClient().getChecks().get(checkId);

        assertEquals(check.getCheckId(), checkId);
        assertEquals(check.getName(), "test-validate");

        client.agentClient().deregisterCheck(checkId);
    }


    private void verifyState(String state, Consul client, String serviceId,
                             String serviceName, String note) throws UnknownHostException {
        List<ServiceHealth> nodes = client.healthClient().getAllServiceInstances(serviceName).getResponse();
        boolean found = false;

        for (ServiceHealth health : nodes) {
            if (health.getService().getId().equals(serviceId)) {
                List<HealthCheck> checks = health.getChecks();

                found = true;

                assertEquals(serviceId, health.getService().getId());
                assertEquals(state, checks.get(0).getStatus());
                if (note != null) {
                    assertEquals(note, checks.get(0).getNotes().get());
                }
            }
        }

        assertTrue(found);
    }
}
