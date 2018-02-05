package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.ImmutableValue;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.util.Synchroniser;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class KVCacheTest extends BaseIntegrationTest {

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
            String keyStr = String.format("%s/%s", root, i);
            String valStr = String.valueOf(i);
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }

        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                kvClient.putValue(root + "/" + i, String.valueOf(i * 10));
            }
        }

        Synchroniser.pause(Duration.ofMillis(100));

        map = nc.getMap();
        for (int i = 0; i < 5; i++) {
            String keyStr = String.format("%s/%s", root, i);
            String valStr = i % 2 == 0 ? "" + (i * 10) : String.valueOf(i);
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }

        kvClient.deleteKeys(root);

    }

    @Test
    public void testListeners() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
        String root = UUID.randomUUID().toString();
        final List<Map<String, Value>> events = new ArrayList<>();

        try (KVCache nc = KVCache.newCache(kvClient, root, 10)) {
            nc.addListener(events::add);
            nc.start();

            if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
                fail("cache initialization failed");
            }

            for (int keyIdx = 0; keyIdx < 5; keyIdx++) {
                kvClient.putValue(String.format("%s/%s", root, keyIdx), String.valueOf(keyIdx));
                Synchroniser.pause(Duration.ofMillis(100));
            }
        }

        assertEquals(6, events.size());
        for (int eventIdx = 1; eventIdx < 6; eventIdx++) {
            Map<String, Value> map = events.get(eventIdx);
            assertEquals(eventIdx, map.size());

            for (int keyIdx = 0; keyIdx < eventIdx; keyIdx++) {
                Optional<String> value = map
                        .get(String.format("%s/%s", root, keyIdx))
                        .getValueAsString();

                if (!value.isPresent()) {
                    fail(String.format("Missing value for event %s and key %s", eventIdx, keyIdx));
                }
                assertEquals(String.valueOf(keyIdx), value.get());
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

        final List<Map<String, Value>> events = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
            Synchroniser.pause(Duration.ofMillis(100));
        }

        nc.addListener(events::add);
        assertEquals(1, events.size());

        Map<String, Value> map = events.get(0);
        assertEquals(5, map.size());
        for (int j = 0; j < 5; j++) {
            String keyStr = String.format("%s/%s", root, j);
            String valStr = String.valueOf(j);
            assertEquals(valStr, map.get(keyStr).getValueAsString().get());
        }
        kvClient.deleteKeys(root);
    }

    @Test
    public void testListenersNonExistingKeys() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(kvClient, root, 10);
        final List<Map<String, Value>> events = new ArrayList<>();
        nc.addListener(events::add);
        nc.start();

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }

        Synchroniser.pause(Duration.ofMillis(100));

        assertEquals(1, events.size());
        Map<String, Value> map = events.get(0);
        assertEquals(0, map.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testLifeCycleDoubleStart() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
        String root = UUID.randomUUID().toString();

        KVCache nc = KVCache.newCache(kvClient, root, 10);
        assertEquals(ConsulCache.State.latent, nc.getState());
        nc.start();
        assertThat(nc.getState(), anyOf(is(ConsulCache.State.starting), is(ConsulCache.State.started)));

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
        final List<Map<String, Value>> events = new ArrayList<>();

        KVCache nc = KVCache.newCache(kvClient, root, 10);
        nc.addListener(events::add);
        assertEquals(ConsulCache.State.latent, nc.getState());

        nc.start();
        assertThat(nc.getState(), anyOf(is(ConsulCache.State.starting), is(ConsulCache.State.started)));

        if (!nc.awaitInitialized(1, TimeUnit.SECONDS)) {
            fail("cache initialization failed");
        }
        assertEquals(ConsulCache.State.started, nc.getState());


        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i, String.valueOf(i));
            Synchroniser.pause(Duration.ofMillis(100));
        }
        assertEquals(6, events.size());

        nc.stop();
        assertEquals(ConsulCache.State.stopped, nc.getState());

        // now assert that we get no more update to the listener
        for (int i = 0; i < 5; i++) {
            kvClient.putValue(root + "/" + i + "-again", String.valueOf(i));
            Synchroniser.pause(Duration.ofMillis(100));
        }

        assertEquals(6, events.size());

        kvClient.deleteKeys(root);

    }

    @Test
    public void ensureCacheInitialization() throws InterruptedException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        final CountDownLatch completed = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        try (KVCache cache = KVCache.newCache(keyValueClient, key, (int)Duration.ofSeconds(1).getSeconds())) {
            cache.addListener(values -> {
                success.set(isValueEqualsTo(values, value));
                completed.countDown();
            });

            cache.start();
            completed.await(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            keyValueClient.deleteKey(key);
        }

        assertTrue(success.get());
    }

    @Test
    @Parameters(method = "getBlockingQueriesDuration")
    @TestCaseName("queries of {0} seconds")
    public void checkUpdateNotifications(int queryDurationSec) throws InterruptedException {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("kvcache-itest-%d").build()
        );

        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        String newValue = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        final CountDownLatch completed = new CountDownLatch(2);
        final AtomicBoolean success = new AtomicBoolean(false);

        try (KVCache cache = KVCache.newCache(keyValueClient, key, queryDurationSec)) {
            cache.addListener(values -> {
                success.set(isValueEqualsTo(values, newValue));
                completed.countDown();
            });

            cache.start();
            executor.schedule(() -> keyValueClient.putValue(key, newValue), 3, TimeUnit.SECONDS);
            completed.await(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            keyValueClient.deleteKey(key);
            executor.shutdownNow();
        }

        assertTrue(success.get());
    }

    public Object getBlockingQueriesDuration() {
        return new Object[]{
                new Object[]{1},
                new Object[]{10}

        };
    }

    private boolean isValueEqualsTo(Map<String, Value> values, String expectedValue) {
        Value value = values.get("");
        if (value == null) {
            return false;
        }
        Optional<String> valueAsString = value.getValueAsString();
        return valueAsString.isPresent() && expectedValue.equals(valueAsString.get());
    }

    @Test
    @Parameters(method = "getKeyValueTestValues")
    @TestCaseName("wanted {0}, found {1}")
    public void checkKeyExtractor(String rootPath, String input, String expected) {
        //Called in the constructor of the cache, must be use in the test as it may modify rootPath value.
        final String keyPath = KVCache.prepareRootPath(rootPath);

        Function<Value, String> keyExtractor = KVCache.getKeyExtractorFunction(keyPath);
        Assert.assertEquals(expected, keyExtractor.apply(createValue(input)));
    }

    public Object getKeyValueTestValues() {
        return new Object[]{
                new Object[]{"", "a/b", "a/b"},
                new Object[]{"/", "a/b", "a/b"},
                new Object[]{"a", "a/b", "a/b"},
                new Object[]{"a/", "a/b", "b"},
                new Object[]{"a/b", "a/b", ""},
                new Object[]{"a/b", "a/b/", "b/"},
                new Object[]{"a/b", "a/b/c", "b/c"},
                new Object[]{"a/b", "a/bc", "bc"},
                new Object[]{"a/b/", "a/b/", ""},
                new Object[]{"a/b/", "a/b/c", "c"},
                new Object[]{"/a/b", "a/b", ""}
        };
    }

    private Value createValue(final String key) {
        return ImmutableValue.builder()
                .createIndex(1234567890)
                .modifyIndex(1234567890)
                .lockIndex(1234567890)
                .flags(1234567890)
                .key(key)
                .value(Optional.empty())
                .build();
    }
}