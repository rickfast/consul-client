package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.monitoring.ClientEventCallback;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import java.util.List;

public class StatusClient extends BaseClient {

    private static String CLIENT_NAME = "status";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    StatusClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrieves the host/port of the Consul leader.
     *
     * GET /v1/status/leader
     *
     * @return The host/port of the leader.
     */
    public String getLeader() {
        return http.extract(api.getLeader()).replace("\"", "").trim();
    }

    /**
     * Retrieves a list of host/ports for raft peers.
     *
     * GET /v1/status/peers
     *
     * @return List of host/ports for raft peers.
     */
    public List<String> getPeers() {
        return http.extract(api.getPeers());
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("status/leader")
        Call<String> getLeader();

        @GET("status/peers")
        Call<List<String>> getPeers();
    }
}
