package ru.hh.consul.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class Jackson {

    public static final ObjectMapper MAPPER = newObjectMapper();

    private static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    private Jackson() {}

}
