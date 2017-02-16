package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import org.junit.Test;

public class LifecycleTests {

    @Test
    public void shouldBeDestroyable() {
        Consul client = Consul.builder().withHostAndPort(HostAndPort.fromParts("localhost", 8500)).build();
        client.destroy();
    }

}
