package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A cache structure that can provide an up-to-date read-only
 * map backed by consul data
 *
 * @param <V>
 */
public class ConsulCache<K, V> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsulCache.class);

    private final AtomicReference<BigInteger> latestIndex = new AtomicReference<BigInteger>(null);
    private final AtomicReference<ImmutableMap<K, V>> lastState = new AtomicReference<ImmutableMap<K, V>>(ImmutableMap.<K, V>of());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final CopyOnWriteArrayList<Listener<K, V>> listeners = new CopyOnWriteArrayList<Listener<K, V>>();

    private final Function<V, K> keyConversion;
    private final CallbackConsumer<V> callBackConsumer;
    private final ConsulResponseCallback<List<V>> responseCallback;

    ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer) {
        this(keyConversion, callbackConsumer, 10, TimeUnit.SECONDS);
    }

    ConsulCache(
            Function<V, K> keyConversion,
            CallbackConsumer<V> callbackConsumer,
            final long backoffDelayQty,
            final TimeUnit backoffDelayUnit) {

        this.keyConversion = keyConversion;
        this.callBackConsumer = callbackConsumer;

        this.responseCallback = new ConsulResponseCallback<List<V>>() {
            @Override
            public void onComplete(ConsulResponse<List<V>> consulResponse) {

                updateIndex(consulResponse);
                ImmutableMap<K, V> full = convertToMap(consulResponse);

                boolean changed = !full.equals(lastState.get());
                if (changed) {
                    // changes
                    lastState.set(full);
                }
                if (initialized.compareAndSet(false, true)) {
                    initLatch.countDown();
                }

                if (changed) {
                    for (Listener<K, V> l : listeners) {
                        l.notify(full);
                    }
                }
                runCallback();
            }

            @Override
            public void onFailure(Throwable throwable) {

                LOGGER.error("Error getting response from consul. will retry in {} {}", backoffDelayQty, backoffDelayUnit, throwable);

                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        runCallback();
                    }
                }, backoffDelayQty, backoffDelayUnit);
            }
        };
    }

    public void start() throws Exception {
        runCallback();
    }

    private void runCallback() {
        callBackConsumer.consume(latestIndex.get(), responseCallback);
    }

    public boolean awaitInitialized(long timeout, TimeUnit unit) throws InterruptedException {
        return initLatch.await(timeout, unit);
    }

    public ImmutableMap<K, V> getMap() {
        return lastState.get();
    }

    private ImmutableMap<K, V> convertToMap(ConsulResponse<List<V>> response) {
        if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (V v : response.getResponse()) {
            K key = keyConversion.apply(v);
            if (key != null) {
                builder.put(key, v);
            }
        }
        return builder.build();
    }

    private void updateIndex(ConsulResponse<List<V>> consulResponse) {
        if (consulResponse != null && consulResponse.getIndex() != null) {
            this.latestIndex.set(consulResponse.getIndex());
        }
    }

    protected static QueryOptions watchParams(BigInteger index, int blockSeconds) {
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
        boolean added = listeners.add(listener);
        if (initialized.get()) {
            listener.notify(lastState.get());
        }
        return added;
    }

    public boolean removeListener(Listener<K, V> listener) {
        return listeners.remove(listener);
    }

}
