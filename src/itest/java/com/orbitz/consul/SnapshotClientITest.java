package com.orbitz.consul;

import com.orbitz.consul.async.Callback;
import com.orbitz.consul.option.QueryOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SnapshotClientITest extends BaseIntegrationTest {

    private File snapshotFile;
    private SnapshotClient snapshotClient;

    @Before
    public void setUp() throws IOException {
        snapshotClient = client.snapshotClient();
        snapshotFile = File.createTempFile("snapshot", ".gz");
    }

    @After
    public void tearDown() {
        snapshotFile.delete();
    }

    @Test
    public void snapshotClientShouldBeAvailableInConsul() {
        assertNotNull(snapshotClient);
    }

    @Test
    public void shouldBeAbleToSaveAndRestoreSnapshot() throws MalformedURLException, InterruptedException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        client.agentClient().register(8080, new URL("http://localhost:123/health"), 1000L, serviceName, serviceId,
                Collections.emptyList(), Collections.emptyMap());
        Synchroniser.pause(Duration.ofMillis(100));
        assertTrue(checkIfServiceExist(serviceName));

        ensureSaveSnapshot();

        client.agentClient().deregister(serviceId);
        Synchroniser.pause(Duration.ofMillis(100));
        assertFalse(checkIfServiceExist(serviceName));

        ensureRestoreSnapshot();
        Thread.sleep(Duration.ofSeconds(1).toMillis());
        assertTrue(checkIfServiceExist(serviceName));
    }

    private void ensureSaveSnapshot() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        snapshotClient.save(snapshotFile, QueryOptions.BLANK, createCallback(latch, success));
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        assertTrue(success.get());
    }

    private void ensureRestoreSnapshot() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        snapshotClient.restore(snapshotFile, QueryOptions.BLANK, createCallback(latch, success));
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        assertTrue(success.get());
    }

    private boolean checkIfServiceExist(String serviceName) {
        return !client.healthClient().getAllServiceInstances(serviceName).getResponse().isEmpty();
    }

    private <T> Callback<T> createCallback(final CountDownLatch latch, final AtomicBoolean success) {
        return new Callback<T>() {
            @Override
            public void onResponse(T index) {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        };
    }
}
