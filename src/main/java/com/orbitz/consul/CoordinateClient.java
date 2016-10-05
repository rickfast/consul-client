package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.model.coordinate.Coordinate;
import com.orbitz.consul.model.coordinate.Datacenter;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.Http.extract;

/**
 * HTTP Client for /v1/coordinate/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#agent">The Consul API Docs</a>
 */
public class CoordinateClient {

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    CoordinateClient(Retrofit retrofit) {
        this.api = retrofit.create(Api.class);
    }

    public List<Datacenter> getDatacenters() {
        return extract(api.getDatacenters());
    }

    public List<Coordinate> getNodes(String dc) {
        return extract(api.getNodes(dcQuery(dc)));
    }

    public List<Coordinate> getNodes() {
        return getNodes(null);
    }

    private Map<String, String> dcQuery(String dc) {
        Map<String, String> query = Collections.emptyMap();

        if (dc != null) {
            query = ImmutableMap.of("dc", dc);
        }
        return query;
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("coordinate/datacenters")
        Call<List<Datacenter>> getDatacenters();

        @GET("coordinate/nodes")
        Call<List<Coordinate>> getNodes(@QueryMap Map<String, String> query);

    }
}
