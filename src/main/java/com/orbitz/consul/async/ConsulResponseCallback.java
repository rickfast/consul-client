package com.orbitz.consul.async;

import com.orbitz.consul.model.ConsulResponse;

public abstract class ConsulResponseCallback<T> {

    public abstract void onComplete(ConsulResponse<T> consulResponse);
    public abstract void onFailure(Throwable throwable);
}
