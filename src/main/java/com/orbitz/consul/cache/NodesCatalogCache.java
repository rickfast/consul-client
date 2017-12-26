package com.orbitz.consul.cache;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.QueryOptions;

import java.util.function.Function;


public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(Function<Node, String> keyConversion, CallbackConsumer<Node> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {

        return new NodesCatalogCache(Node::getNode, (index, callback) ->
                catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback));

    }

    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        return newCache(catalogClient, QueryOptions.BLANK, 10);
    }

}