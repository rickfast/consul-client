package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.monitoring.ClientEventHandler;

abstract class BaseClient {

    private final ClientConfig config;
    private final ClientEventHandler eventHandler;

    protected BaseClient(String name, ClientConfig config, ClientEventCallback eventCallback) {
        this.config = config;
        this.eventHandler = new ClientEventHandler(name, eventCallback);
    }

    public ClientConfig getConfig() {
        return config;
    }

    public ClientEventHandler getEventHandler() { return eventHandler; }
}
