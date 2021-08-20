package com.orbitz.consul.cache;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class IndexAwareStubCallbackConsumer implements ConsulCache.CallbackConsumer<Value> {

    private final List<Value> result;
    private BigInteger index;

    public IndexAwareStubCallbackConsumer(List<Value> result) {
        this.result = Collections.unmodifiableList(result);
    }

    @Override
    public void consume(BigInteger index, ConsulResponseCallback<List<Value>> callback) {
        this.index = index;
        callback.onComplete(new ConsulResponse<>(result, 0, true, BigInteger.ZERO, null, null));
    }

    public BigInteger getIndex() {
        return index;
    }
}
