package com.orbitz.consul.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static com.orbitz.consul.util.ClientUtil.decodeBase64;

/**
 * For use with JSON fields that Consul Base 64 encodes.
 */
public class Base64EncodingDeserializer extends JsonDeserializer<String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = p.getValueAsString();
        String result = value;

        if (StringUtils.isNotEmpty(result)) {
            result = decodeBase64(result);
        }

        return result;
    }
}
