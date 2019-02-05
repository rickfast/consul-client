package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.async.Callback;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.query.PreparedQuery;
import com.orbitz.consul.model.query.QueryId;
import com.orbitz.consul.model.query.QueryResults;
import com.orbitz.consul.model.query.StoredQuery;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.QueryOptions;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PreparedQueryClient extends BaseClient {

    private static String CLIENT_NAME = "preparedquery";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    PreparedQueryClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Creates a prepared query.
     *
     * POST /v1/query
     *
     * @param preparedQuery The prepared query to create.
     * @return The ID of the created query.
     */
    public String createPreparedQuery(PreparedQuery preparedQuery) {
        return createPreparedQuery(preparedQuery, null);
    }

    /**
     * Creates a prepared query.
     *
     * POST /v1/query
     *
     * @param preparedQuery The prepared query to create.
     * @param dc The data center.
     * @return The ID of the created query.
     */
    public String createPreparedQuery(PreparedQuery preparedQuery, final String dc) {
        return http.extract(api.createPreparedQuery(preparedQuery, dcQuery(dc))).getId();
    }

    private Map<String, String> dcQuery(String dc) {
        return dc != null ? ImmutableMap.of("dc", dc): Collections.emptyMap();
    }
    
    /**
     * Retrieves the list of prepared queries.
     * 
     * GET /v1/query
     * 
     * @return The list of prepared queries.
     */
    public List<StoredQuery> getPreparedQueries() {
        return getPreparedQueries(null);
    }

    /**
     * Retrieves the list of prepared queries.
     *
     * GET /v1/query
     *
     * @param dc The data center.
     * @return The list of prepared queries.
     */
    public List<StoredQuery> getPreparedQueries(final String dc) {
        return http.extract(api.getPreparedQueries(dcQuery(dc)));
    }

    /**
     * Retrieves a prepared query by its ID.
     *
     * GET /v1/query/{id}
     *
     * @param id The query ID.
     * @return The store prepared query.
     */
    public Optional<StoredQuery> getPreparedQuery(String id) {
        return getPreparedQuery(id, null);
    }

    /**
     * Retrieves a prepared query by its ID.
     *
     * GET /v1/query/{id}
     *
     * @param id The query ID.
     * @param dc The data center.
     * @return The store prepared query.
     */
    public Optional<StoredQuery> getPreparedQuery(String id, final String dc) {
        List<StoredQuery> result = http.extract(api.getPreparedQuery(id, dcQuery(dc)));

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /**
     * Executes a prepared query by its name or ID.
     *
     * @param nameOrId The query name or ID.
     * @return A {@link QueryResults} object containing service instances.
     */
    public QueryResults execute(String nameOrId) {
        return http.extract(api.execute(nameOrId, Collections.emptyMap()));
    }

    /**
     * Executes a prepared query by its name or ID.
     *
     * @param nameOrId The query name or ID.
     * @param options Query options.
     * @param callback Basic callback for the response.
     */
    public void execute(String nameOrId, QueryOptions options, final Callback<QueryResults> callback) {
        http.extractBasicResponse(api.execute(nameOrId, options.toQuery()), callback);
    }

    /**
     * Retrofit API interface.
     */
    interface Api {
	
        @GET("query")
        Call<List<StoredQuery>> getPreparedQueries(@QueryMap Map<String, String> queryMap);

        @POST("query")
        Call<QueryId> createPreparedQuery(@Body PreparedQuery preparedQuery,
                                          @QueryMap Map<String, String> queryMap);

        @GET("query/{id}")
        Call<List<StoredQuery>> getPreparedQuery(@Path("id") String id,
                                                 @QueryMap Map<String, String> queryMap);

        @GET("query/{nameOrId}/execute")
        Call<QueryResults> execute(@Path("nameOrId") String nameOrId,
                                   @QueryMap Map<String, Object> queryMap);
    }
}
