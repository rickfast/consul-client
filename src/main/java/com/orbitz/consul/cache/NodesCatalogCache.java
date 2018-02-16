package com.orbitz.consul.cache;

import com.google.common.primitives.Ints;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.QueryOptions;

import java.util.function.Function;

public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(Function<Node, String> keyConversion,
                              CallbackConsumer<Node> callbackConsumer,
                              CacheConfig cacheConfig,
                              ClientEventHandler eventHandler) {
        super(keyConversion, callbackConsumer, cacheConfig, eventHandler);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {

        final CallbackConsumer<Node> callbackConsumer = (index, callback) ->
                catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback);

        return new NodesCatalogCache(Node::getNode,
                callbackConsumer,
                catalogClient.getConfig().getCacheConfig(),
                catalogClient.getEventHandler());
    }

    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, QueryOptions.BLANK, watchSeconds);
    }

}