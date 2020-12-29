package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.monitoring.ClientEventHandler;
import com.orbitz.consul.util.Http;

abstract class BaseCacheableClient extends BaseClient {

    private final Consul.NetworkTimeoutConfig networkTimeoutConfig;

    protected BaseCacheableClient(String name, ClientConfig config, ClientEventCallback eventCallback,
                                  Consul.NetworkTimeoutConfig networkTimeoutConfig) {
        super(name, config, eventCallback);
        this.networkTimeoutConfig = networkTimeoutConfig;
    }

    public Consul.NetworkTimeoutConfig getNetworkTimeoutConfig() {
        return networkTimeoutConfig;
    }
}
