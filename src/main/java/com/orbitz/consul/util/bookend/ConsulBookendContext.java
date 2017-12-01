package com.orbitz.consul.util.bookend;

import java.util.Optional;

import java.util.HashMap;
import java.util.Map;

public class ConsulBookendContext {

    private Map<String, Object> data;

    ConsulBookendContext() {

    }

    public void put(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }

        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> klazz) {
        return Optional.ofNullable((T) data.get(key));
    }
}
