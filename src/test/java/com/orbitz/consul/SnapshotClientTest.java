package com.orbitz.consul;

import com.orbitz.consul.async.Callback;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SnapshotClientTest extends BaseIntegrationTest {

    private File snapshotFile;
    private SnapshotClient snapshotClient;
    private CountDownLatch latch = new CountDownLatch(1);
    private AtomicBoolean success = new AtomicBoolean(false);

    @Before
    public void setUp() {
        snapshotClient = client.snapshotClient();
        snapshotFile = new File("./snapshot.gz");

        latch = new CountDownLatch(1);
        success = new AtomicBoolean(false);
    }

    @Test
    public void snapshotClientShouldBeAvailableInConsul() {
        assertNotNull(snapshotClient);
    }

    @Test
    public void shouldBeAbleToSaveAndRestoreSnapshot() throws MalformedURLException, InterruptedException {
        String serviceName = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        client.agentClient().register(8080, new URL("http://localhost:123/health"), 1000L, serviceName, serviceId);
        assertTrue(checkIfServiceExist(serviceName));

        ensureSaveSnapshot();

        client.agentClient().deregister(serviceId);
        assertFalse(checkIfServiceExist(serviceName));

        ensureRestoreSnapshot();
        Thread.sleep(Duration.ofSeconds(1).toMillis());
        assertTrue(checkIfServiceExist(serviceName));
    }

    private void ensureSaveSnapshot() throws InterruptedException {
        snapshotClient.save(snapshotFile, QueryOptions.BLANK, createCallback());
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        assertTrue(success.get());
    }

    private void ensureRestoreSnapshot() throws InterruptedException {
        snapshotClient.restore(snapshotFile, QueryOptions.BLANK, createCallback());
        assertTrue(latch.await(1, TimeUnit.MINUTES));
        assertTrue(success.get());
    }

    private boolean checkIfServiceExist(String serviceName) {
        return !client.healthClient().getAllServiceInstances(serviceName).getResponse().isEmpty();
    }

    private <T> Callback<T> createCallback() {
        return new Callback<T>() {
            @Override
            public void onResponse(T index) {
                latch.countDown();
                success.set(true);
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        };
    }
}
