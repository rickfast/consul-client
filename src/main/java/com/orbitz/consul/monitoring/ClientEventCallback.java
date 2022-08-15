package com.orbitz.consul.monitoring;

import com.orbitz.consul.cache.CacheDescriptor;

import java.time.Duration;

public interface ClientEventCallback {

    /**
     * @deprecated use {@link #onHttpRequestSuccess(String, String, String, String, int)} instead.
     */
    @Deprecated(forRemoval = true)
    default void onHttpRequestSuccess(String clientName, String method, String queryString) { }

    default void onHttpRequestSuccess(String clientName, String method, String path, String queryString, int responseCode) { }

    /**
     * @deprecated use {@link #onHttpRequestFailure(String, String, String, String, Throwable)} instead.
     */
    @Deprecated(forRemoval = true)
    default void onHttpRequestFailure(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestFailure(String clientName, String method, String path, String queryString, Throwable throwable) { }

    /**
     * @deprecated use {@link #onHttpRequestInvalid(String, String, String, String, int, Throwable)} instead.
     */
    @Deprecated(forRemoval = true)
    default void onHttpRequestInvalid(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestInvalid(String clientName, String method, String path, String queryString, int responseCode, Throwable throwable) { }

    default void onCacheStart(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCacheStop(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCachePollingError(String clientName, CacheDescriptor cacheDescriptor, Throwable throwable) { }

    default void onCachePollingSuccess(String clientName, CacheDescriptor cacheDescriptor, boolean withNotification, Duration duration) { }
}
