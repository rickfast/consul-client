package com.orbitz.consul.cache;

import com.google.common.primitives.Ints;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.QueryOptions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ServiceCatalogCache extends ConsulCache<String, CatalogService> {

    private ServiceCatalogCache(Function<CatalogService, String> keyConversion,
                                CallbackConsumer<CatalogService> callbackConsumer,
                                CacheConfig cacheConfig,
                                ClientEventHandler eventHandler) {
        super(keyConversion, callbackConsumer, cacheConfig, eventHandler);
    }

    public static ServiceCatalogCache newCache(
            final CatalogClient catalogClient,
            final String serviceName,
            final QueryOptions queryOptions,
            final int watchSeconds) {

        final CallbackConsumer<CatalogService> callbackConsumer = (index, callback) ->
                catalogClient.getService(serviceName, watchParams(index, watchSeconds, queryOptions), callback);

        return new ServiceCatalogCache(CatalogService::getServiceId,
                callbackConsumer,
                catalogClient.getConfig().getCacheConfig(),
                catalogClient.getEventHandler());
    }

    public static ServiceCatalogCache newCache(final CatalogClient catalogClient, final String serviceName) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, serviceName, QueryOptions.BLANK, watchSeconds);
    }
}