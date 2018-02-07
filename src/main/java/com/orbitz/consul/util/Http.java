package com.orbitz.consul.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.Callback;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.monitoring.ClientEventHandler;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;

public class Http {

    private final ClientEventHandler eventHandler;

    public Http(ClientEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private static boolean isSuccessful(Response<?> response, Integer... okCodes) {
        return response.isSuccessful() || Sets.newHashSet(okCodes).contains(response.code());
    }

    public <T> T extract(Call<T> call, Integer... okCodes) {
        Response<T> response = executeCall(call);
        ensureResponseSuccessful(call, response, okCodes);
        return response.body();
    }

    public void handle(Call<Void> call, Integer... okCodes) {
        Response<Void> response = executeCall(call);
        ensureResponseSuccessful(call, response, okCodes);
    }

    public <T> ConsulResponse<T> extractConsulResponse(Call<T> call, Integer... okCodes) {
        Response<T> response = executeCall(call);
        ensureResponseSuccessful(call, response, okCodes);
        return consulResponse(response);
    }

    private <T> Response<T> executeCall(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            eventHandler.httpRequestFailure(call.request(), e);
            throw new ConsulException(e);
        }
    }

    private <T> void ensureResponseSuccessful(Call<T> call, Response<T> response, Integer... okCodes) {
        if(isSuccessful(response, okCodes)) {
            eventHandler.httpRequestSuccess(call.request());
        } else {
            ConsulException exception = new ConsulException(response.code(), response);
            eventHandler.httpRequestInvalid(call.request(), exception);
            throw exception;
        }
    }

    public <T> void extractConsulResponse(Call<T> call, final ConsulResponseCallback<T> callback,
                                                 final Integer... okCodes) {
        call.enqueue(createCallback(call, callback, okCodes));
    }

    @VisibleForTesting
    <T> retrofit2.Callback<T> createCallback(Call<T> call, final ConsulResponseCallback<T> callback,
                                             final Integer... okCodes) {
        return new retrofit2.Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (isSuccessful(response, okCodes)) {
                    eventHandler.httpRequestSuccess(call.request());
                    callback.onComplete(consulResponse(response));
                } else {
                    ConsulException exception = new ConsulException(response.code(), response);
                    eventHandler.httpRequestInvalid(call.request(), exception);
                    callback.onFailure(exception);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                eventHandler.httpRequestFailure(call.request(), t);
                callback.onFailure(t);
            }
        };
    }

    public <T> void extractBasicResponse(Call<T> call, final Callback<T> callback,
                                                final Integer... okCodes) {
        extractConsulResponse(call, createConsulResponseCallbackWrapper(callback), okCodes);
    }

    private <T> ConsulResponseCallback<T> createConsulResponseCallbackWrapper(Callback<T> callback) {
        return new ConsulResponseCallback<T>() {
            @Override
            public void onComplete(ConsulResponse<T> consulResponse) {
                callback.onResponse(consulResponse.getResponse());
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        };
    }

    @VisibleForTesting
    static <T> ConsulResponse<T> consulResponse(Response<T> response) {
        Headers headers = response.headers();
        String indexHeaderValue = headers.get("X-Consul-Index");
        String lastContactHeaderValue = headers.get("X-Consul-Lastcontact");
        String knownLeaderHeaderValue = headers.get("X-Consul-Knownleader");

        BigInteger index = indexHeaderValue == null ? BigInteger.ZERO : new BigInteger(indexHeaderValue);
        long lastContact = lastContactHeaderValue == null ? 0 : Long.valueOf(lastContactHeaderValue);
        boolean knownLeader = knownLeaderHeaderValue == null ? false : Boolean.valueOf(knownLeaderHeaderValue);

        return new ConsulResponse<>(response.body(), lastContact, knownLeader, index);
    }
}
