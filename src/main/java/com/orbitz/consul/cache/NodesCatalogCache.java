package com.orbitz.consul.cache;

import com.google.common.primitives.Ints;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.QueryOptions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(Function<Node, String> keyConversion,
                              CallbackConsumer<Node> callbackConsumer,
                              CacheConfig cacheConfig,
                              ClientEventHandler eventHandler,
                              ScheduledExecutorService callbackExecutorService) {
        super(keyConversion, callbackConsumer, cacheConfig, eventHandler, new CacheDescriptor("catalog.nodes"), callbackExecutorService);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds,
            final ScheduledExecutorService callbackExecutorService) {

        final CallbackConsumer<Node> callbackConsumer = (index, callback) ->
                catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback);

        return new NodesCatalogCache(Node::getNode,
                callbackConsumer,
                catalogClient.getConfig().getCacheConfig(),
                catalogClient.getEventHandler(),
                callbackExecutorService);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {
            return newCache(catalogClient, queryOptions, watchSeconds, createDefault());
    }

    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, QueryOptions.BLANK, watchSeconds);
    }

}