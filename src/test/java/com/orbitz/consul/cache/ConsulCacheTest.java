package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        final ConsulCache<String, Value> consulCache = new ConsulCache<>(keyExtractor, null, cacheConfig, eventHandler, new CacheDescriptor(""));
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
}
