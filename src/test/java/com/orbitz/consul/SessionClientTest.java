package com.orbitz.consul;

import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import com.orbitz.consul.model.session.SessionInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SessionClientTest extends BaseIntegrationTest {

    private KeyValueClient keyValueClient;
    private SessionClient sessionClient;

    @Before
    public void setUp() {
        keyValueClient = client.keyValueClient();
        sessionClient = client.sessionClient();
    }

    @Test
    public void testCreateAndDestroySession() throws Exception {
        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        SessionCreatedResponse session = sessionClient.createSession(value);
        assertNotNull(session);

        sessionClient.destroySession(session.getId());
    }

    @Test
    public void testCreateEmptySession() throws Exception {
        SessionCreatedResponse session = sessionClient.createSession(ImmutableSession.builder().build());
        assertNotNull(session);
        sessionClient.destroySession(session.getId());
    }

    @Test
    public void testRenewSession() throws Exception {
        final Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();

        SessionCreatedResponse session = sessionClient.createSession(value);
        assertNotNull(session);

        try {
            SessionInfo info = sessionClient.renewSession(session.getId()).get();
            assertEquals(session.getId(), info.getId());
        } finally {
            sessionClient.destroySession(session.getId());
        }
    }

    @Test
    public void testAcquireLock() {
        String key = UUID.randomUUID().toString();

        Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();
        String valueName = value.getName().get();

        try {
            assertTrue("Should succeed to acquire a lock",
                    keyValueClient.acquireLock(key, valueName, sessionId));
            assertEquals(sessionId, keyValueClient.getSession(key).get());
        } finally {
            keyValueClient.releaseLock(key, sessionId);
            keyValueClient.deleteKey(key);
            sessionClient.destroySession(sessionId);
        }
    }

    @Test
    public void testAcquireLockTwiceFromSameSession() {
        String key = UUID.randomUUID().toString();

        Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();
        String valueName = value.getName().get();

        try {
            assertTrue("Should succeed to acquire a lock - first time",
                    keyValueClient.acquireLock(key, valueName, sessionId));
            assertTrue("Should succeed to acquire a lock - second time",
                    keyValueClient.acquireLock(key, valueName, sessionId));
            assertEquals(sessionId, keyValueClient.getSession(key).get());
        } finally {
            keyValueClient.releaseLock(key, sessionId);
            keyValueClient.deleteKey(key);
            sessionClient.destroySession(sessionId);
        }
    }

    @Test
    public void testAcquireLockTwiceFromDifferentSessions() {
        String key = UUID.randomUUID().toString();

        Session firstSessionValue = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String firstSessionId = sessionClient.createSession(firstSessionValue).getId();
        String firstSessionValueContent = firstSessionValue.getName().get();

        Session secondSessionValue = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String secondSessionId = sessionClient.createSession(secondSessionValue).getId();
        String secondSessionValueNameContent = secondSessionValue.getName().get();

        try {
            assertTrue("Should succeed to acquire a lock - first session",
                    keyValueClient.acquireLock(key, firstSessionValueContent, firstSessionId));
            assertFalse("Should fail to acquire a lock - second session",
                    keyValueClient.acquireLock(key, secondSessionValueNameContent, secondSessionId));

            assertEquals(firstSessionId, keyValueClient.getSession(key).get());
        } finally {
            keyValueClient.releaseLock(key, firstSessionId);
            keyValueClient.deleteKey(key);
            sessionClient.destroySession(firstSessionId);
            sessionClient.destroySession(secondSessionId);
        }
    }

    @Test
    public void testGetSessionInfo() throws Exception {
        String key = UUID.randomUUID().toString();

        Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();
        String valueName = value.getName().get();

        try {
            assertTrue("Should succeed to acquire a lock",
                    keyValueClient.acquireLock(key, valueName, sessionId));

            SessionInfo sessionInfo = sessionClient.getSessionInfo(sessionId).orElse(null);
            assertNotNull(sessionInfo);
            assertEquals(sessionId, sessionInfo.getId());
        } finally {
            keyValueClient.releaseLock(key, sessionId);
            keyValueClient.deleteKey(key);
            sessionClient.destroySession(sessionId);
        }
    }

    @Test
    public void testListSessions() throws Exception {
        String key = UUID.randomUUID().toString();

        Session value = ImmutableSession.builder().name("session_" + UUID.randomUUID().toString()).build();
        String sessionId = sessionClient.createSession(value).getId();

        try {
            assertTrue(keyValueClient.acquireLock(key, value.getName().get(), sessionId));
            List<SessionInfo> result = sessionClient.listSessions();

            assertTrue(result.stream().anyMatch(sessionInfo -> sessionId.equals(sessionInfo.getId())));
        } finally {
            keyValueClient.releaseLock(key, sessionId);
            keyValueClient.deleteKey(key);
            sessionClient.destroySession(sessionId);
        }
    }
}
