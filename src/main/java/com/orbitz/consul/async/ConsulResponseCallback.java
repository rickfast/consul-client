package com.orbitz.consul.async;

import com.orbitz.consul.model.ConsulResponse;

/**
 * For API calls that support long-polling, this callback is used to handle
 * the result on success or failure for an async HTTP call.
 *
 * @param <T> The Response type.
 */
public abstract class ConsulResponseCallback<T> {

    /**
     * Callback for a successful {@link com.orbitz.consul.model.ConsulResponse}.
     *
     * @param consulResponse The Consul response.
     */
    public abstract void onComplete(ConsulResponse<T> consulResponse);

    /**
     * Callback for an unsuccessful request.
     *
     * @param throwable The exception thrown.
     */
    public abstract void onFailure(Throwable throwable);
}
