package ru.hh.consul.cache;

import java.math.BigInteger;
import java.util.Objects;
import ru.hh.consul.KeyValueClient;
import ru.hh.consul.config.CacheConfig;
import ru.hh.consul.model.kv.Value;
import ru.hh.consul.option.QueryOptions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class KVCache extends ConsulCache<String, Value> {

    private KVCache(KeyValueClient kvClient,
                    String rootPath,
                    String keyPath,
                    int watchSeconds,
                    QueryOptions queryOptions,
                    Scheduler callbackScheduler,
                    BigInteger initialIndex) {
        super(getKeyExtractorFunction(keyPath),
            (index, callback) -> {
                QueryOptions params = watchParams(index, watchSeconds, queryOptions);
                kvClient.getValues(keyPath, params, callback);
            },
            kvClient.getConfig().getCacheConfig(),
            kvClient.getEventHandler(),
            new CacheDescriptor("keyvalue", rootPath),
            callbackScheduler,
            initialIndex);
    }

    static Function<Value, String> getKeyExtractorFunction(final String rootPath) {
        return input -> {
            Objects.requireNonNull(input, "Input to key extractor is null");
            Objects.requireNonNull(input.getKey(), "Input to key extractor has no key");

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
            final QueryOptions queryOptions,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler scheduler = createExternal(callbackExecutorService);
        return new KVCache(kvClient, rootPath, prepareRootPath(rootPath), watchSeconds, queryOptions, scheduler, null);
    }

    public static KVCache newCache(
            KeyValueClient kvClient,
            String rootPath,
            int watchSeconds,
            QueryOptions queryOptions,
            BigInteger initialIndex,
            ScheduledExecutorService callbackExecutorService) {

      Scheduler scheduler = createExternal(callbackExecutorService);
      return new KVCache(kvClient, rootPath, prepareRootPath(rootPath), watchSeconds, queryOptions, scheduler, initialIndex);
    }

    public static KVCache newCache(
            final KeyValueClient kvClient,
            final String rootPath,
            final int watchSeconds,
            final QueryOptions queryOptions) {
        return new KVCache(kvClient, rootPath, prepareRootPath(rootPath), watchSeconds, queryOptions, createDefault(), null);
    }

    public static KVCache newCache(
            KeyValueClient kvClient,
            String rootPath,
            int watchSeconds,
            BigInteger initialIndex,
            QueryOptions queryOptions) {
      return new KVCache(kvClient, rootPath, prepareRootPath(rootPath), watchSeconds, queryOptions, createDefault(), initialIndex);
    }

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
    @Deprecated
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
    @Deprecated
    public static KVCache newCache(final KeyValueClient kvClient, final String rootPath) {
        CacheConfig cacheConfig = kvClient.getConfig().getCacheConfig();
        int watchSeconds = Math.toIntExact(cacheConfig.getWatchDuration().getSeconds());
        return newCache(kvClient, rootPath, watchSeconds);
    }
}
