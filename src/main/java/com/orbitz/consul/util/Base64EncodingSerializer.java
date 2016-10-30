package com.orbitz.consul.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

import java.io.IOException;

public class Base64EncodingSerializer extends JsonSerializer<Optional<String>> {

    @Override
    public void serialize(Optional<String> string, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        if (string.isPresent()) {
            jsonGenerator.writeString(BaseEncoding.base64().encode(string.get().getBytes()));
        } else {
            jsonGenerator.writeNull();
        }
    }
}
