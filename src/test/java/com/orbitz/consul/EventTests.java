package com.orbitz.consul;

import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.util.Synchroniser;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTests extends BaseIntegrationTest {

    @Test
    public void shouldFire() throws InterruptedException {
        EventClient eventClient = client.eventClient();

        String name = RandomStringUtils.random(10, true, true);
        Event fired = eventClient.fireEvent(name);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for(Event event : eventClient.listEvents().getEvents()) {
            if (event.getName().equals(name) && event.getId().equals(fired.getId())) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void shouldFireWithPayload() throws InterruptedException {
        EventClient eventClient = client.eventClient();

        String payload = RandomStringUtils.randomAlphabetic(20);
        String name = RandomStringUtils.randomAlphabetic(10);
        Event fired = eventClient.fireEvent(name, payload);

        Synchroniser.pause(Duration.ofMillis(100));

        boolean found = false;

        for(Event event : eventClient.listEvents().getEvents()) {
            if (event.getName().equals(name) && event.getId().equals(fired.getId())) {
                found = true;

                assertEquals(payload, event.getPayload().get());
            }
        }

        assertTrue(found);
    }
}
