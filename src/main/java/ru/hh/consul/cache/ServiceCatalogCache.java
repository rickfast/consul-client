package ru.hh.consul.cache;

import java.math.BigInteger;
import ru.hh.consul.CatalogClient;
import ru.hh.consul.config.CacheConfig;
import ru.hh.consul.model.catalog.CatalogService;
import ru.hh.consul.option.QueryOptions;
import java.util.concurrent.ScheduledExecutorService;

public class ServiceCatalogCache extends ConsulCache<String, CatalogService> {

    private ServiceCatalogCache(CatalogClient catalogClient,
                                String serviceName,
                                QueryOptions queryOptions,
                                int watchSeconds,
                                Scheduler callbackScheduler,
                                BigInteger initialIndex) {

        super(CatalogService::getServiceId,
            (index, callback) -> catalogClient.getService(serviceName, watchParams(index, watchSeconds, queryOptions), callback),
            catalogClient.getConfig().getCacheConfig(),
            catalogClient.getEventHandler(),
            new CacheDescriptor("catalog.service", serviceName),
            callbackScheduler,
            initialIndex);
    }

    public static ServiceCatalogCache newCache(
            final CatalogClient catalogClient,
            final String serviceName,
            final QueryOptions queryOptions,
            final int watchSeconds,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, scheduler, null);
    }

    public static ServiceCatalogCache newCache(
            CatalogClient catalogClient,
            String serviceName,
            QueryOptions queryOptions,
            int watchSeconds,
            BigInteger initialIndex,
            ScheduledExecutorService callbackExecutorService) {

      Scheduler scheduler = createExternal(callbackExecutorService);
      return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, scheduler, initialIndex);
    }

    public static ServiceCatalogCache newCache(
            final CatalogClient catalogClient,
            final String serviceName,
            final QueryOptions queryOptions,
            final int watchSeconds) {

        return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, createDefault(), null);
    }

    public static ServiceCatalogCache newCache(
            CatalogClient catalogClient,
            String serviceName,
            QueryOptions queryOptions,
            int watchSeconds,
            BigInteger initialIndex) {

      return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, createDefault(), initialIndex);
    }

    @Deprecated
    public static ServiceCatalogCache newCache(final CatalogClient catalogClient, final String serviceName) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Math.toIntExact(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, serviceName, QueryOptions.BLANK, watchSeconds);
    }
}
