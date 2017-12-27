package com.orbitz.consul.cache;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CacheConfigTest {

    @Test
    public void testDefaultBackOffDelay() {
        assertEquals(10000L, CacheConfig.get().getBackOffDelay().toMillis());
    }

    @Test
    public void testBackOffDelayFromProperties() {
        String property = String.format("%s.%s", CacheConfig.CONFIG_CACHE_PATH, CacheConfig.BACKOFF_DELAY);
        Properties properties = new Properties();
        properties.setProperty(property, "500");

        Config config = ConfigFactory.parseProperties(properties);
        CacheConfig cacheConfig = new CacheConfig(config);
        assertEquals(500L, cacheConfig.getBackOffDelay().toMillis());
    }

    @Test
    public void testDefaultWatchDuration() {
        assertEquals(10000L, CacheConfig.get().getWatchDuration().toMillis());
    }

    @Test
    public void testMaxWatchDuration() {
        assertEquals(Duration.ofMinutes(10), getWatchDuration("10 minutes"));
    }

    @Test
    public void testMinWatchDuration() {
        assertEquals(Duration.ofSeconds(0), getWatchDuration("0 second"));
    }

    @Test(expected = RuntimeException.class)
    public void testNegativeWatchDuration() {
        getWatchDuration("-1 second");
    }

    @Test(expected = RuntimeException.class)
    public void testTooLongWatchDuration() {
        getWatchDuration("11 minutes");
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidWatchDuration() {
        getWatchDuration("invalid duration in second");
    }

    private Duration getWatchDuration(String duration) {
        String config = String.format("%s.%s: %s", CacheConfig.CONFIG_CACHE_PATH, CacheConfig.WATCH_DURATION, duration);
        CacheConfig cacheConfig = new CacheConfig(ConfigFactory.parseString(config));
        return cacheConfig.getWatchDuration();
    }
}
