package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventCallback;
import retrofit2.Retrofit;

/**
 * HTTP Client for /v1/snapshot/ endpoints.
 *
 * @see <a href="https://www.consul.io/api/snapshot.html">The Consul API Docs</a> for Snapshots
 */
public class SnapshotClient extends BaseClient {

    private static String CLIENT_NAME = "snapshot";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    SnapshotClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

    }
}
