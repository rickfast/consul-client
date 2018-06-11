package com.orbitz.consul.cache;

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
