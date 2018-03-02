package com.orbitz.consul.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.time.Duration;

public class CacheConfig {

    @VisibleForTesting
    static final Duration DEFAULT_WATCH_DURATION = Duration.ofSeconds(10);
    @VisibleForTesting
    static final Duration DEFAULT_BACKOFF_DELAY = Duration.ofSeconds(10);
    @VisibleForTesting
    static final Duration DEFAULT_MIN_DELAY_BETWEEN_REQUESTS = Duration.ZERO;
    @VisibleForTesting
    static final boolean DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_ENABLED = true;
    @VisibleForTesting
    static final Duration DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_MARGIN = Duration.ofSeconds(2);
    @VisibleForTesting
    static final boolean DEFAULT_REFRESH_ERROR_LOGGED_AS_WARNING = false;

    private final Duration backOffDelay;
    private final Duration minDelayBetweenRequests;
    private final Duration timeoutAutoAdjustmentMargin;
    private final boolean timeoutAutoAdjustmentEnabled;
    private final boolean refreshErrorLoggedAsWarning;

    private CacheConfig(Duration backOffDelay, Duration minDelayBetweenRequests,
                        boolean timeoutAutoAdjustmentEnabled, Duration timeoutAutoAdjustmentMargin,
                        boolean refreshErrorLoggedAsWarning) {
        this.backOffDelay = backOffDelay;
        this.minDelayBetweenRequests = minDelayBetweenRequests;
        this.timeoutAutoAdjustmentEnabled = timeoutAutoAdjustmentEnabled;
        this.timeoutAutoAdjustmentMargin = timeoutAutoAdjustmentMargin;
        this.refreshErrorLoggedAsWarning = refreshErrorLoggedAsWarning;
    }

    /**
     * Gets the default watch duration for caches.
     */
    public Duration getWatchDuration() {
        return DEFAULT_WATCH_DURATION;
    }

    /**
     * Gets the back-off delay used in caches.
     */
    public Duration getBackOffDelay() {
        return backOffDelay;
    }

    /**
     * Is the automatic adjustment of read timeout enabled?
     */
    public boolean isTimeoutAutoAdjustmentEnabled() {
       return timeoutAutoAdjustmentEnabled;
    }

    /**
     * Gets the margin of the read timeout for caches.
     * The margin represents the additional amount of time given to the read timeout, in addition to the wait duration.
     */
    public Duration getTimeoutAutoAdjustmentMargin() {
        return timeoutAutoAdjustmentMargin;
    }

    /**
     * Gets the minimum time between two requests for caches.
     */
    public Duration getMinimumDurationBetweenRequests() {
        return minDelayBetweenRequests;
    }

    /**
     * Should refresh error be logged as warning?
     * @return true if they should be logged as warning, false if they should remain error.
     */
    public boolean isRefreshErrorLoggedAsWarning() {
        return refreshErrorLoggedAsWarning;
    }

    /**
     * Creates a new {@link CacheConfig.Builder} object.
     *
     * @return A new Consul builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration backOffDelay = DEFAULT_BACKOFF_DELAY;
        private Duration minDelayBetweenRequests = DEFAULT_MIN_DELAY_BETWEEN_REQUESTS;
        private Duration timeoutAutoAdjustmentMargin = DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_MARGIN;
        private boolean timeoutAutoAdjustmentEnabled = DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_ENABLED;
        private boolean refreshErrorLoggedAsWarning = DEFAULT_REFRESH_ERROR_LOGGED_AS_WARNING;

        private Builder() {

        }

        /**
         * Sets the back-off delay used in caches.
         */
        public Builder withBackOffDelay(Duration delay) {
            this.backOffDelay = Preconditions.checkNotNull(delay, "Delay cannot be null");
            return this;
        }

        /**
         * Sets the minimum time between two requests for caches.
         */
        public Builder withMinDelayBetweenRequests(Duration delay) {
            this.minDelayBetweenRequests = Preconditions.checkNotNull(delay, "Delay cannot be null");
            return this;
        }

        /**
         * Enable/Disable the automatic adjustment of read timeout
         */
        public Builder withTimeoutAutoAdjustmentEnabled(boolean enabled) {
            this.timeoutAutoAdjustmentEnabled = enabled;
            return this;
        }

        /**
         * Sets the margin of the read timeout for caches.
         * The margin represents the additional amount of time given to the read timeout, in addition to the wait duration.
         */
        public Builder withTimeoutAutoAdjustmentMargin(Duration margin) {
            this.timeoutAutoAdjustmentMargin = Preconditions.checkNotNull(margin, "Margin cannot be null");
            return this;
        }

        /**
         * Sets refresh error log level as warning
         */
        public Builder withRefreshErrorLoggedAsWarning() {
            this.refreshErrorLoggedAsWarning = true;
            return this;
        }

        /**
         * Sets refresh error log level as error
         */
        public Builder withRefreshErrorLoggedAsError() {
            this.refreshErrorLoggedAsWarning = false;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(backOffDelay, minDelayBetweenRequests,
                    timeoutAutoAdjustmentEnabled, timeoutAutoAdjustmentMargin,
                    refreshErrorLoggedAsWarning);
        }
    }
}
