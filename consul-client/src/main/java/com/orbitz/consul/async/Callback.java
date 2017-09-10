package com.orbitz.consul.async;

public interface Callback<T> {

    void onResponse(T result);

    void onFailure(Throwable t);
}
