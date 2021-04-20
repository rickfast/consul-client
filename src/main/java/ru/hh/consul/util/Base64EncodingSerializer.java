package ru.hh.consul.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class Base64EncodingSerializer extends JsonSerializer<Optional<String>> {

    @Override
    public void serialize(Optional<String> string, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (string.isPresent()) {
            jsonGenerator.writeString(Base64.getEncoder().encodeToString(string.get().getBytes()));
        } else {
            jsonGenerator.writeNull();
        }
    }
}
