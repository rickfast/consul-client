package com.orbitz.consul.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;

import java.time.Duration;

public class CacheConfig {

    @VisibleForTesting
    static final Duration DEFAULT_WATCH_DURATION = Duration.ofSeconds(10);
    @VisibleForTesting
    static final Duration DEFAULT_BACKOFF_DELAY = Duration.ofSeconds(10);
    @VisibleForTesting
    static final Duration DEFAULT_MIN_DELAY_BETWEEN_REQUESTS = Duration.ZERO;
    @VisibleForTesting
    static final Duration DEFAULT_MIN_DELAY_ON_EMPTY_RESULT = Duration.ZERO;
    @VisibleForTesting
    static final boolean DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_ENABLED = true;
    @VisibleForTesting
    static final Duration DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_MARGIN = Duration.ofSeconds(2);
    @VisibleForTesting
    static final RefreshErrorLogConsumer DEFAULT_REFRESH_ERROR_LOG_CONSUMER = Logger::error;

    private final Duration minBackOffDelay;
    private final Duration maxBackOffDelay;
    private final Duration minDelayBetweenRequests;
    private final Duration minDelayOnEmptyResult;
    private final Duration timeoutAutoAdjustmentMargin;
    private final boolean timeoutAutoAdjustmentEnabled;
    private final RefreshErrorLogConsumer refreshErrorLogConsumer;

    private CacheConfig(Duration minBackOffDelay, Duration maxBackOffDelay, Duration minDelayBetweenRequests,
                        Duration minDelayOnEmptyResult, boolean timeoutAutoAdjustmentEnabled,
                        Duration timeoutAutoAdjustmentMargin, RefreshErrorLogConsumer refreshErrorLogConsumer) {
        this.minBackOffDelay = minBackOffDelay;
        this.maxBackOffDelay = maxBackOffDelay;
        this.minDelayBetweenRequests = minDelayBetweenRequests;
        this.minDelayOnEmptyResult = minDelayOnEmptyResult;
        this.timeoutAutoAdjustmentEnabled = timeoutAutoAdjustmentEnabled;
        this.timeoutAutoAdjustmentMargin = timeoutAutoAdjustmentMargin;
        this.refreshErrorLogConsumer = refreshErrorLogConsumer;
    }

    /**
     * Gets the default watch duration for caches.
     */
    public Duration getWatchDuration() {
        return DEFAULT_WATCH_DURATION;
    }

    /**
     * Gets the minimum back-off delay used in caches.
     */
    public Duration getMinimumBackOffDelay() {
        return minBackOffDelay;
    }

    /**
     * Gets the maximum back-off delay used in caches.
     */
    public Duration getMaximumBackOffDelay() {
        return maxBackOffDelay;
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
     * Gets the minimum time between two requests for caches.
     */
    public Duration getMinimumDurationDelayOnEmptyResult() {
        return minDelayOnEmptyResult;
    }

    /**
     * Gets the function that will be called in case of error.
     */
    public RefreshErrorLogConsumer getRefreshErrorLoggingConsumer() {
        return refreshErrorLogConsumer;
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
        private Duration minBackOffDelay = DEFAULT_BACKOFF_DELAY;
        private Duration maxBackOffDelay = DEFAULT_BACKOFF_DELAY;
        private Duration minDelayBetweenRequests = DEFAULT_MIN_DELAY_BETWEEN_REQUESTS;
        private Duration minDelayOnEmptyResult = DEFAULT_MIN_DELAY_ON_EMPTY_RESULT;
        private Duration timeoutAutoAdjustmentMargin = DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_MARGIN;
        private boolean timeoutAutoAdjustmentEnabled = DEFAULT_TIMEOUT_AUTO_ADJUSTMENT_ENABLED;
        private RefreshErrorLogConsumer refreshErrorLogConsumer = DEFAULT_REFRESH_ERROR_LOG_CONSUMER;

        private Builder() {

        }

        /**
         * Sets the back-off delay used in caches.
         * @throws IllegalArgumentException if {@code delay} is negative.
         */
        public Builder withBackOffDelay(Duration delay) {
            this.minBackOffDelay = Preconditions.checkNotNull(delay, "Delay cannot be null");
            this.maxBackOffDelay = delay;
            Preconditions.checkArgument(!delay.isNegative(), "Delay must be positive");
            return this;
        }

        /**
         * Sets a random delay between the {@code minDelay} and {@code maxDelay} (inclusive) to occur between retries.
         * @throws IllegalArgumentException if {@code minDelay} or {@code maxDelay} is negative, or if {@code minDelay} is superior to {@code maxDelay}.
         */
        public Builder withBackOffDelay(Duration minDelay, Duration maxDelay) {
            this.minBackOffDelay = Preconditions.checkNotNull(minDelay, "Minimum delay cannot be null");
            this.maxBackOffDelay = Preconditions.checkNotNull(maxDelay, "Maximum delay cannot be null");
            Preconditions.checkArgument(!minDelay.isNegative(), "Minimum delay must be positive");
            Preconditions.checkArgument(!maxDelay.minus(minDelay).isNegative(), "Minimum delay must be less than maximum delay");
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
         * Sets the minimum time between two requests for caches when an empty result is returned.
         */
        public Builder withMinDelayOnEmptyResult(Duration delay) {
            this.minDelayOnEmptyResult = Preconditions.checkNotNull(delay, "Delay cannot be null");
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
         * Log refresh errors as warning
         */
        public Builder withRefreshErrorLoggedAsWarning() {
            this.refreshErrorLogConsumer = Logger::warn;
            return this;
        }

        /**
         * Log refresh errors as error
         */
        public Builder withRefreshErrorLoggedAsError() {
            this.refreshErrorLogConsumer = Logger::error;
            return this;
        }

        /**
         * Log refresh errors using custom function
         */
        public Builder withRefreshErrorLoggedAs(RefreshErrorLogConsumer fn) {
            this.refreshErrorLogConsumer = fn;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(minBackOffDelay, maxBackOffDelay, minDelayBetweenRequests, minDelayOnEmptyResult,
                    timeoutAutoAdjustmentEnabled, timeoutAutoAdjustmentMargin,
                    refreshErrorLogConsumer);
        }
    }

    public interface RefreshErrorLogConsumer {
        void accept(Logger logger, String message, Throwable error);
    }
}
