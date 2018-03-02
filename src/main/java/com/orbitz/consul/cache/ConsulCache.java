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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final AtomicReference<BigInteger> latestIndex = new AtomicReference<>(null);
    private final AtomicLong lastContact = new AtomicLong();
    private final AtomicBoolean isKnownLeader = new AtomicBoolean();
    private final AtomicReference<ImmutableMap<K, V>> lastResponse = new AtomicReference<>(null);
    private final AtomicReference<State> state = new AtomicReference<>(State.latent);
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setDaemon(true).build());
    private final CopyOnWriteArrayList<Listener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final ReentrantLock listenersStartingLock = new ReentrantLock();
    private final Stopwatch stopWatch = Stopwatch.createUnstarted();

    private final Function<V, K> keyConversion;
    private final CallbackConsumer<V> callBackConsumer;
    private final ConsulResponseCallback<List<V>> responseCallback;
    private final ClientEventHandler eventHandler;

    ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer,
            CacheConfig cacheConfig,
            ClientEventHandler eventHandler) {

        this.keyConversion = keyConversion;
        this.callBackConsumer = callbackConsumer;
        this.eventHandler = eventHandler;

        this.responseCallback = new ConsulResponseCallback<List<V>>() {
            @Override
            public void onComplete(ConsulResponse<List<V>> consulResponse) {

                if (consulResponse.isKnownLeader()) {
                    if (!isRunning()) {
                        return;
                    }
                    Duration elapsedTime = stopWatch.elapsed();
                    updateIndex(consulResponse);
                    LOGGER.debug("Consul cache updated (index={}), request duration: {} ms",
                            latestIndex, elapsedTime.toMillis());

                    ImmutableMap<K, V> full = convertToMap(consulResponse);

                    boolean changed = !full.equals(lastResponse.get());
                    eventHandler.cachePollingSuccess(changed, elapsedTime);

                    if (changed) {
                        // changes
                        lastResponse.set(full);
                        // metadata changes
                        lastContact.set(consulResponse.getLastContact());
                        isKnownLeader.set(consulResponse.isKnownLeader());
                    }

                    if (changed) {
                        Boolean locked = false;
                        if (state.get() == State.starting) {
                            listenersStartingLock.lock();
                            locked = true;
                        }
                        try {
                            for (Listener<K, V> l : listeners) {
                                l.notify(full);
                            }
                        }
                        finally {
                            if (locked) {
                                listenersStartingLock.unlock();
                            }
                        }
                    }

                    if (state.compareAndSet(State.starting, State.started)) {
                        initLatch.countDown();
                    }

                    Duration timeToWait = cacheConfig.getMinimumDurationBetweenRequests().minus(elapsedTime);
                    if (timeToWait.isNegative() || timeToWait.isZero()) {
                        runCallback();
                    } else {
                        executorService.schedule(ConsulCache.this::runCallback,
                                timeToWait.toMillis(), TimeUnit.MILLISECONDS);
                    }

                } else {
                    onFailure(new ConsulException("Consul cluster has no elected leader"));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (!isRunning()) {
                    return;
                }
                eventHandler.cachePollingError(throwable);

                String message = String.format("Error getting response from consul. will retry in %d %s",
                        cacheConfig.getBackOffDelay().toMillis(), TimeUnit.MILLISECONDS);

                if (cacheConfig.isRefreshErrorLoggedAsWarning()) {
                    LOGGER.warn(message, throwable);
                } else {
                    LOGGER.error(message, throwable);
                }

                executorService.schedule(ConsulCache.this::runCallback,
                        cacheConfig.getBackOffDelay().toMillis(), TimeUnit.MILLISECONDS);
            }
        };
    }

    public void start() {
        checkState(state.compareAndSet(State.latent, State.starting),"Cannot transition from state %s to %s", state.get(), State.starting);
        eventHandler.cacheStart();
        runCallback();
    }

    public void stop() {
        eventHandler.cacheStop();
        State previous = state.getAndSet(State.stopped);
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        if (previous != State.stopped) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    private void runCallback() {
        if (isRunning()) {
            stopWatch.reset().start();
            callBackConsumer.consume(latestIndex.get(), responseCallback);
        }
    }

    private boolean isRunning() {
        return state.get() == State.started || state.get() == State.starting;
    }

    public boolean awaitInitialized(long timeout, TimeUnit unit) throws InterruptedException {
        return initLatch.await(timeout, unit);
    }

    public ImmutableMap<K, V> getMap() {
        return lastResponse.get();
    }

    public ConsulResponse<ImmutableMap<K,V>> getMapWithMetadata() {
        return new ConsulResponse<>(lastResponse.get(), lastContact.get(), isKnownLeader.get(), latestIndex.get());
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
            this.latestIndex.set(consulResponse.getIndex());
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

    /**
     * passed in by creators to vary the content of the cached values
     *
     * @param <V>
     */
    protected interface CallbackConsumer<V> {
        void consume(BigInteger index, ConsulResponseCallback<List<V>> callback);
    }

    /**
     * Implementers can register a listener to receive
     * a new map when it changes
     *
     * @param <V>
     */
    public interface Listener<K, V> {
        void notify(Map<K, V> newValues);
    }

    public boolean addListener(Listener<K, V> listener) {
        Boolean locked = false;
        boolean added;
        if (state.get() == State.starting) {
            listenersStartingLock.lock();
            locked = true;
        }
        try {
            added = listeners.add(listener);
            if (state.get() == State.started) {
                listener.notify(lastResponse.get());
            }
        }
        finally {
            if (locked) {
                listenersStartingLock.unlock();
            }
        }
        return added;
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
}
