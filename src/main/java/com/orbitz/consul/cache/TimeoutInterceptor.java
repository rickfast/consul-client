package com.orbitz.consul.cache;

import com.google.common.base.Strings;
import com.orbitz.consul.config.CacheConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimeoutInterceptor implements Interceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeoutInterceptor.class);

    private CacheConfig config;

    public TimeoutInterceptor(CacheConfig config) {
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int readTimeout = chain.readTimeoutMillis();

        // Snapshot might be very large. Timeout should be adjusted for this endpoint.
        if (request.url().encodedPath().contains("snapshot")) {
            readTimeout = (int) Duration.ofHours(1).toMillis();
        }
        else if (config.isTimeoutAutoAdjustmentEnabled()) {
            String waitQuery = request.url().queryParameter("wait");
            Duration waitDuration = parseWaitQuery(waitQuery);
            if (waitDuration != null) {
                int waitDurationMs = (int) waitDuration.toMillis();
                int readTimeoutConfigMargin = (int) config.getTimeoutAutoAdjustmentMargin().toMillis();

                // According to https://www.consul.io/api/index.html#blocking-queries
                // A small random amount of additional wait time is added to the supplied maximum wait time by consul
                // agent to spread out the wake up time of any concurrent requests.
                // This adds up to (wait / 16) additional time to the maximum duration.
                int readTimeoutRequiredMargin = (int) Math.ceil((double)(waitDurationMs) / 16);

                readTimeout = waitDurationMs + readTimeoutRequiredMargin + readTimeoutConfigMargin;
            }
        }

        return chain
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .proceed(request);
    }

    private Duration parseWaitQuery(String query) {
        if (Strings.isNullOrEmpty(query)) {
            return null;
        }

        Duration wait = null;
        try {
            if (query.contains("m")) {
                wait = Duration.ofMinutes(Integer.valueOf(query.replace("m","")));
            } else if (query.contains("s")) {
                wait = Duration.ofSeconds(Integer.valueOf(query.replace("s","")));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while extracting wait duration from query parameters: %s", query));
        }
        return wait;
    }
}
