package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventCallback;

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
