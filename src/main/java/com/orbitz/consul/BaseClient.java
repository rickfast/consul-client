package com.orbitz.consul;

import com.orbitz.consul.config.ConsulConfig;

abstract class BaseClient {

    private ConsulConfig config;

    BaseClient(ConsulConfig config) {
        this.config = config;
    }

    public ConsulConfig getConfig() {
        return config;
    }
}
