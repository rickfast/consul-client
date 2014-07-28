package com.orbitz.consul;

import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.agent.Member;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.Check;
import com.orbitz.consul.model.health.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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
     * Pings the Consul Agent.
     */
    public void ping() {
        try {
            Response.StatusType status = webTarget.path("self").request().get()
                    .getStatusInfo();

            if(status.getStatusCode() != Response.Status.OK.getStatusCode()) {
                throw new ConsulException(String.format("Error pinging Consul: %s", status.getReasonPhrase()));
            }
        } catch (Exception ex) {
            throw new ConsulException("Error connecting to Consul", ex);
        }
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
     * Retrieves all checks registered with the Agent.
     *
     * GET /v1/agent/checks
     *
     * @return Map of Check ID to Checks.
     */
    public Map<String, Check> getChecks() {
        return webTarget.path("checks").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, Check>>() {});
    }

    /**
     * Retrieves all services registered with the Agent.
     *
     * GET /v1/agent/services
     *
     * @return Map of Service ID to Services.
     */
    public Map<String, Service> getServices() {
        return webTarget.path("services").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, Service>>() {});
    }

    /**
     * Retrieves all members that the Agent can see in the gossip pool.
     *
     * GET /v1/agent/members
     *
     * @return List of Members.
     */
    public List<Member> getMembers() {
        return webTarget.path("members").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Member>>() {});
    }

    /**
     * GET /v1/agent/force-leave/{node}
     *
     * Instructs the agent to force a node into the "left" state.
     *
     * @param node
     */
    public void forceLeave(String node) {
        webTarget.path("force-leave").path(node).request().get();
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
