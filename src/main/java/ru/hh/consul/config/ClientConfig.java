package ru.hh.consul.config;

import java.util.Objects;

public class ClientConfig {

    private final CacheConfig cacheConfig;

    public ClientConfig() {
        this(CacheConfig.builder().build());
    }

    public ClientConfig(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig, "Cache configuration is mandatory");
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }
}
