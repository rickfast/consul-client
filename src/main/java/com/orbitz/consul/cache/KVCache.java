package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.kv.Value;

import java.math.BigInteger;
import java.util.List;

public class KVCache extends ConsulCache<Value> {

    private KVCache(Function<Value, String> keyConversion, ConsulCache.CallbackConsumer<Value> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    /**
     * Factory method to construct a String/{@link Value} map with a 10 second
     * block interval
     *
     * @param kvClient the {@link KeyValueClient} to use
     * @param rootPath the root path
     * @return the cache object
     */
    public static ConsulCache<Value> newCache(
            final KeyValueClient kvClient,
            final String rootPath,
            final int watchSeconds) {

        final Function<Value, String> keyExtractor = new Function<Value, String>() {
            @Override
            public String apply(Value input) {
                return input.getKey().substring(rootPath.length() + 1);
            }
        };

        final CallbackConsumer<Value> callbackConsumer = new CallbackConsumer<Value>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<Value>> callback) {
                kvClient.getValues(rootPath, watchParams(index, watchSeconds), callback);
            }
        };

        return new KVCache(keyExtractor, callbackConsumer);
    }

}
