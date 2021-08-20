package com.orbitz.consul.cache;

import com.google.common.primitives.Ints;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.QueryOptions;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(CatalogClient catalogClient,
                              QueryOptions queryOptions,
                              int watchSeconds,
                              Scheduler callbackScheduler,
                              BigInteger initialIndex) {
        super(Node::getNode,
              (index, callback) -> {
                  checkWatch(catalogClient.getNetworkTimeoutConfig().getClientReadTimeoutMillis(), watchSeconds);
                  catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback);
              },
              catalogClient.getConfig().getCacheConfig(),
              catalogClient.getEventHandler(),
              new CacheDescriptor("catalog.nodes"),
              callbackScheduler,
              initialIndex);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, scheduler, null);
    }

    public static NodesCatalogCache newCache(
            CatalogClient catalogClient,
            QueryOptions queryOptions,
            int watchSeconds,
            BigInteger initialIndex,
            ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, scheduler, initialIndex);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, createDefault(), null);
    }

    public static NodesCatalogCache newCache(
        CatalogClient catalogClient,
        QueryOptions queryOptions,
        int watchSeconds,
        BigInteger initialIndex) {
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, createDefault(), initialIndex);
    }

    @Deprecated
    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, QueryOptions.BLANK, watchSeconds);
    }

}