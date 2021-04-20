package ru.hh.consul.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.util.Base64;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * For use with JSON fields that Consul Base 64 encodes.
 */
public class Base64EncodingDeserializer extends JsonDeserializer<Optional<String>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();

        if (StringUtils.isNotEmpty(value)) {
            return Optional.of(new String(Base64.getDecoder().decode(value)));
        }
        return Optional.empty();
    }
}
