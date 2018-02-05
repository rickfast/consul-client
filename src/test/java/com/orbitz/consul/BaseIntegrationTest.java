package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import org.junit.BeforeClass;

import java.time.Duration;

public abstract class BaseIntegrationTest {

    protected static Consul client;

    protected static HostAndPort defaultClientHostAndPort = HostAndPort.fromParts("localhost", 8500);

    @BeforeClass
    public static void beforeClass() {
        client = Consul.builder()
                .withHostAndPort(defaultClientHostAndPort)
                .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                .build();
    }
}
