package com.orbitz.consul.cache;

/**
 * A {@link CacheDescriptor} describes an instance of a cache.
 * The cache is represented by an {@link CacheDescriptor#endpoint} and a {@link CacheDescriptor#key}.
 * For instance, a cache targeting "/v1/catalog/service/myService" will be represented by endpoint "catalog.service" and key "myService".
 */
public class CacheDescriptor {

    private final String endpoint;
    private final String key;

    public CacheDescriptor(String endpoint) {
        this(endpoint, null);
    }

    public CacheDescriptor(String endpoint, String key) {
        this.endpoint = endpoint;
        this.key = key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        if (key == null) {
            return endpoint;
        }
        return String.format("%s \"%s\"", endpoint, key);
    }
}
