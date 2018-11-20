package com.orbitz.consul.config;

import com.orbitz.consul.cache.CacheDescriptor;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.monitoring.ClientEventHandler;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

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
        assertEquals(CacheConfig.DEFAULT_BACKOFF_DELAY, config.getMinimumBackOffDelay());
        assertEquals(CacheConfig.DEFAULT_BACKOFF_DELAY, config.getMaximumBackOffDelay());
        assertEquals(CacheConfig.DEFAULT_WATCH_DURATION, config.getWatchDuration());
        assertEquals(CacheConfig.DEFAULT_MIN_DELAY_BETWEEN_REQUESTS, config.getMinimumDurationBetweenRequests());
        assertEquals(CacheConfig.DEFAULT_MIN_DELAY_ON_EMPTY_RESULT, config.getMinimumDurationDelayOnEmptyResult());
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
        assertEquals(backOffDelay, config.getMinimumBackOffDelay());
        assertEquals(backOffDelay, config.getMaximumBackOffDelay());
    }

    @Test
    @Parameters(method = "getDurationSamples")
    @TestCaseName("Delay: {0}")
    public void testOverrideMinDelayBetweenRequests(Duration delayBetweenRequests) {
        CacheConfig config = CacheConfig.builder().withMinDelayBetweenRequests(delayBetweenRequests).build();
        assertEquals(delayBetweenRequests, config.getMinimumDurationBetweenRequests());
    }

    @Test
    @Parameters(method = "getDurationSamples")
    @TestCaseName("Delay: {0}")
    public void testOverrideMinDelayOnEmptyResult(Duration delayBetweenRequests) {
        CacheConfig config = CacheConfig.builder().withMinDelayOnEmptyResult(delayBetweenRequests).build();
        assertEquals(delayBetweenRequests, config.getMinimumDurationDelayOnEmptyResult());
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

    @Test
    @Parameters(method = "getMinMaxDurationSamples")
    @TestCaseName("min Delay: {0}, max Delay: {1}")
    public void testOverrideRandomBackOffDelay(Duration minDelay, Duration maxDelay, boolean isValid) {
        try {
            CacheConfig config = CacheConfig.builder().withBackOffDelay(minDelay, maxDelay).build();
            if (!isValid) {
                Assert.fail(String.format("Should not be able to build cache with min retry delay %d ms and max retry delay %d ms",
                        minDelay.toMillis(), maxDelay.toMillis()));
            }
            assertEquals(minDelay, config.getMinimumBackOffDelay());
            assertEquals(maxDelay, config.getMaximumBackOffDelay());
        } catch (NullPointerException | IllegalArgumentException e) {
            if (isValid) {
                throw new AssertionError(String.format("Should be able to build cache with min retry delay %d ms and max retry delay %d ms",
                        minDelay.toMillis(), maxDelay.toMillis()), e);
            }
        }
    }

    public Object getMinMaxDurationSamples() {
        return new Object[]{
                new Object[] { Duration.ZERO, Duration.ZERO, true },
                new Object[] { Duration.ofSeconds(2), Duration.ofSeconds(2), true },
                new Object[] { Duration.ZERO, Duration.ofSeconds(2), true },
                new Object[] { Duration.ofSeconds(2), Duration.ZERO, false },
                new Object[] { Duration.ofSeconds(1), Duration.ofSeconds(2), true },
                new Object[] { Duration.ofSeconds(2), Duration.ofSeconds(1), false },
                new Object[] { Duration.ofSeconds(-1), Duration.ZERO, false },
                new Object[] { Duration.ZERO, Duration.ofSeconds(-1), false },
                new Object[] { Duration.ofSeconds(-1), Duration.ofSeconds(-1), false },
        };
    }

    @Test
    public void testMinDelayOnEmptyResultWithNoResults() throws InterruptedException {
        TestCacheSupplier res = new TestCacheSupplier(0, Duration.ofMillis(100));

        TestCache cache = TestCache.createCache(CacheConfig.builder()
                .withMinDelayOnEmptyResult(Duration.ofMillis(100))
                .build(), res);
        cache.start();
        Thread.sleep(300);
        assertTrue(res.run > 0);
        cache.stop();
    }

    @Test
    public void testMinDelayOnEmptyResultWithResults() throws InterruptedException {
        TestCacheSupplier res = new TestCacheSupplier(1, Duration.ofMillis(50));

        TestCache cache = TestCache.createCache(CacheConfig.builder()
                .withMinDelayOnEmptyResult(Duration.ofMillis(100))
                .withMinDelayBetweenRequests(Duration.ofMillis(50)) // do not blow ourselves up
                .build(), res);
        cache.start();
        Thread.sleep(300);
        assertTrue(res.run > 0);
        cache.stop();
    }


    static class TestCache extends ConsulCache<Integer, Integer> {
        private TestCache(Function<Integer, Integer> keyConversion, CallbackConsumer<Integer> callbackConsumer, CacheConfig cacheConfig, ClientEventHandler eventHandler, CacheDescriptor cacheDescriptor) {
            super(keyConversion, callbackConsumer, cacheConfig, eventHandler, cacheDescriptor);
        }

        static TestCache createCache(CacheConfig config, Supplier<List<Integer>> res) {
            ClientEventHandler ev = mock(ClientEventHandler.class);
            CacheDescriptor cacheDescriptor = new CacheDescriptor("test", "test");

            final CallbackConsumer<Integer> callbackConsumer = (index, callback) -> {
                callback.onComplete(new ConsulResponse<>(res.get(), 0, true, BigInteger.ZERO));
            };

            return new TestCache((i) -> i,
                    callbackConsumer,
                    config,
                    ev,
                    cacheDescriptor);
        }
    }

    static class TestCacheSupplier implements Supplier<List<Integer>> {
        int run = 0;
        int resultCount;
        private Duration expectedInterval;
        private LocalTime lastCall;

        TestCacheSupplier(int resultCount, Duration expectedInterval) {
            this.resultCount = resultCount;
            this.expectedInterval = expectedInterval;
        }

        @Override
        public List<Integer> get() {
            if (lastCall != null) {
                long between = Duration.between(lastCall, LocalTime.now()).toMillis();
                assertTrue(String.format("expected duration between calls of %d, got %s", expectedInterval.toMillis(), between),
                        Math.abs(between - expectedInterval.toMillis()) < 20);
            }
            lastCall = LocalTime.now();
            run++;

            List<Integer> response = new ArrayList<>();
            for (int i = 0; i < resultCount; i++) {
                response.add(1);
            }
            return response;
        }
    }
}
