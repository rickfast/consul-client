package com.orbitz.consul;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogNode;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.Options;
import com.orbitz.consul.option.QueryOptions;
import com.orbitz.consul.util.Http;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.Http.extractConsulResponse;

/**
 * HTTP Client for /v1/catalog/ endpoints.
 */
public class CatalogClient {

    private final Api api;
    
    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    CatalogClient(Retrofit retrofit) {
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrieves all datacenters.
     *
     * GET /v1/catalog/datacenters
     *
     * @return A list of datacenter names.
     */
    public List<String> getDatacenters() {
        return Http.extract(api.getDatacenters());
    }

    /**
     * Retrieves all nodes.
     *
     * GET /v1/catalog/nodes
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes() {
        return getNodes(null, QueryOptions.BLANK);
    }

    /**
     * Retrieves all nodes for a given datacenter.
     *
     * GET /v1/catalog/nodes?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes(CatalogOptions catalogOptions) {
        return getNodes(catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves all nodes with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/nodes
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes(QueryOptions queryOptions) {
        return getNodes(null, queryOptions);
    }

    /**
     * Retrieves all nodes for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/nodes?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes(CatalogOptions catalogOptions, QueryOptions queryOptions) {
        return extractConsulResponse(api.getNodes(Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrieves all services for a given datacenter.
     *
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices() {
        return getServices(null, QueryOptions.BLANK);
    }

    /**
     * Retrieves all services for a given datacenter.
     *
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(CatalogOptions catalogOptions) {
        return getServices(catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves all services for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(QueryOptions queryOptions) {
        return getServices(null, queryOptions);
    }

    /**
     * Retrieves all services for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(CatalogOptions catalogOptions, QueryOptions queryOptions) {
        return extractConsulResponse(api.getServices(Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrieves a single service.
     *
     * GET /v1/catalog/service/{service}
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */

    /**
     * Asynchronously retrieves a single service
     *
     * GET /v1/catalog/service/{service}
     *
     * @param service      The service to query.
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @param callback       Callback implemented by callee to handle results.
     * {@link com.orbitz.consul.model.health.HealthCheck} objects.
     */
    public void getService(String service,
                           CatalogOptions catalogOptions,
                           QueryOptions queryOptions,
                           ConsulResponseCallback<List<CatalogService>> callback) {
        extractConsulResponse(api.getService(service, Options.from(catalogOptions, queryOptions)), callback);
    }

    /**
     * Retrieves a single service.
     *
     * GET /v1/catalog/service/{service}
     *
     * @param service      The service to query.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service) {
        return getService(service, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single service for a given datacenter.
     *
     * GET /v1/catalog/service/{service}?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service, CatalogOptions catalogOptions) {
        return getService(service, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single service with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/service/{service}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service, QueryOptions queryOptions) {
        return getService(service, null, queryOptions);
    }

    /**
     * Retrieves a single service for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/service/{service}?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service, CatalogOptions catalogOptions,
                                                           QueryOptions queryOptions) {
        return extractConsulResponse(api.getService(service, Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrieves a single node.
     *
     * GET /v1/catalog/node/{node}
     *
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node) {
        return getNode(node, null, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single node for a given datacenter.
     *
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node, CatalogOptions catalogOptions) {
        return getNode(node, catalogOptions, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single node with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/node/{node}
     *
     * @param queryOptions The Query Options to use.
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node, QueryOptions queryOptions) {
        return getNode(node, null, queryOptions);
    }

    /**
     * Retrieves a single node for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     *
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node, CatalogOptions catalogOptions, QueryOptions queryOptions) {
        return extractConsulResponse(api.getNode(node, Options.from(catalogOptions, queryOptions)));
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("catalog/datacenters")
        Call<List<String>> getDatacenters();

        @GET("catalog/nodes")
        Call<List<Node>> getNodes(@QueryMap Map<String, Object> query);

        @GET("catalog/node/{node}")
        Call<CatalogNode> getNode(@Path("node") String node,
                                  @QueryMap Map<String, Object> query);

        @GET("catalog/services")
        Call<Map<String, List<String>>> getServices(@QueryMap Map<String, Object> query);

        @GET("catalog/service/{service}")
        Call<List<CatalogService>> getService(@Path("service") String service,
                                              @QueryMap Map<String, Object> query);
    }
}