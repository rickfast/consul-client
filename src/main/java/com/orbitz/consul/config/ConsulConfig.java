package com.orbitz.consul.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConsulConfig {

    private CacheConfig cacheConfig;

    public ConsulConfig() {
        this(ConfigFactory.load());
    }

    public ConsulConfig(Config config) {
        Preconditions.checkNotNull(config);
        this.cacheConfig = new CacheConfig(config);
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

}
