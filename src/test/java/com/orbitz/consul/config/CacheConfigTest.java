package com.orbitz.consul.config;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

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
        assertEquals(CacheConfig.DEFAULT_REFRESH_ERROR_LOGGED_AS_WARNING, config.isRefreshErrorLoggedAsWarning());
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
    public void testOverrideRefreshErrorLogLevel(boolean logLevelWarning) {
        CacheConfig config = logLevelWarning
                ? CacheConfig.builder().withRefreshErrorLoggedAsWarning().build()
                : CacheConfig.builder().withRefreshErrorLoggedAsError().build();
        assertEquals(logLevelWarning, config.isRefreshErrorLoggedAsWarning());
    }

    public Object getDurationSamples() {
        return new Object[]{
                Duration.ZERO,
                Duration.ofSeconds(2),
                Duration.ofMinutes(10)
        };
    }
}
