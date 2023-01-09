package com.orbitz.consul.monitoring;

import com.orbitz.consul.cache.CacheDescriptor;

import java.time.Duration;

public interface ClientEventCallback {

    /**
     * @deprecated use {@link #onHttpRequestSuccess(String, String, String, String, int)} instead.
     */
    @Deprecated
    default void onHttpRequestSuccess(String clientName, String method, String queryString) { }

    default void onHttpRequestSuccess(String clientName, String method, String path, String queryString, int responseCode) {
        // Kept for compatibility at the moment
        onHttpRequestSuccess(clientName, method, queryString);
    }

    /**
     * @deprecated use {@link #onHttpRequestFailure(String, String, String, String, Throwable)} instead.
     */
    @Deprecated
    default void onHttpRequestFailure(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestFailure(String clientName, String method, String path, String queryString, Throwable throwable) {
        // Kept for compatibility at the moment
        onHttpRequestFailure(clientName, method, queryString, throwable);
    }

    /**
     * @deprecated use {@link #onHttpRequestInvalid(String, String, String, String, int, Throwable)} instead.
     */
    @Deprecated
    default void onHttpRequestInvalid(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestInvalid(String clientName, String method, String path, String queryString, int responseCode, Throwable throwable) {
        // Kept for compatibility at the moment
        onHttpRequestInvalid(clientName, method, queryString, throwable);
    }

    default void onCacheStart(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCacheStop(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCachePollingError(String clientName, CacheDescriptor cacheDescriptor, Throwable throwable) { }

    default void onCachePollingSuccess(String clientName, CacheDescriptor cacheDescriptor, boolean withNotification, Duration duration) { }
}
