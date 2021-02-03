package ru.hh.consul.util;

import ru.hh.consul.BaseIntegrationTest;
import ru.hh.consul.Consul;
import ru.hh.consul.model.agent.Agent;
import org.junit.Test;

import java.net.Proxy;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;

public class CustomBuilderITest extends BaseIntegrationTest{

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
