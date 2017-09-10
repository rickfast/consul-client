package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.model.operator.RaftConfiguration;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.Map;

import static com.orbitz.consul.util.Http.extract;
import static com.orbitz.consul.util.Http.handle;

public class OperatorClient {

    private final Api api;

    OperatorClient(Retrofit retrofit) {
        this.api = retrofit.create(Api.class);
    }

    public RaftConfiguration getRaftConfiguration() {
        return extract(api.getConfiguration(ImmutableMap.<String, String>of()));
    }

    public RaftConfiguration getRaftConfiguration(String datacenter) {
        return extract(api.getConfiguration(ImmutableMap.of("dc", datacenter)));
    }

    public RaftConfiguration getStaleRaftConfiguration(String datacenter) {
        return extract(api.getConfiguration(ImmutableMap.of(
            "dc", datacenter, "stale", "true"
        )));
    }

    public RaftConfiguration getStaleRaftConfiguration() {
        return extract(api.getConfiguration(ImmutableMap.of(
                "stale", "true"
        )));
    }

    public void deletePeer(String address) {
        handle(api.deletePeer(address, ImmutableMap.<String, String>of()));
    }

    public void deletePeer(String address, String datacenter) {
        handle(api.deletePeer(address, ImmutableMap.of("dc", datacenter)));
    }

    interface Api {

        @GET("operator/raft/configuration")
        Call<RaftConfiguration> getConfiguration(@QueryMap Map<String, String> query);

        @DELETE("operator/raft/peer")
        Call<Void> deletePeer(@Query("address") String address,
                              @QueryMap Map<String, String> query);
    }
}
