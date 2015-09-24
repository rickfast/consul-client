package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.kv.Value;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ConsulCacheTest {


    @Test
    public void nodeCacheServicePassingTest() throws Exception {
        Consul client = Consul.newClient();
        HealthClient healthClient = client.healthClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);
        Agent agent = client.agentClient().getAgent();
        Thread.sleep(100);

        ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);

        svHealth.start();
        svHealth.awaitInitialized(3, TimeUnit.SECONDS);

        HostAndPort serviceKey = HostAndPort.fromParts(agent.getConfig().getAdvertiseAddr(), 8080);
        ServiceHealth health = svHealth.getMap().get(serviceKey);
        assertEquals(serviceId, health.getService().getId());

        client.agentClient().fail(serviceId);
        Thread.sleep(100);

        health = svHealth.getMap().get(serviceKey);
        assertNull(health);

    }

    @Test
    public void nodeCacheKvTest() throws Exception {

        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
        }

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );
        nc.start();

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }

        ImmutableMap<String, Value> map = nc.getMap();
        for (int i = 0; i < 5; i++) {
            String keyStr = "" + i;
            String valStr = keyStr;
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }

        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                kvClient.putValue(root + "/" + i, String.valueOf(i * 10));
            }
        }

        Thread.sleep(100);

        map = nc.getMap();
        for (int i = 0; i < 5; i++) {
            String keyStr = "" + i;
            String valStr = i % 2 == 0 ? "" + (i * 10) : keyStr;
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }

        kvClient.deleteKeys(root);

    }

    @Test
    public void testListeners() throws Exception {
        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );

        final List<Map<String, Value>> events = new ArrayList<Map<String, Value>>();
        nc.addListener(new ConsulCache.Listener<String, Value>() {
            @Override
            public void notify(Map<String, Value> newValues) {
                events.add(newValues);
            }
        });

        nc.start();

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
            Thread.sleep(100);
        }

        assertEquals(5, events.size());

        for (int i = 0; i < 5; i++) {

            Map<String, Value> map = events.get(i);
            assertEquals(i + 1, map.size());
            for (int j = 0; j < i; j++) {
                String keyStr = "" + j;
                String valStr = keyStr;
                assertEquals(valStr, map.get(keyStr).getValueAsString().get());
            }
        }
        kvClient.deleteKeys(root);

    }

    @Test
    public void testLateListenersGetValues() throws Exception {
        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );
        nc.start();

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }

        final List<Map<String, Value>> events = new ArrayList<Map<String, Value>>();

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
            Thread.sleep(100);
        }

        nc.addListener(new ConsulCache.Listener<String, Value>() {
            @Override
            public void notify(Map<String, Value> newValues) {
                events.add(newValues);
            }
        });

        assertEquals(1, events.size());

        Map<String, Value> map = events.get(0);
        assertEquals(5, map.size());
        for (int j = 0; j < 5; j++) {
            String keyStr = "" + j;
            String valStr = keyStr;
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }
        kvClient.deleteKeys(root);
    }

    @Test(expected = IllegalStateException.class)
    public void testLifeCycleDoubleStart() throws Exception {
        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );

        assertEquals(ConsulCache.State.latent, nc.getState());
        nc.start();
        assertEquals(ConsulCache.State.starting, nc.getState());

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }
        assertEquals(ConsulCache.State.started, nc.getState());
        nc.start();

    }

    @Test
    public void testLifeCycle() throws Exception {
        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );

        final List<Map<String, Value>> events = new ArrayList<Map<String, Value>>();
        nc.addListener(new ConsulCache.Listener<String, Value>() {
            @Override
            public void notify(Map<String, Value> newValues) {
                events.add(newValues);
            }
        });

        assertEquals(ConsulCache.State.latent, nc.getState());
        nc.start();
        assertEquals(ConsulCache.State.starting, nc.getState());

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }
        assertEquals(ConsulCache.State.started, nc.getState());


        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
            Thread.sleep(100);
        }
        assertEquals(5, events.size());
        nc.stop();
        assertEquals(ConsulCache.State.stopped, nc.getState());

        // now assert that we get no more update to the listener
        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i + "-again", String.valueOf(i));
            Thread.sleep(100);
        }

        assertEquals(5, events.size());

        kvClient.deleteKeys(root);

    }
}
