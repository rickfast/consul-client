package com.orbitz.consul;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class StatusClient {

    private WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    StatusClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Retrieves the host/port of the Consul leader.
     *
     * GET /v1/status/leader
     *
     * @return The host/port of the leader.
     */
    public String getLeader() {
        return webTarget.path("leader").request().get(String.class)
                .replace("\"", "").trim();
    }

    /**
     * Retrieves a list of host/ports for raft peers.
     *
     * GET /v1/status/peers
     *
     * @return List of host/ports for raft peers.
     */
    public List<String> getPeers() {
        return webTarget.path("peers").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<String>>() {});
    }
}
