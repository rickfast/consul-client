package com.orbitz.consul.config;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(JUnitParamsRunner.class)
public class CacheConfigTest {

    @Test
    public void testDefaults() {
        CacheConfig config = CacheConfig.builder().build();
        assertEquals(CacheConfig.DEFAULT_BACKOFF_DELAY, config.getBackOffDelay());
        assertEquals(CacheConfig.DEFAULT_WATCH_DURATION, config.getWatchDuration());
        assertEquals(CacheConfig.DEFAULT_MIN_DELAY_BETWEEN_REQUESTS, config.getMinimumDurationBetweenRequests());
        assertEquals(CacheConfig.DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_ENABLED, config.isTimeoutAutoAdjustmentEnabled());
        assertEquals(CacheConfig.DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_MARGIN, config.getTimeoutAutoAdjustmentMargin());

        AtomicBoolean loggedAsWarn = new AtomicBoolean(false);
        Logger logger = mock(Logger.class);
        doAnswer(vars -> {
            loggedAsWarn.set(true);
            return null;
        }).when(logger).error(anyString(), any(Throwable.class));
        config.getRefreshErrorLoggingConsumer().accept(logger, null, null);
        assertTrue("Should have logged as warning", loggedAsWarn.get());
    }

    @Test
    @Parameters(method = "getDurationSamples")
    @TestCaseName("Delay: {0}")
    public void testOverrideBackOffDelay(Duration backOffDelay) {
        CacheConfig config = CacheConfig.builder().withBackOffDelay(backOffDelay).build();
        assertEquals(backOffDelay, config.getBackOffDelay());
    }

    @Test
    @Parameters(method = "getDurationSamples")
    @TestCaseName("Delay: {0}")
    public void testOverrideMinDelayBetweenRequests(Duration delayBetweenRequests) {
        CacheConfig config = CacheConfig.builder().withMinDelayBetweenRequests(delayBetweenRequests).build();
        assertEquals(delayBetweenRequests, config.getMinimumDurationBetweenRequests());
    }

    @Test
    @Parameters({"true", "false"})
    @TestCaseName("Enabled: {0}")
    public void testOverrideTimeoutAutoAdjustmentEnabled(boolean enabled) {
        CacheConfig config = CacheConfig.builder().withTimeoutAutoAdjustmentEnabled(enabled).build();
        assertEquals(enabled, config.isTimeoutAutoAdjustmentEnabled());
    }

    @Test
    @Parameters(method = "getDurationSamples")
    @TestCaseName("Margin: {0}")
    public void testOverrideTimeoutAutoAdjustmentMargin(Duration margin) {
        CacheConfig config = CacheConfig.builder().withTimeoutAutoAdjustmentMargin(margin).build();
        assertEquals(margin, config.getTimeoutAutoAdjustmentMargin());
    }

    @Test
    @Parameters({"true", "false"})
    @TestCaseName("LogLevel as Warning: {0}")
    public void testOverrideRefreshErrorLogConsumer(boolean logLevelWarning) throws InterruptedException {
        CacheConfig config = logLevelWarning
                ? CacheConfig.builder().withRefreshErrorLoggedAsWarning().build()
                : CacheConfig.builder().withRefreshErrorLoggedAsError().build();

        AtomicBoolean logged = new AtomicBoolean(false);
        AtomicBoolean loggedAsWarn = new AtomicBoolean(false);
        Logger logger = mock(Logger.class);
        doAnswer(vars -> {
            loggedAsWarn.set(true);
            logged.set(true);
            return null;
        }).when(logger).warn(anyString(), any(Throwable.class));
        doAnswer(vars -> {
            loggedAsWarn.set(false);
            logged.set(true);
            return null;
        }).when(logger).error(anyString(), any(Throwable.class));

        config.getRefreshErrorLoggingConsumer().accept(logger, null, null);
        assertTrue(logged.get());
        assertEquals(logLevelWarning, loggedAsWarn.get());
    }

    @Test
    public void testOverrideRefreshErrorLogCustom() {
        AtomicBoolean loggedAsDebug = new AtomicBoolean(false);
        Logger logger = mock(Logger.class);
        doAnswer(vars -> {
            loggedAsDebug.set(true);
            return null;
        }).when(logger).debug(anyString(), any(Throwable.class));

        CacheConfig config = CacheConfig.builder().withRefreshErrorLoggedAs(Logger::debug).build();
        config.getRefreshErrorLoggingConsumer().accept(logger, null, null);
        assertTrue(loggedAsDebug.get());
    }

    public Object getDurationSamples() {
        return new Object[]{
                Duration.ZERO,
                Duration.ofSeconds(2),
                Duration.ofMinutes(10)
        };
    }
}
