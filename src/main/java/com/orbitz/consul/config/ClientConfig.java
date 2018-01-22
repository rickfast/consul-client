package com.orbitz.consul.config;

import com.google.common.base.Preconditions;

public class ClientConfig {

    private CacheConfig cacheConfig;

    public ClientConfig() {
        this(CacheConfig.builder().build());
    }

    public ClientConfig(CacheConfig cacheConfig) {
        this.cacheConfig = Preconditions.checkNotNull(cacheConfig, "Cache configuration is mandatory");
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }
}
