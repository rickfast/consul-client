package com.orbitz.consul.monitoring;

import com.orbitz.consul.cache.CacheDescriptor;

import java.time.Duration;

public interface ClientEventCallback {

    default void onHttpRequestSuccess(String clientName, String method, String queryString) { }

    default void onHttpRequestFailure(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestInvalid(String clientName, String method, String queryString, Throwable throwable) { }

    default void onCacheStart(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCacheStop(String clientName, CacheDescriptor cacheDescriptor) { }

    default void onCachePollingError(String clientName, CacheDescriptor cacheDescriptor, Throwable throwable) { }

    default void onCachePollingSuccess(String clientName, CacheDescriptor cacheDescriptor, boolean withNotification, Duration duration) { }
}
