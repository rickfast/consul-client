package com.orbitz.consul.cache;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class CacheConfigTest {

    @Test
    public void testDefaultBackOffDelay() {
        Assert.assertEquals(10000L, CacheConfig.get().getBackOffDelayInMs());
    }

    @Test
    public void testBackOffDelayFromProperties() {
        String property = String.format("%s.%s", CacheConfig.CONFIG_CACHE_PATH, CacheConfig.BACKOFF_DELAY);
        Properties properties = new Properties();
        properties.setProperty(property, "500");

        Config config = ConfigFactory.parseProperties(properties);
        CacheConfig cacheConfig = new CacheConfig(config);
        Assert.assertEquals(500L, cacheConfig.getBackOffDelayInMs());
    }
}
