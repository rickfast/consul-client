package com.orbitz.consul;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogDeregistration;
import com.orbitz.consul.model.catalog.CatalogNode;
import com.orbitz.consul.model.catalog.CatalogRegistration;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.QueryOptions;
import com.orbitz.consul.util.Http;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.Http.extractConsulResponse;
import static com.orbitz.consul.util.Http.handle;

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
     * <p/>
     * GET /v1/catalog/datacenters
     *
     * @return A list of datacenter names.
     */
    public List<String> getDatacenters() {
        return Http.extract(api.getDatacenters());
    }

    /**
     * Retrieves all nodes.
     * <p/>
     * GET /v1/catalog/nodes
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes() {
        return getNodes(QueryOptions.BLANK);
    }

    /**
     * Retrieves all nodes for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     * <p/>
     * GET /v1/catalog/nodes?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a list of
     * {@link com.orbitz.consul.model.health.Node} objects.
     */
    public ConsulResponse<List<Node>> getNodes(QueryOptions queryOptions) {
        return extractConsulResponse(api.getNodes(queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta()));
    }

    /**
     * Asynchronously retrieves the nodes for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     * <p/>
     * GET /v1/catalog/nodes?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @param callback     Callback implemented by callee to handle results.
     *                     {@link com.orbitz.consul.model.health.Node} objects.
     */
    public void getNodes(QueryOptions queryOptions, ConsulResponseCallback<List<Node>> callback) {
        extractConsulResponse(api.getNodes(queryOptions.toQuery(), queryOptions.getTag(),
                queryOptions.getNodeMeta()), callback);
    }

    /**
     * Retrieves all services for a given datacenter.
     * <p/>
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices() {
        return getServices(QueryOptions.BLANK);
    }

    /**
     * Retrieves all services for a given datacenter.
     * <p/>
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(QueryOptions queryOptions) {
        return extractConsulResponse(api.getServices(queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta()));
    }

    /**
     * Retrieves a single service.
     * <p/>
     * GET /v1/catalog/service/{service}
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service) {
        return getService(service, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single service for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     * <p/>
     * GET /v1/catalog/service/{service}?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing
     * {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String service, QueryOptions queryOptions) {
        return extractConsulResponse(api.getService(service, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta()));
    }

    /**
     * Retrieves a single node.
     * <p/>
     * GET /v1/catalog/node/{node}
     *
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node) {
        return getNode(node, QueryOptions.BLANK);
    }

    /**
     * Retrieves a single node for a given datacenter with {@link com.orbitz.consul.option.QueryOptions}.
     * <p/>
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param queryOptions The Query Options to use.
     * @return A list of matching {@link com.orbitz.consul.model.catalog.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(String node, QueryOptions queryOptions) {
        return extractConsulResponse(api.getNode(node, queryOptions.toQuery(),
                queryOptions.getTag(), queryOptions.getNodeMeta()));
    }

    /**
     * Registers a service or node.
     * <p/>
     * PUT /v1/catalog/register
     *
     * @param registration A {@link CatalogRegistration}
     */
    public void register(CatalogRegistration registration) {
        handle(api.register(registration));
    }

    /**
     * Deregisters a service or node.
     * <p/>
     * PUT /v1/catalog/deregister
     *
     * @param deregistration A {@link CatalogDeregistration}
     */
    public void deregister(CatalogDeregistration deregistration) {
        handle(api.deregister(deregistration));
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("catalog/datacenters")
        Call<List<String>> getDatacenters();

        @GET("catalog/nodes")
        Call<List<Node>> getNodes(@QueryMap Map<String, Object> query,
                                  @Query("tag") List<String> tag,
                                  @Query("node-meta") List<String> nodeMeta);

        @GET("catalog/node/{node}")
        Call<CatalogNode> getNode(@Path("node") String node,
                                  @QueryMap Map<String, Object> query,
                                  @Query("tag") List<String> tag,
                                  @Query("node-meta") List<String> nodeMeta);

        @GET("catalog/services")
        Call<Map<String, List<String>>> getServices(@QueryMap Map<String, Object> query,
                                                    @Query("tag") List<String> tag,
                                                    @Query("node-meta") List<String> nodeMeta);

        @GET("catalog/service/{service}")
        Call<List<CatalogService>> getService(@Path("service") String service,
                                              @QueryMap Map<String, Object> queryMeta,
                                              @Query("tag") List<String> tag,
                                              @Query("node-meta") List<String> nodeMeta);

        @PUT("catalog/register")
        Call<Void> register(@Body CatalogRegistration registration);

        @PUT("catalog/deregister")
        Call<Void> deregister(@Body CatalogDeregistration deregistration);
    }
}