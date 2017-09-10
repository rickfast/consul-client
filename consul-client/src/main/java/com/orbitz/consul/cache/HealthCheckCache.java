package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.option.QueryOptions;

import java.math.BigInteger;
import java.util.List;

public class HealthCheckCache extends ConsulCache<String, HealthCheck> {

    private HealthCheckCache(Function<HealthCheck, String> keyConversion, CallbackConsumer<HealthCheck> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    /**
     * Factory method to construct a string/{@link HealthCheck} map for a particular {@link com.orbitz.consul.model.State}.
     * <p/>
     * Keys will be the {@link HealthCheck#getCheckId()}.
     *
     * @param healthClient the {@link HealthClient}
     * @param state        the state fo filter checks
     * @return a cache object
     */
    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.orbitz.consul.model.State state,
            final int watchSeconds,
            final QueryOptions queryOptions,
            Function<HealthCheck, String> keyExtractor) {

        CallbackConsumer<HealthCheck> callbackConsumer = new CallbackConsumer<HealthCheck>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<HealthCheck>> callback) {
                QueryOptions params = watchParams(index, watchSeconds, queryOptions);
                healthClient.getChecksByState(state, params, callback);
            }
        };

        return new HealthCheckCache(keyExtractor, callbackConsumer);
    }

    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.orbitz.consul.model.State state,
            final int watchSeconds,
            final QueryOptions queryOptions) {

        Function<HealthCheck, String> keyExtractor = new Function<HealthCheck, String>() {
            @Override
            public String apply(HealthCheck input) {
                return input.getCheckId();
            }
        };

        return newCache(healthClient, state, watchSeconds, queryOptions, keyExtractor);
    }

    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.orbitz.consul.model.State state,
            final int watchSeconds) {
        return newCache(healthClient, state, watchSeconds, QueryOptions.BLANK);
    }

    public static HealthCheckCache newCache(final HealthClient healthClient, final com.orbitz.consul.model.State state) {
        return newCache(healthClient, state, 10);
    }

}
