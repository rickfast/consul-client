package com.orbitz.consul.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static com.orbitz.consul.util.ClientUtil.decodeBase64;

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
            return Optional.of(decodeBase64(value));
        }
        return Optional.absent();
    }
}
