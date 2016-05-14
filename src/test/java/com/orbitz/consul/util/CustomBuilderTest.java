package com.orbitz.consul.util;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.Agent;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomBuilderTest {

    @Test
    public void shouldConnectWithCustomBuilder() throws UnknownHostException {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(3600, TimeUnit.SECONDS)
                .writeTimeout(900, TimeUnit.MILLISECONDS);


        Consul client = Consul.builder().withHttpClientBuilder(builder).build();
        Agent agent = client.agentClient().getAgent();

        assertNotNull(agent);
        assertEquals("127.0.0.1", agent.getConfig().getClientAddr());
    }

    @Test
    public void shouldConnectWithCustomTimeouts() throws UnknownHostException {
        Consul client = Consul.builder()
                .withConnectTimeoutMillis(10000)
                .withReadTimeoutMillis(3600000)
                .withWriteTimeoutMillis(900)
                .build();
        Agent agent = client.agentClient().getAgent();

        assertNotNull(agent);
        assertEquals("127.0.0.1", agent.getConfig().getClientAddr());
    }

}
