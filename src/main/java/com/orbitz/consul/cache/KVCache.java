package com.orbitz.consul.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.config.CacheConfig;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.option.QueryOptions;

import java.util.function.Function;

public class KVCache extends ConsulCache<String, Value> {

    private KVCache(Function<Value, String> keyConversion,
                    ConsulCache.CallbackConsumer<Value> callbackConsumer,
                    CacheConfig cacheConfig,
                    ClientEventHandler eventHandler) {
        super(keyConversion, callbackConsumer, cacheConfig, eventHandler);
    }

    @VisibleForTesting
    static Function<Value, String> getKeyExtractorFunction(final String rootPath) {
        return input -> {
            Preconditions.checkNotNull(input, "Input to key extractor is null");
            Preconditions.checkNotNull(input.getKey(), "Input to key extractor has no key");

            if (rootPath.equals(input.getKey())) {
                return "";
            }
            int lastSlashIndex = rootPath.lastIndexOf("/");
            if (lastSlashIndex >= 0) {
                return input.getKey().substring(lastSlashIndex+1);
            }
            return input.getKey();
        };
    }

    public static KVCache newCache(
            final KeyValueClient kvClient,
            final String rootPath,
            final int watchSeconds,
            final QueryOptions queryOptions) {

        final String keyPath = prepareRootPath(rootPath);

        final Function<Value, String> keyExtractor = getKeyExtractorFunction(keyPath);

        final ConsulCache.CallbackConsumer<Value> callbackConsumer = (index, callback) -> {
            QueryOptions params = watchParams(index, watchSeconds, queryOptions);
            kvClient.getValues(keyPath, params, callback);
        };

        return new KVCache(keyExtractor,
                callbackConsumer,
                kvClient.getConfig().getCacheConfig(),
                kvClient.getEventHandler());
    }

    @VisibleForTesting
    static String prepareRootPath(String rootPath) {
        return rootPath.startsWith("/") ? rootPath.substring(1) : rootPath;
    }

    /**
     * Factory method to construct a String/{@link Value} map.
     *
     * @param kvClient the {@link KeyValueClient} to use
     * @param rootPath the root path (will be stripped from keys in the cache)
     * @param watchSeconds how long to tell the Consul server to wait for new values (note that
     *                     if this is 60 seconds or more, the client's read timeout will need
     *                     to be increased as well)
     * @return the cache object
     */
    public static KVCache newCache(
            final KeyValueClient kvClient,
            final String rootPath,
            final int watchSeconds) {
        return newCache(kvClient, rootPath, watchSeconds, QueryOptions.BLANK);
    }

    /**
     * Factory method to construct a String/{@link Value} map with a 10 second
     * block interval
     *
     * @param kvClient the {@link KeyValueClient} to use
     * @param rootPath the root path
     * @return the cache object
     */
    public static KVCache newCache(final KeyValueClient kvClient, final String rootPath) {
        CacheConfig cacheConfig = kvClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(kvClient, rootPath, watchSeconds);
    }
}
