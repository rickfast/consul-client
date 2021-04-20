package ru.hh.consul.cache;

import ru.hh.consul.HealthClient;
import ru.hh.consul.config.CacheConfig;
import ru.hh.consul.model.health.ServiceHealth;
import ru.hh.consul.option.QueryOptions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import ru.hh.consul.util.Address;

public class ServiceHealthCache extends ConsulCache<ServiceHealthKey, ServiceHealth> {

    private ServiceHealthCache(HealthClient healthClient,
                               String serviceName,
                               boolean passing,
                               int watchSeconds,
                               QueryOptions queryOptions,
                               Function<ServiceHealth, ServiceHealthKey> keyExtractor,
                               Scheduler callbackScheduler) {
        super(keyExtractor,
              (index, callback) -> {
                  QueryOptions params = watchParams(index, watchSeconds, queryOptions);
                  if (passing) {
                      healthClient.getHealthyServiceInstances(serviceName, params, callback);
                  } else {
                      healthClient.getAllServiceInstances(serviceName, params, callback);
                  }
              },
              healthClient.getConfig().getCacheConfig(),
              healthClient.getEventHandler(),
              new CacheDescriptor("health.service", serviceName),
              callbackScheduler);
    }

    /**
     * Factory method to construct a string/{@link ServiceHealth} map for a particular service.
     * <p/>
     * Keys will be a {@link Address} object made up of the service's address/port combo
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
            final Function<ServiceHealth, ServiceHealthKey> keyExtractor,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new ServiceHealthCache(healthClient, serviceName, passing, watchSeconds, queryOptions, keyExtractor, scheduler);
    }

    public static ServiceHealthCache newCache(
            final HealthClient healthClient,
            final String serviceName,
            final boolean passing,
            final int watchSeconds,
            final QueryOptions queryOptions,
            final Function<ServiceHealth, ServiceHealthKey> keyExtractor) {

        return new ServiceHealthCache(healthClient, serviceName, passing, watchSeconds, queryOptions, keyExtractor, createDefault());
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
        int watchSeconds = Math.toIntExact(cacheConfig.getWatchDuration().getSeconds());
        return newCache(healthClient, serviceName, true, QueryOptions.BLANK, watchSeconds);
    }
}
