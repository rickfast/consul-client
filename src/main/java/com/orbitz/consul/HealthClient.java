package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.Options;
import com.orbitz.consul.option.QueryOptions;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.Http.extractConsulResponse;

/**
 * HTTP Client for /v1/health/ endpoints.
 */
public class HealthClient {

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    HealthClient(Retrofit retrofit) {
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrieves the healthchecks for a node.
     *
     * GET /v1/health/node/{node}
     *
     * @param node The node to return checks for
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node) {
        return getNodeChecks(node, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a node in a given datacenter.
     *
     * GET /v1/health/node/{node}?dc={datacenter}
     *
     * @param node The node to return checks for
     * @param catalogOptions The catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, CatalogOptions catalogOptions) {
        return getNodeChecks(node, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a node with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/node/{node}
     *
     * @param node The node to return checks for
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, QueryOptions queryOptions) {
        return getNodeChecks(node, null, queryOptions);
    }

    /**
     * Retrieves the healthchecks for a node in a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/node/{node}?dc={datacenter}
     *
     * @param node The node to return checks for
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, CatalogOptions catalogOptions,
                                                           QueryOptions queryOptions) {
        return extractConsulResponse(api.getNodeChecks(node, Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrieves the healthchecks for a service.
     *
     * GET /v1/health/checks/{service}
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service) {
        return getServiceChecks(service, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a service in a given datacenter.
     *
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param catalogOptions The catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, CatalogOptions catalogOptions) {
        return getServiceChecks(service, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a service with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/checks/{service}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, QueryOptions queryOptions) {
        return getServiceChecks(service, null, queryOptions);
    }

    /**
     * Retrieves the healthchecks for a service in a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, CatalogOptions catalogOptions,
                                                              QueryOptions queryOptions) {
        return extractConsulResponse(api.getServiceChecks(service, Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Asynchronously retrieves the healthchecks for a service in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param service      The service to query.
     * @param queryOptions   The Query Options to use.
     * @param callback       Callback implemented by callee to handle results.
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public void getServiceChecks(String service,
                                 QueryOptions queryOptions,
                                 ConsulResponseCallback<List<HealthCheck>> callback) {
        extractConsulResponse(api.getServiceChecks(service, queryOptions.toQuery()), callback);
    }


    /**
     * Asynchronously retrieves the healthchecks for a service in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/checks/{service}?dc={datacenter}
     *
     * @param service      The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @param callback       Callback implemented by callee to handle results.
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public void getServiceChecks(String service,
                                 CatalogOptions catalogOptions,
                                 QueryOptions queryOptions,
                                 ConsulResponseCallback<List<HealthCheck>> callback) {
        extractConsulResponse(api.getServiceChecks(service, Options.from(queryOptions, catalogOptions)), callback);
    }

    /**
     * Retrieves the healthchecks for a state.
     *
     * GET /v1/health/state/{state}
     *
     * @param state The state to query.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state) {
        return getChecksByState(state, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a state in a given datacenter.
     *
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param state          The state to query.
     * @param catalogOptions The catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state, CatalogOptions catalogOptions) {
        return getChecksByState(state, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a state with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/state/{state}
     *
     * @param state        The state to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state, QueryOptions queryOptions) {
        return getChecksByState(state, null, queryOptions);
    }

    /**
     * Retrieves the healthchecks for a state in a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param state          The state to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(State state, CatalogOptions catalogOptions,
                                                              QueryOptions queryOptions) {
        return extractConsulResponse(api.getChecksByState(state.getName(), Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrieves the healthchecks for all healthy service instances.
     *
     * GET /v1/health/service/{service}?passing
     *
     * @param service The service to query.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service) {
        return getHealthyServiceInstances(service, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all healthy service instances in a given datacenter.
     *
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service, CatalogOptions catalogOptions) {
        return getHealthyServiceInstances(service, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all healthy service instances with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?passing
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service, QueryOptions queryOptions) {
        return getHealthyServiceInstances(service, null, queryOptions);
    }

    /**
     * Retrieves the healthchecks for all healthy service instances in a given datacenter with
     * {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServiceInstances(String service, CatalogOptions catalogOptions,
                                                                          QueryOptions queryOptions) {
        return extractConsulResponse(api.getServiceInstances(service,
                optionsFrom(ImmutableMap.of("passing", "true"), Options.from(catalogOptions, queryOptions))));
    }



    /**
     * Asynchronously retrieves the healthchecks for all healthy service instances in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * Experimental.
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @param callback       Callback implemented by callee to handle results.
     */
    public void getHealthyServiceInstances(String service, CatalogOptions catalogOptions,
                                           QueryOptions queryOptions,
                                           ConsulResponseCallback<List<ServiceHealth>> callback) {
        extractConsulResponse(api.getServiceInstances(service,
                optionsFrom(ImmutableMap.of("passing", "true"), Options.from(catalogOptions, queryOptions))),
                callback);
    }

    /**
     * Asynchronously retrieves the healthchecks for all healthy service instances in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}&amp;passing
     *
     * Experimental.
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     */
    public void getHealthyServiceInstances(String service, QueryOptions queryOptions,
                                           ConsulResponseCallback<List<ServiceHealth>> callback) {
        extractConsulResponse(api.getServiceInstances(service,
                optionsFrom(ImmutableMap.of("passing", "true"), queryOptions.toQuery())), callback);
    }

    /**
     * Retrieves the healthchecks for all nodes.
     *
     * GET /v1/health/service/{service}
     *
     * @param service The service to query.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service) {
        return getAllServiceInstances(service, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all nodes in a given datacenter.
     *
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service, CatalogOptions catalogOptions) {
        return getAllServiceInstances(service, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all nodes with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service, QueryOptions queryOptions) {
        return getAllServiceInstances(service, null, queryOptions);
    }

    /**
     * Retrieves the healthchecks for all nodes in a given datacenter with
     * {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllServiceInstances(String service, CatalogOptions catalogOptions,
                                                                      QueryOptions queryOptions) {
        return extractConsulResponse(api.getServiceInstances(service, Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Asynchronously retrieves the healthchecks for all nodes in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * Experimental.
     *
     * @param service        The service to query.
     * @param catalogOptions The catalog specific options to use.
     * @param queryOptions   The Query Options to use.
     * @param callback       Callback implemented by callee to handle results.
     */
    public void getAllServiceInstances(String service, CatalogOptions catalogOptions,
                                       QueryOptions queryOptions,
                                       ConsulResponseCallback<List<ServiceHealth>> callback) {
        extractConsulResponse(api.getServiceInstances(service, Options.from(catalogOptions, queryOptions)),
                callback);
    }

    /**
     * Asynchronously retrieves the healthchecks for all nodes in a given
     * datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * Experimental.
     *
     * @param service      The service to query.
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     */
    public void getAllServiceInstances(String service, QueryOptions queryOptions,
                                       ConsulResponseCallback<List<ServiceHealth>> callback) {
        extractConsulResponse(api.getServiceInstances(service, queryOptions.toQuery()), callback);
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
                                              @QueryMap Map<String, Object> query);

        @GET("health/checks/{service}")
        Call<List<HealthCheck>> getServiceChecks(@Path("service") String service,
                                                 @QueryMap Map<String, Object> query);

        @GET("health/state/{state}")
        Call<List<HealthCheck>> getChecksByState(@Path("state") String state,
                                                 @QueryMap Map<String, Object> query);

        @GET("health/service/{service}")
        Call<List<ServiceHealth>> getServiceInstances(@Path("service") String service,
                                                      @QueryMap Map<String, Object> query);
    }
}
