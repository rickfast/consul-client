package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import org.junit.BeforeClass;

public abstract class BaseIntegrationTest {

    protected static Consul client;

    @BeforeClass
    public static void beforeClass() {
        client = Consul.builder().withHostAndPort(HostAndPort.fromParts("localhost", 8500)).build();
    }
}
