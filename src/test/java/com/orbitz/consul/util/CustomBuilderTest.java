package com.orbitz.consul.util;

import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.Agent;
import org.junit.Test;

import java.net.Proxy;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;

public class CustomBuilderTest extends BaseIntegrationTest{

    @Test
    public void shouldConnectWithCustomTimeouts() throws UnknownHostException {
        Consul client = Consul.builder()
                .withHostAndPort(defaultClientHostAndPort)
                .withProxy(Proxy.NO_PROXY)
                .withConnectTimeoutMillis(10000)
                .withReadTimeoutMillis(3600000)
                .withWriteTimeoutMillis(900)
                .build();
        Agent agent = client.agentClient().getAgent();
        assertNotNull(agent);
    }

}
