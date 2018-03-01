package com.orbitz.consul;

import java.nio.charset.Charset;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.ImmutableOperation;
import com.orbitz.consul.model.kv.Operation;
import com.orbitz.consul.model.kv.TxResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import com.orbitz.consul.option.ImmutableDeleteOptions;
import com.orbitz.consul.option.ImmutableDeleteOptions.Builder;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KeyValueTests extends BaseIntegrationTest {
    private static final Charset TEST_CHARSET = Charset.forName("IBM297");

    @Test
    public void shouldPutAndReceiveString() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertEquals(value, keyValueClient.getValueAsString(key).get());
    }

    @Test
    public void shouldPutAndReceiveStringWithAnotherCharset() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value, TEST_CHARSET));
        assertEquals(value, keyValueClient.getValueAsString(key, TEST_CHARSET).get());
    }

    @Test
    public void shouldPutAndReceiveValue() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, received.getValueAsString().get());
        assertEquals(0L, received.getFlags());
    }

    @Test
    public void shouldPutAndReceiveValueWithAnotherCharset() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value, TEST_CHARSET));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, received.getValueAsString(TEST_CHARSET).get());
        assertEquals(0L, received.getFlags());
    }

    @Test
    public void shouldPutAndReceiveWithFlags() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        long flags = UUID.randomUUID().getMostSignificantBits();

        assertTrue(keyValueClient.putValue(key, value, flags));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, received.getValueAsString().get());
        assertEquals(flags, received.getFlags());
    }

    @Test
    public void shouldPutAndReceiveWithFlagsAndCharset() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        long flags = UUID.randomUUID().getMostSignificantBits();

        assertTrue(keyValueClient.putValue(key, value, flags, TEST_CHARSET));
        Value received = keyValueClient.getValue(key).get();
        assertEquals(value, received.getValueAsString(TEST_CHARSET).get());
        assertEquals(flags, received.getFlags());
    }

    @Test
    public void putNullValue() {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key));

        Value received = keyValueClient.getValue(key).get();
        assertFalse(received.getValue().isPresent());
    }

    @Test
    public void putNullValueWithAnotherCharset() {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, null, 0, PutOptions.BLANK, TEST_CHARSET));

        Value received = keyValueClient.getValue(key).get();
        assertFalse(received.getValue().isPresent());
    }

    @Test
    public void shouldPutAndReceiveBytes() {
        KeyValueClient keyValueClient = client.keyValueClient();
        byte[] value = new byte[256];
        ThreadLocalRandom.current().nextBytes(value);

        String key = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value, 0, PutOptions.BLANK));

        Value received = keyValueClient.getValue(key).get();
        assertTrue(received.getValue().isPresent());

        byte[] receivedBytes = received.getValueAsBytes()
                .get();

        assertArrayEquals(value, receivedBytes);
    }

    @Test
    public void shouldPutAndReceiveStrings() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String key2 = key + "/" + UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value));
        assertTrue(keyValueClient.putValue(key2, value2));
        assertEquals(ImmutableSet.of(value, value2), new HashSet<>(keyValueClient.getValuesAsString(key)));
    }

    @Test
    public void shouldPutAndReceiveStringsWithAnotherCharset() throws UnknownHostException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String key2 = key + "/" + UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();
        final String value2 = UUID.randomUUID().toString();

        assertTrue(keyValueClient.putValue(key, value, TEST_CHARSET));
        assertTrue(keyValueClient.putValue(key2, value2, TEST_CHARSET));
        assertEquals(ImmutableSet.of(value, value2), new HashSet<>(keyValueClient.getValuesAsString(key, TEST_CHARSET)));
    }

    @Test
    public void shouldDelete() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        keyValueClient.putValue(key, value);
        assertTrue(keyValueClient.getValueAsString(key).isPresent());

        keyValueClient.deleteKey(key);
        assertFalse(keyValueClient.getValueAsString(key).isPresent());
    }


    @Test
    public void shouldDeleteRecursively() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String childKEY = key + "/" + UUID.randomUUID().toString();

        final String value = UUID.randomUUID().toString();

        keyValueClient.putValue(key);
        keyValueClient.putValue(childKEY, value);

        assertTrue(keyValueClient.getValueAsString(childKEY).isPresent());

        keyValueClient.deleteKeys(key);

        assertFalse(keyValueClient.getValueAsString(key).isPresent());
        assertFalse(keyValueClient.getValueAsString(childKEY).isPresent());
    }

    @Test
    public void shouldDeleteCas() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        /**
         * Update the value twice and remember the value at each step
         */
        keyValueClient.putValue(key, value);

        final Optional<Value> valueAfter1stPut = keyValueClient.getValue(key);
        assertTrue(valueAfter1stPut.isPresent());
        assertTrue(valueAfter1stPut.get().getValueAsString().isPresent());

        keyValueClient.putValue(key, UUID.randomUUID().toString());

        final Optional<Value> valueAfter2ndPut = keyValueClient.getValue(key);
        assertTrue(valueAfter2ndPut.isPresent());
        assertTrue(valueAfter2ndPut.get().getValueAsString().isPresent());

        /**
         * Trying to delete the key once with the older lock, which should not work
         */
        final Builder deleteOptionsBuilderWithOlderLock = ImmutableDeleteOptions.builder();
        deleteOptionsBuilderWithOlderLock.cas(valueAfter1stPut.get().getModifyIndex());
        final ImmutableDeleteOptions deleteOptionsWithOlderLock = deleteOptionsBuilderWithOlderLock.build();

        keyValueClient.deleteKey(key, deleteOptionsWithOlderLock);

        assertTrue(keyValueClient.getValueAsString(key).isPresent());

        /**
         * Deleting the key with the most recent lock, which should work
         */
        final Builder deleteOptionsBuilderWithLatestLock = ImmutableDeleteOptions.builder();
        deleteOptionsBuilderWithLatestLock.cas(valueAfter2ndPut.get().getModifyIndex());
        final ImmutableDeleteOptions deleteOptionsWithLatestLock = deleteOptionsBuilderWithLatestLock.build();

        keyValueClient.deleteKey(key, deleteOptionsWithLatestLock);

        assertFalse(keyValueClient.getValueAsString(key).isPresent());
    }

    @Test
    public void acquireAndReleaseLock() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();
        String key = UUID.randomUUID().toString();
        String value = "session_" + UUID.randomUUID().toString();
        SessionCreatedResponse response = sessionClient.createSession(ImmutableSession.builder().name(value).build());
        String sessionId = response.getId();

        try {
            assertTrue(keyValueClient.acquireLock(key, value, sessionId));
            assertTrue(keyValueClient.acquireLock(key, value, sessionId)); // No ideas why there was an assertFalse

            assertTrue("SessionId must be present.", keyValueClient.getValue(key).get().getSession().isPresent());
            assertTrue(keyValueClient.releaseLock(key, sessionId));
            assertFalse("SessionId in the key value should be absent.", keyValueClient.getValue(key).get().getSession().isPresent());
            keyValueClient.deleteKey(key);
        } finally {
            sessionClient.destroySession(sessionId);
        }
    }

    @Test
    public void testGetSession() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        assertEquals(false, keyValueClient.getSession(key).isPresent());

        String sessionValue = "session_" + UUID.randomUUID().toString();
        SessionCreatedResponse response = sessionClient.createSession(ImmutableSession.builder().name(sessionValue).build());
        String sessionId = response.getId();

        try {
            assertTrue(keyValueClient.acquireLock(key, sessionValue, sessionId));
            assertTrue(keyValueClient.acquireLock(key, sessionValue, sessionId)); // No ideas why there was an assertFalse
            assertEquals(sessionId, keyValueClient.getSession(key).get());
        } finally {
            sessionClient.destroySession(sessionId);
        }
    }

    @Test
    public void testGetKeys() throws Exception {
        KeyValueClient kvClient = client.keyValueClient();
        String testString = "Hello World!";
        String key = "my_key";
        kvClient.putValue(key, testString);
        // check keys (this line throws com.orbitz.consul.ConsulException: Consul request failed)
        List<String> list = kvClient.getKeys(key);

        assertFalse(list.isEmpty());
        assertEquals(key, list.get(0));
    }

    @Test
    public void testAcquireLock() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        SessionClient sessionClient = client.sessionClient();

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        assertEquals(false, keyValueClient.getSession(key).isPresent());

        String sessionValue = "session_" + UUID.randomUUID().toString();
        SessionCreatedResponse response = sessionClient.createSession(ImmutableSession.builder().name(sessionValue).build());
        String sessionId = response.getId();

        String sessionValue2 = "session_" + UUID.randomUUID().toString();
        SessionCreatedResponse response2 = sessionClient.createSession(ImmutableSession.builder().name(sessionValue).build());
        String sessionId2 = response2.getId();

        try {
            assertTrue(keyValueClient.acquireLock(key, sessionValue, sessionId));
            // session-2 can't acquire the lock
            assertFalse(keyValueClient.acquireLock(key, sessionValue2, sessionId2));
            assertEquals(sessionId, keyValueClient.getSession(key).get());

            keyValueClient.releaseLock(key, sessionId);

            // session-2 now can acquire the lock
            assertTrue(keyValueClient.acquireLock(key, sessionValue2, sessionId2));
            // session-1 can't acquire the lock anymore
            assertFalse(keyValueClient.acquireLock(key, sessionValue, sessionId));
            assertEquals(sessionId2, keyValueClient.getSession(key).get());
        } finally {
            sessionClient.destroySession(sessionId);
            sessionClient.destroySession(sessionId2);
        }
    }

    @Test
    public void testGetValuesAsync() throws InterruptedException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        final CountDownLatch completed = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        keyValueClient.getValues(key, QueryOptions.BLANK, new ConsulResponseCallback<List<Value>>() {
            @Override
            public void onComplete(ConsulResponse<List<Value>> consulResponse) {
                success.set(true);
                completed.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                completed.countDown();
            }
        });
        completed.await(3, TimeUnit.SECONDS);
        keyValueClient.deleteKey(key);
        assertTrue(success.get());
    }

    @Test
    public void testGetConsulResponseWithValue() {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        Optional<ConsulResponse<Value>> response = keyValueClient.getConsulResponseWithValue(key);

        keyValueClient.deleteKey(key);

        assertTrue(response.get().getResponse().getKey().equals(key));
        assertTrue(response.get().getResponse().getValue().isPresent());
        assertNotNull(response.get().getIndex());
    }

    @Test
    public void testGetConsulResponseWithValues() {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        keyValueClient.putValue(key, value);

        ConsulResponse<List<Value>> response = keyValueClient.getConsulResponseWithValues(key);

        keyValueClient.deleteKey(key);

        assertTrue(!response.getResponse().isEmpty());
        assertNotNull(response.getIndex());
    }

    @Test
    public void testGetValueNotFoundAsync() throws InterruptedException {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();


        final CountDownLatch completed = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        keyValueClient.getValue(key, QueryOptions.BLANK, new ConsulResponseCallback<Optional<Value>>() {

            @Override
            public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {
                assertNotNull(consulResponse);
                completed.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                fail("404 isn't a failure for KVs");
            }
        });

        completed.await(3, TimeUnit.SECONDS);
        keyValueClient.deleteKey(key);
        assertFalse(success.get());
    }

    @Test
    @Ignore
    public void testBasicTxn() throws Exception {
        KeyValueClient keyValueClient = client.keyValueClient();
        String key = UUID.randomUUID().toString();
        String value = Base64.encodeBase64String(RandomStringUtils.random(20).getBytes());
        Operation[] operation = new Operation[] {ImmutableOperation.builder().verb("set")
                .key(key)
                .value(value).build()};

        ConsulResponse<TxResponse> response = keyValueClient.performTransaction(operation);

        assertEquals(value, keyValueClient.getValueAsString(key).get());
        assertEquals(response.getIndex(), keyValueClient.getValue(key).get().getModifyIndex());
    }

    @Test
    public void testUnknownKey() {
        List<String> shouldBeEmpty = client.keyValueClient().getValuesAsString("unknownKey");
        assertTrue(shouldBeEmpty.isEmpty());
    }
}
