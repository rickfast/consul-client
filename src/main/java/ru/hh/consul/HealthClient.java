package ru.hh.consul;

import com.google.common.collect.ImmutableMap;
import ru.hh.consul.async.ConsulResponseCallback;
import ru.hh.consul.config.ClientConfig;
import ru.hh.consul.model.ConsulResponse;
import ru.hh.consul.model.State;
import ru.hh.consul.model.health.HealthCheck;
import ru.hh.consul.model.health.ServiceHealth;
import ru.hh.consul.monitoring.ClientEventCallback;
import ru.hh.consul.option.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client for /v1/health/ endpoints.
 */
public class HealthClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthClient.class);

    private static String CLIENT_NAME = "health";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    HealthClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrieves the healthchecks for a node.
     * <p/>
     * GET /v1/health/node/{node}
     *
     * @param node The node to return checks for
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node) {
        return getNodeChecks(node, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a node in a given datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/node/{node}?dc={datacenter}
     *
     * @param node         The node to return checks for
     * @param queryOptions The Query Options to use.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node,
                                                           QueryOptions queryOptions) {
        return http.extractConsulResponse(api.getNodeChecks(node, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()));
    }

    /**
     * Retrieves the healthchecks for a service.
     * <p/>
     * GET /v1/health/checks/{service}
     *
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service) {
        return getServiceChecks(service, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a service in a given datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service,
                                                              QueryOptions queryOptions) {
        return http.extractConsulResponse(api.getServiceChecks(service, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()));
    }

    /**
     * Asynchronously retrieves the healthchecks for a service in a given
     * datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     *                     {@link HealthCheck} objects.
     */
    public void getServiceChecks(String service,
                                 QueryOptions queryOptions,
                                 ConsulResponseCallback<List<HealthCheck>> callback) {
        http.extractConsulResponse(api.getServiceChecks(service, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()), callback);
    }

    /**
     * Retrieves the healthchecks for a state.
     * <p/>
     * GET /v1/health/state/{state}
     *
     * @param state The state to query.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state) {
        return getChecksByState(state, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a state in a given datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param state        The state to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state,
                                                              QueryOptions queryOptions) {
        return http.extractConsulResponse(api.getChecksByState(state.getName(), queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()));
    }

    /**
     * Asynchronously retrieves the healthchecks for a state in a given datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param state        The state to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     * {@link HealthCheck} objects.
     */
    public void getChecksByState(State state, QueryOptions queryOptions,
                                 ConsulResponseCallback<List<HealthCheck>> callback) {
        http.extractConsulResponse(api.getChecksByState(state.getName(), queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()), callback);
    }

    /**
     * Retrieves the healthchecks for all healthy service instances.
     * <p/>
     * GET /v1/health/service/{service}?passing
     *
     * @param service The service to query.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service) {
        return getHealthyServiceInstances(service, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all healthy service instances in a given datacenter with
     * {@link QueryOptions}.
     * <p/>
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service,
                                                                          QueryOptions queryOptions) {
        return http.extractConsulResponse(api.getServiceInstances(service,
                optionsFrom(ImmutableMap.of("passing", "true"), queryOptions.toQuery()),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()));
    }

    /**
     * Retrieves the healthchecks for all healthy service instances in a given datacenter with
     * {@link QueryOptions}.
     * <p/>
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param timeoutMillis custom timeout in millis
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service,
                                                                          QueryOptions queryOptions,
                                                                          int timeoutMillis) {
        return http.extractConsulResponse(api.getServiceInstances(
            service,
            optionsFrom(Map.of("passing", "true"), queryOptions.toQuery()),
            queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()), Set.of(), timeoutMillis, TimeUnit.MILLISECONDS);
    }


    /**
     * Asynchronously retrieves the healthchecks for all healthy service instances in a given
     * datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     * <p/>
     * Experimental.
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     */
    public void getHealthyServiceInstances(String service, QueryOptions queryOptions,
                                           ConsulResponseCallback<List<ServiceHealth>> callback) {
        LOGGER.debug("Query options for healthy service instances with passing: {}", queryOptions);
        http.extractConsulResponse(api.getServiceInstances(service,
                optionsFrom(ImmutableMap.of("passing", "true"), queryOptions.toQuery()),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()), callback);
    }

    /**
     * Retrieves the healthchecks for all nodes.
     * <p/>
     * GET /v1/health/service/{service}
     *
     * @param service The service to query.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service) {
        return getAllServiceInstances(service, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all nodes in a given datacenter with
     * {@link QueryOptions}.
     * <p/>
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link ConsulResponse} containing a list of
     * {@link HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service, QueryOptions queryOptions) {
        return http.extractConsulResponse(api.getServiceInstances(service, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()));
    }

    /**
     * Asynchronously retrieves the healthchecks for all nodes in a given
     * datacenter with {@link QueryOptions}.
     * <p/>
     * GET /v1/health/service/{service}?dc={datacenter}
     * <p/>
     * Experimental.
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     */
    public void getAllServiceInstances(String service, QueryOptions queryOptions,
                                       ConsulResponseCallback<List<ServiceHealth>> callback) {
        http.extractConsulResponse(api.getServiceInstances(service, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta(), queryOptions.toHeaders()), callback);
    }

    private static Map<String, Object> optionsFrom(Map<String, ?>... options) {
        Map<String, Object> result = new HashMap<>();

        for (Map<String, ?> option : options) {
            result.putAll(option);
        }

        return result;
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("health/node/{node}")
        Call<List<HealthCheck>> getNodeChecks(@Path("node") String node,
                                              @QueryMap Map<String, Object> query,
                                              @Query("tag") List<String> tag,
                                              @Query("node-meta") List<String> nodeMeta,
                                              @HeaderMap Map<String, String> headers);

        @GET("health/checks/{service}")
        Call<List<HealthCheck>> getServiceChecks(@Path("service") String service,
                                                 @QueryMap Map<String, Object> query,
                                                 @Query("tag") List<String> tag,
                                                 @Query("node-meta") List<String> nodeMeta,
                                                 @HeaderMap Map<String, String> headers);

        @GET("health/state/{state}")
        Call<List<HealthCheck>> getChecksByState(@Path("state") String state,
                                                 @QueryMap Map<String, Object> query,
                                                 @Query("tag") List<String> tag,
                                                 @Query("node-meta") List<String> nodeMeta,
                                                 @HeaderMap Map<String, String> headers);

        @GET("health/service/{service}")
        Call<List<ServiceHealth>> getServiceInstances(@Path("service") String service,
                                                      @QueryMap Map<String, Object> query,
                                                      @Query("tag") List<String> tag,
                                                      @Query("node-meta") List<String> nodeMeta,
                                                      @HeaderMap Map<String, String> headers);
    }
}
