package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.QueryOptions;

import java.math.BigInteger;
import java.util.List;


public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(Function<Node, String> keyConversion, CallbackConsumer<Node> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {
        Function<Node, String> keyExtractor = new Function<Node, String>() {
            @Override
            public String apply(Node node) {
                return node.getNode();
            }
        };

        CallbackConsumer<Node> callbackConsumer = new CallbackConsumer<Node>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<Node>> callback) {
                catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback);
            }
        };

        return new NodesCatalogCache(keyExtractor, callbackConsumer);

    }

    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        return newCache(catalogClient, QueryOptions.BLANK, 10);
    }

}