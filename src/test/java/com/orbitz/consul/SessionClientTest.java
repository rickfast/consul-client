package com.orbitz.consul;

import com.google.common.base.Optional;
import com.orbitz.consul.model.session.SessionInfo;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.UUID;

public class SessionClientTest extends TestCase {

    @Test
    public void testCreateAndDestroySession() throws Exception {
        Consul client = Consul.newClient();
        SessionClient sessionClient = client.sessionClient();
        String value = "{\"Name\":\"service1\"}";
        Optional<String> session = sessionClient.createSession(value);

        assertNotNull(session);
        assertTrue(sessionClient.destroySession(session.get()));
    }


    @Test
    public void testGetSessionInfo() throws Exception {
        Consul client = Consul.newClient();
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();
        String key = UUID.randomUUID().toString();

        final String value = "{\"Name\":\"myservice\"}";
        String sessionId = sessionClient.createSession(value).get();

        assertTrue(keyValueClient.acquireLock(key, value, sessionId));
        assertFalse(keyValueClient.acquireLock(key, value, sessionId));
        assertEquals(sessionId, keyValueClient.getSession(key).get());

        SessionInfo sessionInfo = sessionClient.getSessionInfo(sessionId).orNull();
        assertNotNull(sessionInfo);
        assertEquals(sessionId, sessionInfo.getId());
    }

}