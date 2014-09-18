package com.orbitz.consul;

import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogNode;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.QueryOptions;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.util.ClientUtil.response;

/**
 * HTTP Client for /v1/catalog/ endpoints.
 */
public class CatalogClient {
    
    private WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    CatalogClient(WebTarget webTarget) {
        this.webTarget = webTarget;        
    }

    /**
     * Retrieves all datacenters.
     *
     * GET /v1/catalog/datacenters
     *
     * @return A list of datacenter names.
     */
    public List<String> getDatacenters() {
        return webTarget.path("datacenters").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<String>>() {
                });
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
        return response(webTarget.path("nodes"), catalogOptions, queryOptions,
                new GenericType<List<Node>>() {});
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
        return response(webTarget.path("services"), catalogOptions, queryOptions,
                new GenericType<Map<String, List<String>>>() {
                });
    }

    /**
     * Retrieves a single service.
     *
     * GET /v1/catalog/service/{service}
     *
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
        return response(webTarget.path("service").path(service), catalogOptions, queryOptions,
                new GenericType<List<CatalogService>>() {});
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
        return response(webTarget.path("node").path(node), catalogOptions, queryOptions,
                new GenericType<CatalogNode>() {});
    }
}
