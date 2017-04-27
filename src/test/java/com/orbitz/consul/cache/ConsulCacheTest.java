package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ConsulCacheTest extends BaseIntegrationTest {

    @Test
    public void nodeCacheHealthCheckTest() throws Exception {
        HealthClient healthClient = client.healthClient();
        String checkName = UUID.randomUUID().toString();
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, checkName, 20L);
        try {
            client.agentClient().passCheck(checkId);
            Thread.sleep(100);

            HealthCheckCache hCheck = HealthCheckCache.newCache(healthClient, State.PASS);

            hCheck.start();
            hCheck.awaitInitialized(3, TimeUnit.SECONDS);

            HealthCheck check = hCheck.getMap().get(checkId);
            assertEquals(checkId, check.getCheckId());

            client.agentClient().failCheck(checkId);
            Thread.sleep(100);

            check = hCheck.getMap().get(checkId);
            assertNull(check);
        }
        finally {
            client.agentClient().deregister(checkId);
        }
    }

    @Test
    public void nodeCacheServicePassingTest() throws Exception {
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

        ServiceHealthKey serviceKey = ServiceHealthKey.of(serviceId, agent.getConfig().getAdvertiseAddr(), 8080);
        ServiceHealth health = svHealth.getMap().get(serviceKey);
        assertEquals(serviceId, health.getService().getId());

        client.agentClient().fail(serviceId);
        Thread.sleep(100);

        health = svHealth.getMap().get(serviceKey);
        assertNull(health);

    }

    @Test
    public void testServicesAreUniqueByID() throws Exception {
        HealthClient healthClient = client.healthClient();
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String serviceId2 = UUID.randomUUID().toString();

        client.agentClient().register(8080, 20L, serviceName, serviceId);
        client.agentClient().pass(serviceId);

        client.agentClient().register(8080, 20L, serviceName, serviceId2);
        client.agentClient().pass(serviceId2);

        ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);

        svHealth.start();
        svHealth.awaitInitialized(3, TimeUnit.SECONDS);

        Agent agent = client.agentClient().getAgent();
        Thread.sleep(100);

        ServiceHealthKey serviceKey1 = ServiceHealthKey.of(serviceId, agent.getConfig().getAdvertiseAddr(), 8080);
        ServiceHealthKey serviceKey2 = ServiceHealthKey.of(serviceId2, agent.getConfig().getAdvertiseAddr(), 8080);

        ImmutableMap<ServiceHealthKey, ServiceHealth> healthMap = svHealth.getMap();
        assertEquals(healthMap.size(), 2);
        ServiceHealth health =healthMap.get(serviceKey1);
        ServiceHealth health2 = healthMap.get(serviceKey2);

        assertEquals(serviceId, health.getService().getId());
        assertEquals(serviceId2, health2.getService().getId());
    }

    @Test
    public void nodeCacheKvTest() throws Exception {

        KeyValueClient kvClient = client.keyValueClient();
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
        KeyValueClient kvClient = client.keyValueClient();
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

        assertEquals(6, events.size());

        for (int i = -1; i < 5; i++) {

            Map<String, Value> map = events.get(i+1);
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
        KeyValueClient kvClient = client.keyValueClient();
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

    @Test
    public void testListenersNonExistingKeys() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
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

        Thread.sleep(100);

        assertEquals(1, events.size());
        Map<String, Value> map = events.get(0);
        assertEquals(0, map.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testLifeCycleDoubleStart() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(
                kvClient, root, 10
        );

        assertEquals(ConsulCache.State.latent, nc.getState());
        nc.start();
        assertEquals(ConsulCache.State.starting, nc.getState());

        if (!nc.awaitInitialized(10, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }
        assertEquals(ConsulCache.State.started, nc.getState());
        nc.start();

    }

    @Test
    public void testLifeCycle() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
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

    /**
     * Test that if Consul for some reason returns a duplicate service or keyvalue entry
     * that we recover gracefully by taking the first value, ignoring duplicates, and warning
     * user of the condition
     */
    @Test
    public void testDuplicateServicesDontCauseFailure() {
        final Function<Value, String> keyExtractor = new Function<Value, String>() {
            @Override
            public String apply(final Value input) {
                return "SAME_KEY";
            }
        };
        final List<Value> response = Arrays.asList(Mockito.mock(Value.class), Mockito.mock(Value.class));
        final ConsulCache<String, Value> consulCache = new ConsulCache<>(keyExtractor, null);
        final ConsulResponse<List<Value>> consulResponse = new ConsulResponse<>(response, 0, false, BigInteger.ONE);
        final ImmutableMap<String, Value> map = consulCache.convertToMap(consulResponse);
        assertNotNull(map);
        // Second copy has been weeded out
        assertEquals(1, map.size());
    }

    @Test
    public void testWatchParamsWithNoAdditionalOptions() {
        BigInteger index = new BigInteger("12");
        QueryOptions expectedOptions = ImmutableQueryOptions.builder()
                .index(index)
                .wait("10s")
                .build();
        QueryOptions actualOptions = ConsulCache.watchParams(index, 10, QueryOptions.BLANK);
        assertEquals(expectedOptions, actualOptions);
    }

    @Test
    public void testWatchParamsWithAdditionalOptions() {
        BigInteger index = new BigInteger("12");
        QueryOptions additionalOptions = ImmutableQueryOptions.builder()
                .consistencyMode(ConsistencyMode.STALE)
                .token("186596")
                .near("156892")
                .build();

        QueryOptions expectedOptions = ImmutableQueryOptions.builder()
                .index(index)
                .wait("10s")
                .consistencyMode(ConsistencyMode.STALE)
                .token("186596")
                .near("156892")
                .build();

        QueryOptions actualOptions = ConsulCache.watchParams(index, 10, additionalOptions);
        assertEquals(expectedOptions, actualOptions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWatchParamsWithAdditionalIndexAndWaitingThrows() {
        BigInteger index = new BigInteger("12");
        QueryOptions additionalOptions = ImmutableQueryOptions.builder()
                .index(index)
                .wait("10s")
                .build();
        ConsulCache.watchParams(index, 10, additionalOptions);
    }

    @Test
    public void testDefaultBackOffDelay() {
        Properties properties = new Properties();
        Assert.assertEquals(10000L, ConsulCache.getBackOffDelayInMs(properties));
    }

    @Test
    public void testBackOffDelayFromProperties() {
        Properties properties = new Properties();
        properties.setProperty(ConsulCache.BACKOFF_DELAY_PROPERTY, "500");
        Assert.assertEquals(500L, ConsulCache.getBackOffDelayInMs(properties));
    }

    @Test
    public void testBackOffDelayDoesNotThrow() {
        Properties properties = new Properties();
        properties.setProperty(ConsulCache.BACKOFF_DELAY_PROPERTY, "unparseableLong");
        Assert.assertEquals(10000L, ConsulCache.getBackOffDelayInMs(properties));
    }
}
