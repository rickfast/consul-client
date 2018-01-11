package com.orbitz.consul.config;

import com.typesafe.config.Config;

public class ConsulConfig {

    private CacheConfig cacheConfig;

    ConsulConfig(Config config) {
        this.cacheConfig = new CacheConfig(config);
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

}
