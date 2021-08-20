package com.orbitz.consul.cache;

import com.google.common.primitives.Ints;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.option.QueryOptions;
import java.util.concurrent.ScheduledExecutorService;

public class ServiceCatalogCache extends ConsulCache<String, CatalogService> {

    private ServiceCatalogCache(CatalogClient catalogClient,
                                String serviceName,
                                QueryOptions queryOptions,
                                int watchSeconds,
                                Scheduler callbackScheduler) {

        super(CatalogService::getServiceId,
            (index, callback) -> catalogClient.getService(serviceName, watchParams(index, watchSeconds, queryOptions), callback),
            catalogClient.getConfig().getCacheConfig(),
            catalogClient.getEventHandler(),
            new CacheDescriptor("catalog.service", serviceName),
            callbackScheduler);
    }

    public static ServiceCatalogCache newCache(
            final CatalogClient catalogClient,
            final String serviceName,
            final QueryOptions queryOptions,
            final int watchSeconds,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, scheduler);
    }

    public static ServiceCatalogCache newCache(
            final CatalogClient catalogClient,
            final String serviceName,
            final QueryOptions queryOptions,
            final int watchSeconds) {

        return new ServiceCatalogCache(catalogClient, serviceName, queryOptions, watchSeconds, createDefault());
    }

    public static ServiceCatalogCache newCache(final CatalogClient catalogClient, final String serviceName) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, serviceName, QueryOptions.BLANK, watchSeconds);
    }
}