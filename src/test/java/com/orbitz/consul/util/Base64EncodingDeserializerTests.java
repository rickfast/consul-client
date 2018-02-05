package com.orbitz.consul.util;

import com.google.common.io.BaseEncoding;
import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.model.event.ImmutableEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Base64EncodingDeserializerTests {

    @Test
    public void shouldDeserialize() throws IOException {
        String value = RandomStringUtils.randomAlphabetic(12);
        Event event = ImmutableEvent.builder()
                .id("1")
                .lTime(1L)
                .name("name")
                .version(1)
                .payload(BaseEncoding.base64().encode(value.getBytes()))
                .build();

        String serializedEvent = Jackson.MAPPER.writeValueAsString(event);
        Event deserializedEvent = Jackson.MAPPER.readValue(serializedEvent, Event.class);

        assertEquals(value, deserializedEvent.getPayload().get());
    }
}
