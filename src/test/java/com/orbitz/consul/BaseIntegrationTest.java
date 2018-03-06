package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import org.junit.After;
import org.junit.BeforeClass;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseIntegrationTest {

    private final List<String> deregisterServices = new CopyOnWriteArrayList<>();

    protected static Consul client;

    protected static HostAndPort defaultClientHostAndPort = HostAndPort.fromParts("localhost", 8500);

    @BeforeClass
    public static void beforeClass() {
        client = Consul.builder()
                .withHostAndPort(defaultClientHostAndPort)
                .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                .build();
    }

    @After
    public void after() {
        deregisterServices.forEach(client.agentClient()::deregister);
        deregisterServices.clear();
    }

    protected String createAutoDeregisterServiceId() {
        String serviceId = UUID.randomUUID().toString();
        deregisterServices.add(serviceId);

        return serviceId;
    }
}
