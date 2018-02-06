package com.orbitz.consul.util;

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
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            eventHandler.httpRequestFailure(call.request(), e);
            throw new ConsulException(e);
        }

        if(isSuccessful(response, okCodes)) {
            eventHandler.httpRequestSuccess(call.request());
            return response.body();
        } else {
            ConsulException exception = new ConsulException(response.code(), response);
            eventHandler.httpRequestInvalid(call.request(), exception);
            throw exception;
        }
    }

    public void handle(Call<Void> call, Integer... okCodes) {
        Response<Void> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            eventHandler.httpRequestFailure(call.request(), e);
            throw new ConsulException(e);
        }

        if(isSuccessful(response, okCodes)) {
            eventHandler.httpRequestSuccess(call.request());
        } else {
            ConsulException exception = new ConsulException(response.code(), response);
            eventHandler.httpRequestInvalid(call.request(), exception);
            throw exception;
        }
    }

    public <T> ConsulResponse<T> extractConsulResponse(Call<T> call, Integer... okCodes) {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            eventHandler.httpRequestFailure(call.request(), e);
            throw new ConsulException(e);
        }

        if(isSuccessful(response, okCodes)) {
            eventHandler.httpRequestSuccess(call.request());
        } else {
            ConsulException exception = new ConsulException(response.code(), response);
            eventHandler.httpRequestInvalid(call.request(), exception);
            throw exception;
        }

        return consulResponse(response);
    }

    public <T> void extractConsulResponse(Call<T> call, final ConsulResponseCallback<T> callback,
                                                 final Integer... okCodes) {
        call.enqueue(new retrofit2.Callback<T>() {
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
        });
    }

    public <T> void extractBasicResponse(Call<T> call, final Callback<T> callback,
                                                final Integer... okCodes) {
        call.enqueue(new retrofit2.Callback<T>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (isSuccessful(response, okCodes)) {
                    eventHandler.httpRequestSuccess(call.request());
                    callback.onResponse(response.body());
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
        });
    }

    private static <T> ConsulResponse<T> consulResponse(Response<T> response) {
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
