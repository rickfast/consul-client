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
public class AsyncCallbackConsumer implements ConsulCache.CallbackConsumer<Value>, AutoCloseable {
    private final List<Value> result;
    private int callCount;
    private Thread thread;

    public AsyncCallbackConsumer(List<Value> result) {
        this.result = Collections.unmodifiableList(result);
    }

    @Override
    public void consume(BigInteger index, final ConsulResponseCallback<List<Value>> callback) {
        callCount++;
        thread = new Thread(() -> {
            callback.onComplete(new ConsulResponse<List<Value>>(result, 0, true, BigInteger.ZERO));
        });
        thread.setName("asyncCallbackConsumer");

        thread.start();
    }

    public int getCallCount() {
        return callCount;
    }

    @Override
    public void close() {
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread did not terminate within timeout!");
        }
        if (thread.isAlive()) {
            thread.interrupt();
            throw new RuntimeException("Thread did not terminate in a timely manner!");
        }
    }
}
