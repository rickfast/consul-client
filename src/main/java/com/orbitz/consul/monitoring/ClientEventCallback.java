package com.orbitz.consul.monitoring;

public interface ClientEventCallback {

    default void onHttpRequestSuccess(String clientName, String method, String queryString) { }

    default void onHttpRequestFailure(String clientName, String method, String queryString, Throwable throwable) { }

    default void onHttpRequestInvalid(String clientName, String method, String queryString, Throwable throwable) { }
}
