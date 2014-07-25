package com.orbitz.consul;

import org.junit.Rule;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.UUID;

import static com.orbitz.consul.util.ClientUtil.decodeBase64;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KeyValueTests {

    @Rule
    public ConsulRule consulRule = new ConsulRule();

    @Test
    @ConsulRunning
    public void shouldPutAndReceiveString() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertEquals(value, keyValueClient.getValueAsString(key).get());
    }

    @Test
    @ConsulRunning
    public void shouldPutAndReceiveValue() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertEquals(value, decodeBase64(keyValueClient.getValue(key).get().getValue()));
    }

    @Test
    @ConsulRunning
    public void shouldPutAndReceiveStrings() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String key2 = key + "/" + UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertTrue(keyValueClient.putValue(key2, value2));
        assertEquals(new HashSet<String>() {
            {
                add(value);
                add(value2);
            }
        }, new HashSet<String>(keyValueClient.getValuesAsString(key)));
    }
}
