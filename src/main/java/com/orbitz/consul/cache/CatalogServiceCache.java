package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.option.CatalogOptions;

import java.math.BigInteger;
import java.util.List;

public class CatalogServiceCache extends ConsulCache<CatalogServiceKey, CatalogService> {

    CatalogServiceCache(Function<CatalogService, CatalogServiceKey> keyConversion, CallbackConsumer<CatalogService> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    public static CatalogServiceCache newCache(final CatalogClient catalogClient,
                                               final String serviceName,
                                               final CatalogOptions catalogOptions,
                                               final int watchSeconds) {
        Function<CatalogService, CatalogServiceKey> keyExtractor = new Function<CatalogService, CatalogServiceKey>() {
            @Override
            public CatalogServiceKey apply(CatalogService input) {
                return CatalogServiceKey.fromCatalogService(input);
            }
        };

        CallbackConsumer<CatalogService> callbackConsumer = new CallbackConsumer<CatalogService>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<CatalogService>> callback) {
                catalogClient.getService(serviceName, catalogOptions, watchParams(index, watchSeconds), callback);
            }
        };
        return new CatalogServiceCache(keyExtractor, callbackConsumer);
    }

    public static CatalogServiceCache newCache(final CatalogClient catalogClient, final String serviceName) {
        return newCache(catalogClient, serviceName, CatalogOptions.BLANK, 10);
    }
}
