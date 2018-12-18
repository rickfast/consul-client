package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.ImmutableValue;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(JUnitParamsRunner.class)
public class ConsulCacheTest {

    /**
     * Test that if Consul for some reason returns a duplicate service or keyvalue entry
     * that we recover gracefully by taking the first value, ignoring duplicates, and warning
     * user of the condition
     */
    @Test
    public void testDuplicateServicesDontCauseFailure() {
        final Function<Value, String> keyExtractor = input -> "SAME_KEY";
        final List<Value> response = Arrays.asList(mock(Value.class), mock(Value.class));
        CacheConfig cacheConfig = mock(CacheConfig.class);
        ClientEventHandler eventHandler = mock(ClientEventHandler.class);

        final StubCallbackConsumer callbackConsumer = new StubCallbackConsumer(Collections.emptyList());

        final ConsulCache<String, Value> consulCache = new ConsulCache<>(keyExtractor, callbackConsumer, cacheConfig, eventHandler, new CacheDescriptor(""));
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
                .addTag("someTag")
                .token("186596")
                .near("156892")
                .build();

        QueryOptions expectedOptions = ImmutableQueryOptions.builder()
                .index(index)
                .wait("10s")
                .consistencyMode(ConsistencyMode.STALE)
                .addTag("someTag")
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
    @Parameters(method = "getRetryDurationSamples")
    @TestCaseName("min Delay: {0}, max Delay: {1}")
    public void testRetryDuration(Duration minDelay, Duration maxDelay) {
        CacheConfig cacheConfig = CacheConfig.builder().withBackOffDelay(minDelay, maxDelay).build();
        for (int i=0; i < 1000; i++) {
            long retryDurationMs = ConsulCache.computeBackOffDelayMs(cacheConfig);
            Assert.assertThat(
                    String.format("Retry duration expected between %s and %s but got %d ms", minDelay, maxDelay, retryDurationMs),
                    retryDurationMs,
                    is(allOf(new GreaterOrEqual<>(minDelay.toMillis()), new LessOrEqual<>(maxDelay.toMillis()))));
        }
    }

    public Object getRetryDurationSamples() {
        return new Object[]{
                // Same duration
                new Object[]{Duration.ZERO, Duration.ZERO},
                new Object[]{Duration.ofSeconds(10), Duration.ofSeconds(10)},
                // Different durations
                new Object[]{Duration.ofSeconds(10), Duration.ofSeconds(11)},
                new Object[]{Duration.ofMillis(10), Duration.ofMinutes(1)},
        };
    }

    @Test
    public void testListenerIsCalled() {
        final Function<Value, String> keyExtractor = Value::getKey;
        final CacheConfig cacheConfig = CacheConfig.builder().build();
        ClientEventHandler eventHandler = mock(ClientEventHandler.class);

        final String key = "foo";
        final ImmutableValue value = ImmutableValue.builder()
                .createIndex(1)
                .modifyIndex(2)
                .lockIndex(2)
                .key(key)
                .flags(0)
                .build();
        final List<Value> result = Collections.singletonList(value);
        final StubCallbackConsumer callbackConsumer = new StubCallbackConsumer(
                result);

        final ConsulCache<String, Value> cache = new ConsulCache<>(keyExtractor, callbackConsumer, cacheConfig,
                eventHandler, new CacheDescriptor(""));
        try {
            final StubListener listener = new StubListener();

            cache.addListener(listener);
            cache.start();

            assertEquals(1, listener.getCallCount());
            assertEquals(1, callbackConsumer.getCallCount());

            final Map<String, Value> lastValues = listener.getLastValues();
            assertNotNull(lastValues);
            assertEquals(result.size(), lastValues.size());
            assertTrue(lastValues.containsKey(key));
            assertEquals(value, lastValues.get(key));
        } finally {
            cache.stop();
        }
    }

    @Test
    public void testListenerThrowingExceptionIsIsolated() throws InterruptedException {
        final Function<Value, String> keyExtractor = Value::getKey;
        final CacheConfig cacheConfig = CacheConfig.builder()
                .withMinDelayBetweenRequests(Duration.ofSeconds(10))
                .build();
        ClientEventHandler eventHandler = mock(ClientEventHandler.class);

        final String key = "foo";
        final ImmutableValue value = ImmutableValue.builder()
                .createIndex(1)
                .modifyIndex(2)
                .lockIndex(2)
                .key(key)
                .flags(0)
                .build();
        final List<Value> result = Collections.singletonList(value);
        try (final AsyncCallbackConsumer callbackConsumer = new AsyncCallbackConsumer(result)) {
            try (final ConsulCache<String, Value> cache = new ConsulCache<>(keyExtractor, callbackConsumer, cacheConfig,
                        eventHandler, new CacheDescriptor(""))) {

                final StubListener goodListener = new StubListener();
                final AlwaysThrowsListener badListener1 = new AlwaysThrowsListener();

                cache.addListener(badListener1);
                cache.addListener(goodListener);
                cache.start();

                final StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                // Make sure that we wait some duration of time for asynchronous things to occur
                while (stopWatch.getTime() < 5000 && goodListener.getCallCount() < 1) {
                    Thread.sleep(50);
                }

                assertEquals(1, goodListener.getCallCount());
                assertEquals(1, callbackConsumer.getCallCount());

                final Map<String, Value> lastValues = goodListener.getLastValues();
                assertNotNull(lastValues);
                assertEquals(result.size(), lastValues.size());
                assertTrue(lastValues.containsKey(key));
                assertEquals(value, lastValues.get(key));
            }
        }
    }

    @Test
    public void testExceptionReceivedFromListenerWhenAlreadyStarted() {
        final Function<Value, String> keyExtractor = Value::getKey;
        final CacheConfig cacheConfig = CacheConfig.builder()
                .withMinDelayBetweenRequests(Duration.ofSeconds(10))
                .build();
        final ClientEventHandler eventHandler = mock(ClientEventHandler.class);

        final String key = "foo";
        final ImmutableValue value = ImmutableValue.builder()
                .createIndex(1)
                .modifyIndex(2)
                .lockIndex(2)
                .key(key)
                .flags(0)
                .build();
        final List<Value> result = Collections.singletonList(value);
        final StubCallbackConsumer callbackConsumer = new StubCallbackConsumer(
                result);

        try (final ConsulCache<String, Value> cache = new ConsulCache<>(keyExtractor, callbackConsumer, cacheConfig,
                eventHandler, new CacheDescriptor(""))) {

            final AlwaysThrowsListener badListener = new AlwaysThrowsListener();

            cache.start();

            // Adding listener after cache is already started
            final boolean isAdded = cache.addListener(badListener);
            assertTrue(isAdded);

            final StubListener goodListener = new StubListener();
            cache.addListener(goodListener);

            assertEquals(1, goodListener.getCallCount());
        }
    }

}
