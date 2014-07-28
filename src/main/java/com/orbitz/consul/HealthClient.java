package com.orbitz.consul;

import com.orbitz.consul.model.health.ServiceHealth;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP Client for /v1/health/ endpoints.
 */
public class HealthClient {
    
    private WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    HealthClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Retrieves all passing nodes for a given service.
     *
     * GET /v1/health/service/{service}?passing=true
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.health.ServiceHealth} objects.
     */
    public List<ServiceHealth> getHealthyNodes(String service) {
        return Arrays.asList(webTarget.path("service").path(service).queryParam("passing", "true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(ServiceHealth[].class));
    }

    /**
     * Retrieves all nodes and health for a given service.
     *
     * GET /v1/health/service/{service}
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.health.ServiceHealth} objects.
     */
    public List<ServiceHealth> getServiceHealth(String service) {
        return Arrays.asList(webTarget.path("service").path(service).request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(ServiceHealth[].class));
    }
}
