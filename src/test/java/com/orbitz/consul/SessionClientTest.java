package com.orbitz.consul;

import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import com.orbitz.consul.model.session.SessionInfo;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SessionClientTest extends BaseIntegrationTest {

    @Test
    public void testCreateAndDestroySession() throws Exception {
        SessionClient sessionClient = client.sessionClient();
        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();

        SessionCreatedResponse session = sessionClient.createSession(value);

        assertNotNull(session);
    }

    @Test
    public void testCreateEmptySession() throws Exception {
        SessionClient sessionClient = client.sessionClient();
        SessionCreatedResponse session = sessionClient.createSession(ImmutableSession.builder().build());

        assertNotNull(session);
    }

    @Test(expected = ConsulException.class)
    public void testRenewSession() throws Exception {
        SessionClient sessionClient = client.sessionClient();
        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();

        SessionCreatedResponse session = sessionClient.createSession(value);

        assertNotNull(session);

        SessionInfo info = sessionClient.renewSession(session.getId()).get();

        assertEquals(session.getId(), info.getId());
    }

    @Test
    public void testGetSessionInfo() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();
        String key = UUID.randomUUID().toString();

        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();

        assertTrue(keyValueClient.acquireLock(key, value.getName().get(), sessionId));
        assertFalse(keyValueClient.acquireLock(key, value.getName().get(), sessionId));
        assertEquals(sessionId, keyValueClient.getSession(key).get());

        SessionInfo sessionInfo = sessionClient.getSessionInfo(sessionId).orNull();
        assertNotNull(sessionInfo);
        assertEquals(sessionId, sessionInfo.getId());
    }

    @Test
    public void testListSessions() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();
        String key = UUID.randomUUID().toString();

        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();

        assertTrue(keyValueClient.acquireLock(key, value.getName().get(), sessionId));
        assertFalse(keyValueClient.acquireLock(key, value.getName().get(), sessionId));
        assertEquals(sessionId, keyValueClient.getSession(key).get());

        List<SessionInfo> result = sessionClient.listSessions();

        assertEquals(sessionId, result.get(0).getId());
    }
}