package com.orbitz.consul;

import com.google.common.base.Optional;
import com.orbitz.consul.async.Callback;
import com.orbitz.consul.model.query.PreparedQuery;
import com.orbitz.consul.model.query.QueryId;
import com.orbitz.consul.model.query.QueryResults;
import com.orbitz.consul.model.query.StoredQuery;
import com.orbitz.consul.option.QueryOptions;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.Http.extract;
import static com.orbitz.consul.util.Http.extractBasicResponse;

public class PreparedQueryClient {

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    PreparedQueryClient(Retrofit retrofit) {
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
        return extract(api.createPreparedQuery(preparedQuery)).getId();
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
        List<StoredQuery> result = extract(api.getPreparedQuery(id));

        return result.isEmpty() ? Optional.<StoredQuery>absent() : Optional.of(result.get(0));
    }

    /**
     * Executes a prepared query by its name or ID.
     *
     * @param nameOrId The query name or ID.
     * @return A {@link QueryResults} object containing service instances.
     */
    public QueryResults execute(String nameOrId) {
        return extract(api.execute(nameOrId, Collections.<String, Object>emptyMap()));
    }

    /**
     * Executes a prepared query by its name or ID.
     *
     * @param nameOrId The query name or ID.
     * @param options Query options.
     * @param callback Basic callback for the response.
     */
    public void execute(String nameOrId, QueryOptions options, final Callback<QueryResults> callback) {
        extractBasicResponse(api.execute(nameOrId, options.toQuery()), callback);
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @POST("query")
        Call<QueryId> createPreparedQuery(@Body PreparedQuery preparedQuery);

        @GET("query/{id}")
        Call<List<StoredQuery>> getPreparedQuery(@Path("id") String id);

        @GET("query/{nameOrId}/execute")
        Call<QueryResults> execute(@Path("nameOrId") String nameOrId,
                                   @QueryMap Map<String, Object> queryMap);
    }
}
