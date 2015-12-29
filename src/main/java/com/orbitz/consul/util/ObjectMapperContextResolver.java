package com.orbitz.consul.util;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;
    public ObjectMapperContextResolver(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return objectMapper;
    }

}
