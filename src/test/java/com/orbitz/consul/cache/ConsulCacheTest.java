package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
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

        ServiceHealth health  = svHealth.getMap().get(agent.getConfig().getNodeName());
        assertEquals(serviceId, health.getService().getId());

        client.agentClient().fail(serviceId);
        Thread.sleep(100);

        health  = svHealth.getMap().get(agent.getConfig().getNodeName());
        assertNull(health);

    }

    @Test
    public void nodeCacheKvTest() throws Exception {

        Consul consul = Consul.newClient();
        KeyValueClient kvClient = consul.keyValueClient();
        String root = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i) );
        }

        ConsulCache<Value> nc = KVCache.newCache(
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

        ConsulCache<Value> nc = KVCache.newCache(
                kvClient, root, 10
        );
        nc.start();

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }

        final List<Map<String, Value>> events = new ArrayList<Map<String, Value>>();

        nc.addListener(new ConsulCache.Listener<Value>() {
            @Override
            public void notify(Map<String, Value> newValues) {
                events.add(newValues);
            }
        });

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i) );
            Thread.sleep(100);
        }

        assertEquals(5, events.size());

        for (int i = 0; i < 5; i++) {

            Map<String, Value> map = events.get(i);
            assertEquals(i +1, map.size());
            for (int j = 0; j < i; j++) {
                String keyStr = "" + j;
                String valStr = keyStr;
                assertEquals(valStr, map.get(keyStr).getValueAsString().get());
            }

        }
        kvClient.deleteKeys(root);

    }
}
