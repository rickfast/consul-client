package com.orbitz.consul.async;

import com.orbitz.consul.model.EventResponse;

public interface EventResponseCallback {

    /**
     * Callback for a successful {@link com.orbitz.consul.model.EventResponse}.
     *
     * @param EventResponse The Consul event response.
     */
    public void onComplete(EventResponse EventResponse);

    /**
     * Callback for an unsuccessful request.
     *
     * @param throwable The exception thrown.
     */
    public void onFailure(Throwable throwable);
}
