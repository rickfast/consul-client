package com.orbitz.consul;

import com.orbitz.consul.model.kv.Value;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.UUID;

import static com.orbitz.consul.util.ClientUtil.decodeBase64;
import static org.junit.Assert.*;

public class KeyValueTests {

    @Test
    public void shouldPutAndReceiveString() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertEquals(value, keyValueClient.getValueAsString(key).get());
    }

    @Test
    public void shouldPutAndReceiveValue() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, decodeBase64(received.getValue()));
        assertEquals(0L, received.getFlags());
    }

    @Test
    public void shouldPutAndReceiveWithFlags() throws UnknownHostException {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        long flags = UUID.randomUUID().getMostSignificantBits();

        assertTrue(keyValueClient.putValue(key, value, flags));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, decodeBase64(received.getValue()));
        assertEquals(flags, received.getFlags());
    }

    @Test
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

    @Test
    public void shouldDelete() throws Exception {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        keyValueClient.putValue(key, value);

        assertTrue(keyValueClient.getValueAsString(key).isPresent());

        keyValueClient.deleteKey(key);

        assertFalse(keyValueClient.getValueAsString(key).isPresent());
    }

    @Test
    public void acquireAndReleaseLock() throws Exception {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();
        String key = UUID.randomUUID().toString();

        final String value = "{\"Name\":\"myservice\"}";
        String session = sessionClient.createSession(value).get();

        System.out.println("SessionInfo: " + session);
        assertTrue(keyValueClient.acquireLock(key, value, session));
        assertFalse(keyValueClient.acquireLock(key, value, session));

        System.out.println("key: " + key);
        assertNotNull("SessionId in the key value should be NOT NULL.", keyValueClient.getValue(key).get().getSession());
        assertTrue(keyValueClient.releaseLock(key, session));
        assertNull("SessionId in the key value should be NULL.", keyValueClient.getValue(key).get().getSession());
    }

    @Test
    public void testGetSession() throws Exception {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        assertEquals(false, keyValueClient.getSession(key).isPresent());

        final String sessionValue = "{\"Name\":\"myservice\"}";
        String session = sessionClient.createSession(sessionValue).get();

        System.out.println("SessionInfo: " + session);
        assertTrue(keyValueClient.acquireLock(key, sessionValue, session));
        assertFalse(keyValueClient.acquireLock(key, sessionValue, session));
        assertEquals(session, keyValueClient.getSession(key).get());
    }

}
