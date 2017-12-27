package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import org.junit.BeforeClass;

import java.time.Duration;

public abstract class BaseIntegrationTest {

    protected static Consul client;

    @BeforeClass
    public static void beforeClass() {
        client = Consul.builder()
                .withHostAndPort(HostAndPort.fromParts("localhost", 8500))
                .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                .build();
    }
}
