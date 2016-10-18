package com.orbitz.consul.util;

import com.google.common.collect.Sets;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.Callback;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;

public class Http {

    public static boolean isSuccessful(Response<?> response, Integer... okCodes) {
        return response.isSuccessful() || Sets.newHashSet(okCodes).contains(response.code());
    }

    public static <T> T extract(Call<T> call, Integer... okCodes) {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new ConsulException(e);
        }

        if(isSuccessful(response, okCodes)) {
            return response.body();
        } else {
            throw new ConsulException(response.code(), response);
        }
    }

    public static void handle(Call<Void> call, Integer... okCodes) {
        Response<Void> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new ConsulException(e);
        }

        if(!isSuccessful(response, okCodes)) {
            throw new ConsulException(response.code(), response);
        }
    }

    public static <T> ConsulResponse<T> extractConsulResponse(Call<T> call, Integer... okCodes) {
        Response<T> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new ConsulException(e);
        }

        if(!isSuccessful(response, okCodes)) {
            throw new ConsulException(response.code(), response);
        }

        return consulResponse(response);
    }

    public static <T> void extractConsulResponse(Call<T> call, final ConsulResponseCallback<T> callback,
                                                 final Integer... okCodes) {
        call.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (isSuccessful(response, okCodes)) {
                    callback.onComplete(consulResponse(response));
                } else {
                    callback.onFailure(new ConsulException(response.code(), response));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public static <T> void extractBasicResponse(Call<T> call, final Callback<T> callback,
                                                final Integer... okCodes) {
        call.enqueue(new retrofit2.Callback<T>() {

            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (isSuccessful(response, okCodes)) {
                    callback.onResponse(response.body());
                } else {
                    callback.onFailure(new ConsulException(response.code(), response));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private static <T> ConsulResponse<T> consulResponse(Response<T> response) {
        Headers headers = response.headers();
        String indexHeaderValue = headers.get("X-Consul-Index");
        String lastContactHeaderValue = headers.get("X-Consul-Lastcontact");
        String knownLeaderHeaderValue = headers.get("X-Consul-Knownleader");

        BigInteger index = indexHeaderValue == null ? new BigInteger("0") : new BigInteger(indexHeaderValue);
        long lastContact = lastContactHeaderValue == null ? 0 : Long.valueOf(lastContactHeaderValue);
        boolean knownLeader = knownLeaderHeaderValue == null ? false : Boolean.valueOf(knownLeaderHeaderValue);

        ConsulResponse<T> consulResponse = new ConsulResponse<>(response.body(), lastContact, knownLeader, index);

        return consulResponse;
    }
}
