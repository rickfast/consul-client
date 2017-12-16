package com.orbitz.consul.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.primitives.UnsignedLongs;

import java.io.IOException;

/**
 * @author sgardner
 * @since 2015.02.28
 */
public class UnsignedLongDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String sValue = jp.getValueAsString();
        return UnsignedLongs.decode(sValue);
    }
}
