package com.orbitz.consul.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class Jackson {

    public static final ObjectMapper MAPPER = newObjectMapper();

    private static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        return mapper;
    }

    private Jackson() {}

}
