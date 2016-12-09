package com.orbitz.consul.bookend;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

public class ConsulBookendContext {

    private Map<String, Object> data;

    ConsulBookendContext() {
        data = new HashMap<>();
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> klazz) {
        return Optional.fromNullable((T) data.get(key));
    }
}
