package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;

abstract class BaseClient {

    private final ClientConfig config;

    protected BaseClient(ClientConfig config) {
        this.config = config;
    }

    public ClientConfig getConfig() {
        return config;
    }
}
