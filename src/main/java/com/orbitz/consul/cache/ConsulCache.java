package com.orbitz.consul.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A cache structure that can provide an up-to-date read-only
 * map backed by consul data
 *
 * @param <V>
 */
public class ConsulCache<K, V> implements AutoCloseable {
    enum State {latent, starting, started, stopped }

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsulCache.class);

    private final AtomicReference<BigInteger> latestIndex;
    private final AtomicLong lastContact = new AtomicLong();
    private final AtomicBoolean isKnownLeader = new AtomicBoolean();
    private final AtomicReference<ConsulResponse.CacheResponseInfo> lastCacheInfo = new AtomicReference<>(null);
    private final AtomicReference<ImmutableMap<K, V>> lastResponse = new AtomicReference<>(null);
    private final AtomicReference<State> state = new AtomicReference<>(State.latent);
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final Scheduler scheduler;
    private final CopyOnWriteArrayList<Listener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final ReentrantLock listenersStartingLock = new ReentrantLock();
    private final Stopwatch stopWatch = Stopwatch.createUnstarted();

    private final Function<V, K> keyConversion;
    private final CallbackConsumer<V> callBackConsumer;
    private final ConsulResponseCallback<List<V>> responseCallback;
    private final ClientEventHandler eventHandler;
    private final CacheDescriptor cacheDescriptor;

    protected ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer,
            CacheConfig cacheConfig,
            ClientEventHandler eventHandler,
            CacheDescriptor cacheDescriptor,
            @Nullable BigInteger initialIndex) {

        this(keyConversion, callbackConsumer, cacheConfig, eventHandler, cacheDescriptor, createDefault(), initialIndex);
    }

    protected ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer,
            CacheConfig cacheConfig,
            ClientEventHandler eventHandler,
            CacheDescriptor cacheDescriptor,
            ScheduledExecutorService callbackScheduleExecutorService,
            @Nullable BigInteger initialIndex) {

        this(keyConversion, callbackConsumer, cacheConfig, eventHandler, cacheDescriptor,
          new ExternalScheduler(callbackScheduleExecutorService), initialIndex
        );
    }

    protected ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer,
            CacheConfig cacheConfig,
            ClientEventHandler eventHandler,
            CacheDescriptor cacheDescriptor,
            Scheduler callbackScheduler,
            @Nullable BigInteger initialIndex) {
        Validate.notNull(keyConversion, "keyConversion must not be null");
        Validate.notNull(callbackConsumer, "callbackConsumer must not be null");
        Validate.notNull(cacheConfig, "cacheConfig must not be null");
        Validate.notNull(eventHandler, "eventHandler must not be null");

        latestIndex = new AtomicReference<>(initialIndex);
        this.keyConversion = keyConversion;
        this.callBackConsumer = callbackConsumer;
        this.eventHandler = eventHandler;
        this.cacheDescriptor = cacheDescriptor;
        this.scheduler = callbackScheduler;

        this.responseCallback = new ConsulResponseCallback<List<V>>() {
            @Override
            public void onComplete(ConsulResponse<List<V>> consulResponse) {

                if (consulResponse.isKnownLeader()) {
                    if (!isRunning()) {
                        return;
                    }
                    long elapsedTime = stopWatch.elapsed(TimeUnit.MILLISECONDS);
                    updateIndex(consulResponse);
                    LOGGER.debug("Consul cache updated for {} (index={}), request duration: {} ms",
                            cacheDescriptor, latestIndex, elapsedTime);

                    ImmutableMap<K, V> full = convertToMap(consulResponse);

                    boolean changed = !full.equals(lastResponse.get());
                    eventHandler.cachePollingSuccess(cacheDescriptor, changed, elapsedTime);

                    if (changed) {
                        // changes
                        lastResponse.set(full);
                        // metadata changes
                        lastContact.set(consulResponse.getLastContact());
                        isKnownLeader.set(consulResponse.isKnownLeader());

                        withStartingLock(() -> {
                            for (Listener<K, V> l : listeners) {
                                try {
                                    l.notify(full);
                                } catch (RuntimeException e) {
                                    LOGGER.warn("ConsulCache Listener's notify method threw an exception.", e);
                                }
                            }
                            return null;
                        });
                    }

                    if (state.compareAndSet(State.starting, State.started)) {
                        initLatch.countDown();
                    }

                    Duration timeToWait = cacheConfig.getMinimumDurationBetweenRequests();
                    if ((consulResponse.getResponse() == null || consulResponse.getResponse().isEmpty()) &&
                            cacheConfig.getMinimumDurationDelayOnEmptyResult().compareTo(timeToWait) > 0) {
                        timeToWait = cacheConfig.getMinimumDurationDelayOnEmptyResult();
                    }
                    timeToWait = timeToWait.minusMillis(elapsedTime);

                    scheduler.schedule(ConsulCache.this::runCallback, timeToWait.toMillis(), TimeUnit.MILLISECONDS);

                } else {
                    onFailure(new ConsulException("Consul cluster has no elected leader"));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (!isRunning()) {
                    return;
                }
                eventHandler.cachePollingError(cacheDescriptor, throwable);
                long delayMs = computeBackOffDelayMs(cacheConfig);
                String message = String.format("Error getting response from consul for %s, will retry in %d %s",
                        cacheDescriptor, delayMs, TimeUnit.MILLISECONDS);

                cacheConfig.getRefreshErrorLoggingConsumer().accept(LOGGER, message, throwable);

                scheduler.schedule(ConsulCache.this::runCallback, delayMs, TimeUnit.MILLISECONDS);
            }
        };
    }

    private <T> T withStartingLock(Supplier<T> action) {
      boolean wasInStartingState = state.get() == State.starting;
      if (wasInStartingState) {
        listenersStartingLock.lock();
      }
      try {
        return action.get();
      }
      finally {
        if (wasInStartingState) {
          listenersStartingLock.unlock();
        }
      }
    }

    static long computeBackOffDelayMs(CacheConfig cacheConfig) {
        return cacheConfig.getMinimumBackOffDelay().toMillis() +
                Math.round(Math.random() * (cacheConfig.getMaximumBackOffDelay().minus(cacheConfig.getMinimumBackOffDelay()).toMillis()));
    }

    public void start() {
        checkState(state.compareAndSet(State.latent, State.starting),"Cannot transition from state %s to %s", state.get(), State.starting);
        eventHandler.cacheStart(cacheDescriptor);
        runCallback();
    }

    public void stop() {
        try {
            eventHandler.cacheStop(cacheDescriptor);
        } catch (RejectedExecutionException ree) {
            LOGGER.error("Unable to propagate cache stop event. ", ree);
        }

        State previous = state.getAndSet(State.stopped);
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        if (previous != State.stopped) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void runCallback() {
        if (isRunning()) {
            stopWatch.reset().start();
            callBackConsumer.consume(latestIndex.get(), responseCallback);
        }
    }

    private boolean isRunning() {
      State currentState = this.state.get();
      return currentState == State.started || currentState == State.starting;
    }

    public boolean awaitInitialized(long timeout, TimeUnit unit) throws InterruptedException {
        return initLatch.await(timeout, unit);
    }

    public ImmutableMap<K, V> getMap() {
        return lastResponse.get();
    }

    public ConsulResponse<ImmutableMap<K,V>> getMapWithMetadata() {
        return new ConsulResponse<>(lastResponse.get(), lastContact.get(), isKnownLeader.get(), latestIndex.get(), Optional.ofNullable(lastCacheInfo.get()));
    }

    @VisibleForTesting
    ImmutableMap<K, V> convertToMap(final ConsulResponse<List<V>> response) {
        if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
            return ImmutableMap.of();
        }
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        final Set<K> keySet = new HashSet<>();
        for (final V v : response.getResponse()) {
            final K key = keyConversion.apply(v);
            if (key != null) {
                if (!keySet.contains(key)) {
                    builder.put(key, v);
                } else {
                    LOGGER.warn("Duplicate service encountered. May differ by tags. Try using more specific tags? " + key.toString());
                }
            }
            keySet.add(key);
        }
        return builder.build();
    }

    private void updateIndex(ConsulResponse<List<V>> consulResponse) {
        if (consulResponse != null && consulResponse.getIndex() != null) {
          BigInteger previousIndex = this.latestIndex.getAndSet(consulResponse.getIndex());
          LOGGER.trace("Updated cache index from {} to {}", previousIndex, latestIndex.get());
        }
    }

    protected static QueryOptions watchParams(final BigInteger index, final int blockSeconds,
                                              QueryOptions queryOptions) {
        checkArgument(!queryOptions.getIndex().isPresent() && !queryOptions.getWait().isPresent(),
                "Index and wait cannot be overridden");

        ImmutableQueryOptions.Builder builder =  ImmutableQueryOptions.builder()
                .from(watchDefaultParams(index, blockSeconds))
                .token(queryOptions.getToken())
                .consistencyMode(queryOptions.getConsistencyMode())
                .near(queryOptions.getNear())
                .datacenter(queryOptions.getDatacenter());
        for (String tag : queryOptions.getTag()) {
            builder.addTag(tag);
        }
        return builder.build();
    }

    private static QueryOptions watchDefaultParams(final BigInteger index, final int blockSeconds) {
        if (index == null) {
            return QueryOptions.BLANK;
        } else {
            return QueryOptions.blockSeconds(blockSeconds, index).build();
        }
    }

    protected static Scheduler createDefault() {
        return new DefaultScheduler();
    }

    protected static Scheduler createExternal(ScheduledExecutorService executor) {
        return new ExternalScheduler(executor);
    }

    /**
     * passed in by creators to vary the content of the cached values
     *
     * @param <V>
     */
    @FunctionalInterface
    protected interface CallbackConsumer<V> {
        void consume(BigInteger index, ConsulResponseCallback<List<V>> callback);
    }

    /**
     * Implementers can register a listener to receive
     * a new map when it changes
     *
     * @param <V>
     */
    @FunctionalInterface
    public interface Listener<K, V> {
        void notify(Map<K, V> newValues);
    }

    public boolean addListener(Listener<K, V> listener) {
        return withStartingLock(() -> {
            boolean added = listeners.add(listener);
            if (this.state.get() == State.started) {
                try {
                    listener.notify(lastResponse.get());
                } catch (RuntimeException e) {
                    LOGGER.warn("ConsulCache Listener's notify method threw an exception.", e);
                }
            }
            return added;
          });
    }

    public List<Listener<K, V>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public boolean removeListener(Listener<K, V> listener) {
        return listeners.remove(listener);
    }

    @VisibleForTesting
    protected State getState() {
        return state.get();
    }

    protected static class Scheduler {
        public Scheduler(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        void schedule(Runnable r, long delay, TimeUnit unit) {
            executor.schedule(r, delay, unit);
        }

        void shutdownNow() {
            executor.shutdownNow();
        }

        private final ScheduledExecutorService executor;
    }

    private static class DefaultScheduler extends Scheduler {
        public DefaultScheduler() {
            super(Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                    .setNameFormat("consulCacheScheduledCallback-%d")
                    .setDaemon(true)
                    .build()));
        }
    }

    private static final class ExternalScheduler extends Scheduler {

        private ExternalScheduler(ScheduledExecutorService executor) {
            super(executor);
        }

        @Override
        public void shutdownNow() {
            // do nothing, since executor was externally created
        }
    }

    protected static void checkWatch(int networkReadMillis, int cacheWatchSeconds) {
        if (networkReadMillis <= TimeUnit.SECONDS.toMillis(cacheWatchSeconds)) {
            throw new IllegalArgumentException("Cache watchInterval="+ cacheWatchSeconds + "sec >= networkClientReadTimeout="
                + networkReadMillis + "ms. It can cause issues");
        }
    }
}
