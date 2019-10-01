package com.orbitz.consul.util.failover.strategy;

import com.google.common.net.HostAndPort;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class BlacklistingConsulFailoverStrategyTest {
    private BlacklistingConsulFailoverStrategy blacklistingConsulFailoverStrategy;

    @Before
    public void setup() {
        // Create a set of targets
        final Collection<HostAndPort> targets = new ArrayList<>();
        targets.add(HostAndPort.fromParts("1.2.3.4", 8501));
        targets.add(HostAndPort.fromParts("localhost", 8501));

        blacklistingConsulFailoverStrategy = new BlacklistingConsulFailoverStrategy(targets, 100000);
    }

    @Test
    public void getFirstUrlBack() {
        Request previousRequest = new Request.Builder().url("https://1.2.3.4:8501/v1/agent/members").build();
        Response previousResponse = null;

        Optional<Request> result = blacklistingConsulFailoverStrategy.computeNextStage(previousRequest, previousResponse);

        assertEquals(true, result.isPresent());
        assertEquals("https://1.2.3.4:8501/v1/agent/members", result.get().url().toString());
    }

    @Test
    public void getSecondUrlBackAfterFirstOneIsBlacklisted() {
        Request previousRequest = new Request.Builder().url("https://1.2.3.4:8501/v1/agent/members").build();
        Response previousResponse = null;

        Optional<Request> result1 = blacklistingConsulFailoverStrategy.computeNextStage(previousRequest, previousResponse);

        assertEquals(true, result1.isPresent());
        assertEquals("https://1.2.3.4:8501/v1/agent/members", result1.get().url().toString());

        blacklistingConsulFailoverStrategy.markRequestFailed(result1.get());
        Optional<Request> result2 = blacklistingConsulFailoverStrategy.computeNextStage(result1.get(), previousResponse);

        assertEquals(true, result2.isPresent());
        assertEquals("https://localhost:8501/v1/agent/members", result2.get().url().toString());
    }

    @Test
    public void getNoUrlBackAfterBothAreBlacklisted() {
        Request previousRequest = new Request.Builder().url("https://1.2.3.4:8501/v1/agent/members").build();
        Response previousResponse = null;

        Optional<Request> result1 = blacklistingConsulFailoverStrategy.computeNextStage(previousRequest, previousResponse);

        assertEquals(true, result1.isPresent());
        assertEquals("https://1.2.3.4:8501/v1/agent/members", result1.get().url().toString());

        blacklistingConsulFailoverStrategy.markRequestFailed(result1.get());
        Optional<Request> result2 = blacklistingConsulFailoverStrategy.computeNextStage(result1.get(), previousResponse);

        assertEquals(true, result2.isPresent());
        assertEquals("https://localhost:8501/v1/agent/members", result2.get().url().toString());

        blacklistingConsulFailoverStrategy.markRequestFailed(result2.get());

        Optional<Request> result3 = blacklistingConsulFailoverStrategy.computeNextStage(result2.get(), previousResponse);

        assertEquals(false, result3.isPresent());
    }
}
