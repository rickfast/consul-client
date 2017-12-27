package com.orbitz.consul.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.ImmutableValue;
import com.orbitz.consul.model.kv.Value;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnitParamsRunner.class)
public class KVCacheTest extends BaseIntegrationTest {

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
                .value(Optional.<String>empty())
                .build();
    }
}