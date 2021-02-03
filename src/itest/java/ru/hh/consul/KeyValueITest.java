package ru.hh.consul;

import java.nio.charset.Charset;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import ru.hh.consul.async.ConsulResponseCallback;
import ru.hh.consul.model.ConsulResponse;
import ru.hh.consul.model.kv.ImmutableOperation;
import ru.hh.consul.model.kv.Operation;
import ru.hh.consul.model.kv.TxResponse;
import ru.hh.consul.model.kv.Value;
import ru.hh.consul.model.session.ImmutableSession;
import ru.hh.consul.model.session.SessionCreatedResponse;
import ru.hh.consul.option.ConsistencyMode;
import ru.hh.consul.option.ImmutableDeleteOptions;
import ru.hh.consul.option.ImmutableQueryOptions;
import ru.hh.consul.option.PutOptions;
import ru.hh.consul.option.QueryOptions;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KeyValueITest extends BaseIntegrationTest {
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
        final ImmutableDeleteOptions.Builder deleteOptionsBuilderWithOlderLock = ImmutableDeleteOptions.builder();
        deleteOptionsBuilderWithOlderLock.cas(valueAfter1stPut.get().getModifyIndex());
        final ImmutableDeleteOptions deleteOptionsWithOlderLock = deleteOptionsBuilderWithOlderLock.build();

        keyValueClient.deleteKey(key, deleteOptionsWithOlderLock);

        assertTrue(keyValueClient.getValueAsString(key).isPresent());

        /**
         * Deleting the key with the most recent lock, which should work
         */
        final ImmutableDeleteOptions.Builder deleteOptionsBuilderWithLatestLock = ImmutableDeleteOptions.builder();
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

        final int numTests = 2;
        final CountDownLatch completed = new CountDownLatch(numTests);
        final AtomicInteger success = new AtomicInteger(0);

        keyValueClient.getValue(key, QueryOptions.BLANK, new ConsulResponseCallback<Optional<Value>>() {

            @Override
            public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {
                assertNotNull(consulResponse);
                // No cache, no Cache info
                assertFalse(consulResponse.getCacheReponseInfo().isPresent());
                completed.countDown();
                success.incrementAndGet();
            }

            @Override
            public void onFailure(Throwable throwable) {
                throw new AssertionError("KV should work without cache, 404 is not an error", throwable);
            }
        });
        completed.await(3, TimeUnit.SECONDS);
        QueryOptions queryOptions = ImmutableQueryOptions.builder()
                                      .consistencyMode(ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(
                                        Optional.of(60L), Optional.of(180L)
                                      )).build();
        keyValueClient.getValue(key, queryOptions, new ConsulResponseCallback<Optional<Value>>() {

            @Override
            public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {
                assertNotNull(consulResponse);
                completed.countDown();
                success.incrementAndGet();
            }

            @Override
            public void onFailure(Throwable throwable) {
                throw new AssertionError("KV should work with cache even if cache does not support ?cached", throwable);
            }
        });

        completed.await(3, TimeUnit.SECONDS);
        keyValueClient.deleteKey(key);
        assertEquals("Should be all success", success.get(), numTests);
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
    public void testGetKeysWithSeparator() {
        KeyValueClient kvClient = client.keyValueClient();
        kvClient.putValue("nested/first", "first");
        kvClient.putValue("nested/second", "second");

        List<String> keys = kvClient.getKeys("nested", "/");
        assertEquals(1, keys.size());
        assertEquals("nested/", keys.get(0));
    }

    @Test
    public void testUnknownKeyGetValues() {
        List<String> shouldBeEmpty = client.keyValueClient().getValuesAsString("unknownKey");
        assertTrue(shouldBeEmpty.isEmpty());
    }

    @Test
    public void testUnknownKeyGetKeys() {
        List<String> shouldBeEmpty = client.keyValueClient().getKeys("unknownKey");
        assertTrue(shouldBeEmpty.isEmpty());
    }
}
