package ru.hh.consul;

import ru.hh.consul.config.ClientConfig;
import ru.hh.consul.monitoring.ClientEventCallback;

/**
 * Allows tests to create KeyValueClient objects.
 */
public class KeyValueClientFactory {
    private KeyValueClientFactory() {
    }

    public static KeyValueClient create(KeyValueClient.Api api, ClientConfig config, ClientEventCallback eventCallback) {
        return new KeyValueClient(api, config, eventCallback);
    }
}
