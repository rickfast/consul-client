package com.orbitz.consul.monitoring;

import java.time.Duration;

public interface ClientEventCallback {

    default void onHttpRequestSuccess(String clientName, String method, String queryString) { }

    default void onHttpRequestFailure(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestInvalid(String clientName, String method, String queryString, Throwable throwable) { }

    default void onCacheStart(String clientName) { }

    default void onCacheStop(String clientName) { }

    default void onCachePollingError(String clientName, Throwable throwable) { }

    default void onCachePollingSuccess(String clientName, boolean withNotification, Duration duration) { }
}
