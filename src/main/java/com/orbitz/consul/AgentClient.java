package com.orbitz.consul;

import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Agent;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * HTTP Client for /v1/agent/ endpoints.
 */
class AgentClient {
    
    private WebTarget webTarget;
    private String checkId;
    private Registration.Check check;
    private boolean registered;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    AgentClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Indicates whether or not this client instance is registered with
     * Consul.
     *
     * @return <code>true</code> if this client instance is registered with
     * Consul, otherwise <code>false</code>.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param ttl Time to live for the Consul dead man's switch.
     * @param name Service name to register.
     * @param id Service id to register.
     */
    public void register(int port, long ttl, String name, String id) {
        check = new Registration.Check();
        checkId = String.format("service:%s", id);

        check.setTtl(String.format("%ss", ttl));

        register(port, check, name, id);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param script Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param name Service name to register.
     * @param id Service id to register.
     */
    public void register(int port, String script, long interval, String name, String id) {
        check = new Registration.Check();
        checkId = String.format("service:%s", id);

        check.setScript(script);
        check.setInterval(String.format("%ss", interval));

        register(port, check, name, id);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name Service name to register.
     * @param id Service id to register.
     */
    public void register(int port, Registration.Check check, String name, String id) {
        Registration registration = new Registration();

        registration.setPort(port);
        registration.setCheck(check);
        registration.setName(name);
        registration.setId(id);

        register(registration);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param registration The registration payload.
     */
    public void register(Registration registration) {
        Response response = webTarget.path("service").path("register").request()
                .put(Entity.entity(registration, MediaType.APPLICATION_JSON_TYPE));

        if(response.getStatus() != 200) {
            throw new ConsulException(response.readEntity(String.class));
        } else {
            registered = true;
        }
    }

    /**
     * Retrieves the Agent's configuration and member information.
     *
     * GET /v1/agent/self
     *
     * @return The Agent information.
     */
    public Agent getAgent() {
        return webTarget.path("self").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Agent.class);
    }

    /**
     * Checks in with Consul.
     *
     * @param checkId The Check ID to check in.
     * @param state The current state of the Check.
     * @param note Any note to associate with the Check.
     */
    public void check(String checkId, State state, String note) {
        if(isRegistered()) {
            WebTarget resource = webTarget.path("check").path(state.getPath());

            if(note != null) {
                resource = resource.queryParam("note", note);
            }

            resource.path(checkId == null ? this.checkId : checkId).request().get();
        }
    }

    /**
     * Checks in with Consul for the default Check.
     *
     * @param state The current state of the Check.
     * @param note Any note to associate with the Check.
     */
    public void check(State state, String note) {
        check(null, state, note);
    }

    /**
     * Checks in with Consul for the default Check and "pass" state.
     */
    public void pass() {
        check(null, State.PASS, null);
    }

    /**
     * Checks in with Consul for the default Check and "warn" state.
     */
    public void warn(String note) {
        check(null, State.WARN, note);
    }

    /**
     * Checks in with Consul for the default Check and "fail" state.
     */
    public void fail(String note) {
        check(null, State.FAIL, note);
    }
}
