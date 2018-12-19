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
public class StubCallbackConsumer implements ConsulCache.CallbackConsumer<Value> {

    private final List<Value> result;
    private int callCount;

    public StubCallbackConsumer(List<Value> result) {
        this.result = Collections.unmodifiableList(result);
    }

    @Override
    public void consume(BigInteger index, ConsulResponseCallback<List<Value>> callback) {
        callCount++;
        callback.onComplete(new ConsulResponse<>(result, 0, true, BigInteger.ZERO));
    }

    public int getCallCount() {
        return callCount;
    }
}
