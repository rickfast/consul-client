package ru.hh.consul;

import ru.hh.consul.config.ClientConfig;
import ru.hh.consul.monitoring.ClientEventCallback;
import ru.hh.consul.monitoring.ClientEventHandler;
import ru.hh.consul.util.Http;

abstract class BaseClient {

    private final ClientConfig config;
    private final ClientEventHandler eventHandler;
    protected final Http http;

    protected BaseClient(String name, ClientConfig config, ClientEventCallback eventCallback) {
        this.config = config;
        this.eventHandler = new ClientEventHandler(name, eventCallback);
        this.http = new Http(eventHandler);
    }

    public ClientConfig getConfig() {
        return config;
    }

    public ClientEventHandler getEventHandler() { return eventHandler; }
}
