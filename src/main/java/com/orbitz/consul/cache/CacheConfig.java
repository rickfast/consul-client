package com.orbitz.consul.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.util.function.Supplier;

class CacheConfig {

    @VisibleForTesting
    static String CONFIG_CACHE_PATH = "com.orbitz.consul.cache";
    @VisibleForTesting
    static String BACKOFF_DELAY = "backOffDelay";
    private static String TIMEOUT_AUTO_ENABLED = "timeout.autoAdjustment.enable";
    private static String TIMEOUT_AUTO_MARGIN = "timeout.autoAdjustment.margin";

    private static final Supplier<CacheConfig> INSTANCE = Suppliers.memoize(CacheConfig::new);

    private final Config config;

    private CacheConfig() {
         this(ConfigFactory.load());
    }

    @VisibleForTesting
    CacheConfig(Config config) {
        this.config = config.getConfig(CONFIG_CACHE_PATH);
    }

    /**
     * Gets the instance of the cache configuration
     */
    static CacheConfig get() {
        return INSTANCE.get();
    }

    /**
     * Gets the back-off delay used in caches.
     * @return back-off delay
     * @throws RuntimeException if an error occurs while retrieving the configuration property.
     */
    Duration getBackOffDelay() {
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
    boolean isTimeoutAutoAdjustmentEnabled() {
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
    Duration getTimeoutAutoAdjustmentMargin() {
        try {
            return config.getDuration(TIMEOUT_AUTO_MARGIN);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error extracting config variable %s", TIMEOUT_AUTO_ENABLED), ex);
        }
    }
}
