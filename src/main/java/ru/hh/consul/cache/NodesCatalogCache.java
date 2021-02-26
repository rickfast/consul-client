package ru.hh.consul.cache;

import com.google.common.primitives.Ints;
import ru.hh.consul.CatalogClient;
import ru.hh.consul.config.CacheConfig;
import ru.hh.consul.model.health.Node;
import ru.hh.consul.option.QueryOptions;

import java.util.concurrent.ScheduledExecutorService;

public class NodesCatalogCache extends ConsulCache<String, Node> {

    private NodesCatalogCache(CatalogClient catalogClient,
                              QueryOptions queryOptions,
                              int watchSeconds,
                              Scheduler callbackScheduler) {
        super(Node::getNode,
              (index, callback) -> catalogClient.getNodes(watchParams(index, watchSeconds, queryOptions), callback),
              catalogClient.getConfig().getCacheConfig(),
              catalogClient.getEventHandler(),
              new CacheDescriptor("catalog.nodes"),
              callbackScheduler);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, scheduler);
    }

    public static NodesCatalogCache newCache(
            final CatalogClient catalogClient,
            final QueryOptions queryOptions,
            final int watchSeconds) {
        return new NodesCatalogCache(catalogClient, queryOptions, watchSeconds, createDefault());
    }

    public static NodesCatalogCache newCache(final CatalogClient catalogClient) {
        CacheConfig cacheConfig = catalogClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(catalogClient, QueryOptions.BLANK, watchSeconds);
    }

}
