package com.orbitz.consul.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.util.function.Supplier;

public class CacheConfig {

    @VisibleForTesting
    static String CONFIG_CACHE_PATH = "com.orbitz.consul.cache";
    @VisibleForTesting
    static String BACKOFF_DELAY = "backOffDelay";
    @VisibleForTesting
    static String WATCH_DURATION = "watch";

    private static String TIMEOUT_AUTO_ENABLED = "timeout.autoAdjustment.enable";
    private static String TIMEOUT_AUTO_MARGIN = "timeout.autoAdjustment.margin";
    private static String REQUEST_RATE_LIMITER = "minTimeBetweenRequests";

    private static final Supplier<CacheConfig> INSTANCE = Suppliers.memoize(CacheConfig::new);

    private final Config config;

    private CacheConfig() {
         this(ConfigFactory.load());
    }

    public CacheConfig(Config config) {
        this.config = config
                .withFallback(ConfigFactory.parseResources("defaults.conf"))
                .getConfig(CONFIG_CACHE_PATH);
    }

    /**
     * Gets the instance of the cache configuration
     */
    public static CacheConfig get() {
        return INSTANCE.get();
    }

    /**
     * Gets the back-off delay used in caches.
     * @return back-off delay
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    public Duration getBackOffDelay() {
        try {
            return config.getDuration(BACKOFF_DELAY);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", BACKOFF_DELAY), ex);
        }
    }

    /**
     * Is the automatic adjustment of read timeout enabled?
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    public boolean isTimeoutAutoAdjustmentEnabled() {
        try {
            return config.getBoolean(TIMEOUT_AUTO_ENABLED);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", TIMEOUT_AUTO_ENABLED), ex);
        }
    }

    /**
     * Gets the margin of the read timeout for caches.
     * The margin represents the additional amount of time given to the read timeout, in addition to the wait duration.
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    public Duration getTimeoutAutoAdjustmentMargin() {
        try {
            return config.getDuration(TIMEOUT_AUTO_MARGIN);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", TIMEOUT_AUTO_ENABLED), ex);
        }
    }

    /**
     * Gets the default watch duration for caches.
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    public Duration getWatchDuration() {
        Duration duration;
        try {
            duration = config.getDuration(WATCH_DURATION);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", WATCH_DURATION), ex);
        }

        // Watch duration is limited to 10 minutes, see https://www.consul.io/api/index.html#blocking-queries
        if (duration.isNegative() || duration.compareTo(Duration.ofMinutes(10)) > 0) {
            throw new RuntimeException(String.format("Invalid watch duration: %s ms (must be between 0 and 10 minutes",
                    duration.toMillis()));
        }

        return duration;
    }

    /**
     * Gets the minimum time between two requests for caches.
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    public Duration getMinimumDurationBetweenRequests() {
        try {
            return config.getDuration(REQUEST_RATE_LIMITER);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", REQUEST_RATE_LIMITER), ex);
        }
    }
}
