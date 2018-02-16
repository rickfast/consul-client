package com.orbitz.consul.cache;

import com.google.common.net.HostAndPort;
import com.google.common.primitives.Ints;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.QueryOptions;

import java.util.function.Function;

public class ServiceHealthCache extends ConsulCache<ServiceHealthKey, ServiceHealth> {

    private ServiceHealthCache(Function<ServiceHealth, ServiceHealthKey> keyConversion,
                               CallbackConsumer<ServiceHealth> callbackConsumer,
                               CacheConfig cacheConfig,
                               ClientEventHandler eventHandler) {
        super(keyConversion, callbackConsumer, cacheConfig, eventHandler);
    }

    /**
     * Factory method to construct a string/{@link ServiceHealth} map for a particular service.
     * <p/>
     * Keys will be a {@link HostAndPort} object made up of the service's address/port combo
     *
     * @param healthClient the {@link HealthClient}
     * @param serviceName  the name of the service
     * @param passing      include only passing services?
     * @return a cache object
     */
    public static ServiceHealthCache newCache(
            final HealthClient healthClient,
            final String serviceName,
            final boolean passing,
            final int watchSeconds,
            final QueryOptions queryOptions,
            final Function<ServiceHealth, ServiceHealthKey> keyExtractor) {

        final CallbackConsumer<ServiceHealth> callbackConsumer = (index, callback) -> {
            QueryOptions params = watchParams(index, watchSeconds, queryOptions);
            if (passing) {
                healthClient.getHealthyServiceInstances(serviceName, params, callback);
            } else {
                healthClient.getAllServiceInstances(serviceName, params, callback);
            }
        };

        return new ServiceHealthCache(keyExtractor,
                callbackConsumer,
                healthClient.getConfig().getCacheConfig(),
                healthClient.getEventHandler());
    }

    public static ServiceHealthCache newCache(
            final HealthClient healthClient,
            final String serviceName,
            final boolean passing,
            final int watchSeconds,
            final QueryOptions queryOptions) {

        return newCache(healthClient, serviceName, passing, watchSeconds, queryOptions, ServiceHealthKey::fromServiceHealth);
    }
    
    public static ServiceHealthCache newCache(
            final HealthClient healthClient,
            final String serviceName,
            final boolean passing,
            final QueryOptions queryOptions,
            final int watchSeconds) {
        return newCache(healthClient, serviceName, passing, watchSeconds, queryOptions);
    }

    public static ServiceHealthCache newCache(final HealthClient healthClient, final String serviceName) {
        CacheConfig cacheConfig = healthClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(healthClient, serviceName, true, QueryOptions.BLANK, watchSeconds);
    }
}
