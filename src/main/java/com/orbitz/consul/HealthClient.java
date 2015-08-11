package com.orbitz.consul;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.QueryOptions;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static com.orbitz.consul.util.ClientUtil.response;

/**
 * HTTP Client for /v1/health/ endpoints.
 */
public class HealthClient {

    private static final GenericType<List<HealthCheck>> TYPE_HEALTH_CHECK_LIST =
            new GenericType<List<HealthCheck>>() {};
    private static final GenericType<List<ServiceHealth>> TYPE_SERVICE_HEALTH_LIST =
            new GenericType<List<ServiceHealth>>() {};
    private final WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    HealthClient(WebTarget webTarget) {
        this.webTarget = webTarget;
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
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, CatalogOptions catalogOptions, QueryOptions queryOptions) {
        return response(webTarget.path("node").path(node), catalogOptions, queryOptions,
                TYPE_HEALTH_CHECK_LIST);
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
        return response(webTarget.path("checks").path(service), catalogOptions, queryOptions,
                TYPE_HEALTH_CHECK_LIST);
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
        response(webTarget.path("checks").path(service), CatalogOptions.BLANK, queryOptions, TYPE_HEALTH_CHECK_LIST, callback);
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
        response(webTarget.path("checks").path(service), catalogOptions, queryOptions, TYPE_HEALTH_CHECK_LIST, callback);
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
        return response(webTarget.path("state").path(state.getName()), catalogOptions, queryOptions,
                TYPE_HEALTH_CHECK_LIST);
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
        return response(webTarget.path("service").path(service).queryParam("passing", "true"),
                catalogOptions, queryOptions, TYPE_SERVICE_HEALTH_LIST);
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
        response(webTarget.path("service").path(service).queryParam("passing", "true"),
                catalogOptions, queryOptions, TYPE_SERVICE_HEALTH_LIST, callback);
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
        response(webTarget.path("service").path(service).queryParam("passing", "true"),
                CatalogOptions.BLANK, queryOptions, TYPE_SERVICE_HEALTH_LIST, callback);
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
        return response(webTarget.path("service").path(service), catalogOptions, queryOptions,
                TYPE_SERVICE_HEALTH_LIST);
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
        response(webTarget.path("service").path(service), catalogOptions, queryOptions,
                TYPE_SERVICE_HEALTH_LIST, callback);
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
        response(webTarget.path("service").path(service), CatalogOptions.BLANK,
                queryOptions, TYPE_SERVICE_HEALTH_LIST, callback);
    }
}
