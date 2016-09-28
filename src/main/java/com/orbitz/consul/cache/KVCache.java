package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KVCache extends ConsulCache<String, Value> {

    private KVCache(Function<Value, String> keyConversion, ConsulCache.CallbackConsumer<Value> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    public static KVCache newCache(
            final KeyValueClient kvClient,
            final String rootPath,
            final int watchSeconds,
            final QueryOptions queryOptions) {

        final Set<String> rootPathSegments =
                new LinkedHashSet<>(Arrays.asList(rootPath.split("/")));

        final Function<Value, String> keyExtractor = new Function<Value, String>() {
            @Override
            public String apply(Value input) {
                final Set<String> inputPathSegments =
                        new LinkedHashSet<>(Arrays.asList(input.getKey().split("/")));

                return StringUtils.join(Sets.difference(inputPathSegments, rootPathSegments), "/");
            }
        };

        final CallbackConsumer<Value> callbackConsumer = new CallbackConsumer<Value>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<Value>> callback) {
                QueryOptions params = watchParams(index, watchSeconds, queryOptions);
                kvClient.getValues(rootPath, params, callback);
            }
        };

        return new KVCache(keyExtractor, callbackConsumer);
    }

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
    public static KVCache newCache(
            final KeyValueClient kvClient,
            final String rootPath) {
        return newCache(kvClient, rootPath, 10);
    }
}
