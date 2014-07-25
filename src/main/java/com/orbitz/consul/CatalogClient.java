package com.orbitz.consul;

import com.orbitz.consul.model.catalog.ServiceNode;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class CatalogClient {
    
    private WebTarget webTarget;
    
    CatalogClient(WebTarget webTarget) {
        this.webTarget = webTarget;        
    }

    /**
     * Retrieves all nodes for a given service.
     *
     * GET /v1/catalog/service/{service}
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.catalog.ServiceNode} objects.
     */
    public ServiceNode[] getServiceNodes(String service) {
        return webTarget.path("service").path(service).request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(ServiceNode[].class);
    }
}
