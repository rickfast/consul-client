package com.orbitz.consul;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SnapshotClientTest extends BaseIntegrationTest {

    @Test
    public void snapshotClientShouldBeAvailableInConsul() {
        assertNotNull(client.snapshotClient());
    }

}
