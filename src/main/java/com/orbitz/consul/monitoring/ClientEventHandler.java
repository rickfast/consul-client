package com.orbitz.consul.monitoring;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.cache.CacheDescriptor;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import okhttp3.Request;
import retrofit2.Response;

public class ClientEventHandler {

    private static final ScheduledExecutorService EVENT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("event-executor-%s").setDaemon(true).build());

    private final String clientName;
    private final ClientEventCallback callback;

    public ClientEventHandler(String clientName, ClientEventCallback callback) {
        this.clientName = clientName;
        this.callback = callback;
    }

    public void httpRequestSuccess(Request request, Response<?> response) {
        EVENT_EXECUTOR.submit(() -> {
            callback.onHttpRequestSuccess(clientName, request.method(), request.url().query());
            callback.onHttpRequestSuccess(clientName, request.method(), request.url().encodedPath(), request.url().query(), response.code());
        });
    }

    public void httpRequestInvalid(Request request, Response<?> response, Throwable throwable) {
        EVENT_EXECUTOR.submit(() -> {
            callback.onHttpRequestInvalid(clientName, request.method(), request.url().query(), throwable);
            callback.onHttpRequestInvalid(clientName, request.method(), request.url().encodedPath(), request.url().query(), response.code(),
                throwable);
        });
    }

    public void httpRequestFailure(Request request, Throwable throwable) {
        EVENT_EXECUTOR.submit(() -> {
            callback.onHttpRequestFailure(clientName, request.method(), request.url().query(), throwable);
            callback.onHttpRequestFailure(clientName, request.method(), request.url().encodedPath(), request.url().query(), throwable);
        });
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
