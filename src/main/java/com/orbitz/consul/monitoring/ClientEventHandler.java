package com.orbitz.consul.monitoring;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.cache.CacheDescriptor;
import okhttp3.Request;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientEventHandler {

    private static final ScheduledExecutorService EVENT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("event-executor-%s").setDaemon(true).build());

    private final String clientName;
    private final ClientEventCallback callback;

    public ClientEventHandler(String clientName, ClientEventCallback callback) {
        this.clientName = clientName;
        this.callback = callback;
    }

    public void httpRequestSuccess(Request request) {
        EVENT_EXECUTOR.submit(() -> callback.onHttpRequestSuccess(clientName, request.method(), request.url().query()));
    }

    public void httpRequestInvalid(Request request, Throwable throwable) {
        EVENT_EXECUTOR.submit(() ->
                callback.onHttpRequestInvalid(clientName, request.method(), request.url().query(), throwable));
    }

    public void httpRequestFailure(Request request, Throwable throwable) {
        EVENT_EXECUTOR.submit(() ->
                callback.onHttpRequestFailure(clientName, request.method(), request.url().query(), throwable));
    }

    public void cacheStart(CacheDescriptor cacheDescriptor) {
        EVENT_EXECUTOR.submit(() -> callback.onCacheStart(clientName, cacheDescriptor));
    }

    public void cacheStop(CacheDescriptor cacheDescriptor) {
        EVENT_EXECUTOR.submit(() -> callback.onCacheStop(clientName, cacheDescriptor));
    }

    public void cachePollingError(CacheDescriptor cacheDescriptor, Throwable throwable) {
        EVENT_EXECUTOR.submit(() -> callback.onCachePollingError(clientName, cacheDescriptor, throwable));
    }

    public void cachePollingSuccess(CacheDescriptor cacheDescriptor, boolean withNotification, Duration duration) {
        EVENT_EXECUTOR.submit(() -> callback.onCachePollingSuccess(clientName, cacheDescriptor, withNotification, duration));
    }

    public void cachePollingSuccess(CacheDescriptor cacheDescriptor, boolean withNotification, long duration) {
        EVENT_EXECUTOR.submit(() -> callback.onCachePollingSuccess(clientName, cacheDescriptor, withNotification, Duration.of(duration, ChronoUnit.MILLIS)));
    }

    public void stop() {
        EVENT_EXECUTOR.shutdownNow();
    }

}
