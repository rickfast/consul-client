package com.orbitz.consul;

import com.orbitz.consul.model.kv.TxResponse;
import com.orbitz.consul.model.kv.Value;
import okhttp3.Headers;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.Calls;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MockApiService implements KeyValueClient.Api {
    private final BehaviorDelegate<KeyValueClient.Api> delegate;

    public MockApiService(BehaviorDelegate<KeyValueClient.Api> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Call<List<Value>> getValue(String key, Map<String, Object> query) {
        final Headers headers = Headers.of("X-Consul-Knownleader", "true");
        final Call<List<Object>> call = Calls.response(Response.success(Collections.emptyList(), headers));
        return delegate.returning(call).getValue(key, query);
    }

    @Override
    public Call<List<String>> getKeys(String key, Map<String, Object> query) {
        return delegate.returningResponse(Collections.emptyList()).getKeys(key, query);
    }

    @Override
    public Call<Boolean> putValue(String key, Map<String, Object> query) {
        return delegate.returningResponse(true).putValue(key, query);
    }

    @Override
    public Call<Boolean> putValue(String key, RequestBody data, Map<String, Object> query) {
        return delegate.returningResponse(true).putValue(key, query);
    }

    @Override
    public Call<Void> deleteValues(String key, Map<String, Object> query) {
        return delegate.returningResponse(null).deleteValues(key, query);
    }

    @Override
    public Call<TxResponse> performTransaction(RequestBody body, Map<String, Object> query) {
        return delegate.returningResponse(null).performTransaction(body, query);
    }
}
