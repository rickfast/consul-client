package com.orbitz.consul;

import com.orbitz.consul.model.event.Event;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTests {

    @Test
    public void shouldFire() throws InterruptedException {
        Consul client = Consul.newClient();
        EventClient eventClient = client.eventClient();

        String name = RandomStringUtils.random(10);
        Event fired = eventClient.fireEvent(name);

        Thread.sleep(1000);

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
        Consul client = Consul.newClient();
        EventClient eventClient = client.eventClient();

        String payload = RandomStringUtils.random(20);
        String name = RandomStringUtils.random(10);
        Event fired = eventClient.fireEvent(name, payload);

        Thread.sleep(1000);

        boolean found = false;

        for(Event event : eventClient.listEvents().getEvents()) {
            if (event.getName().equals(name) && event.getId().equals(fired.getId())) {
                found = true;

                assertEquals(payload, event.getPayload());
            }
        }

        assertTrue(found);
    }
}
