package com.orbitz.consul.cache;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

public class ConsulCacheTest extends BaseIntegrationTest {

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
        final ConsulCache<String, Value> consulCache = new ConsulCache<>(keyExtractor, null, cacheConfig, eventHandler);
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
}
