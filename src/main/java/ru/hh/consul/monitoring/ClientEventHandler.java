package ru.hh.consul.monitoring;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import ru.hh.consul.cache.CacheDescriptor;
import okhttp3.Request;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ClientEventHandler {

    private static final ScheduledExecutorService EVENT_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
      new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(@NotNull Runnable r) {
          Thread thread = new Thread(r, "event-executor-" + counter.getAndIncrement());
          thread.setDaemon(true);
          return thread;
        }
      });

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

    public void stop() {
        EVENT_EXECUTOR.shutdownNow();
    }

}
