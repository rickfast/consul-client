package com.orbitz.consul;

import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.agent.Check;
import com.orbitz.consul.model.agent.Member;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Service;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * HTTP Client for /v1/agent/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#agent">The Consul API Docs</a>
 */
public class AgentClient {

    private WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    AgentClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Indicates whether or not a particular service is registered with
     * the local Consul agent.
     *
     * @return <code>true</code> if a particular service is registered with
     * the local Consul agent, otherwise <code>false</code>.
     */
    public boolean isRegistered(String serviceId) {
        Map<String, Service> serviceIdToService = getServices();
        return serviceIdToService.containsKey(serviceId);
    }

    /**
     * Pings the Consul Agent.
     */
    public void ping() {
        Response response = null;

        try {
            response = webTarget.path("self").request().get();
            Response.StatusType status = response.getStatusInfo();

            if (status.getStatusCode() != Response.Status.OK.getStatusCode()) {
                throw new ConsulException(String.format("Error pinging Consul: %s",
                        status.getReasonPhrase()));
            }

            response.close();
        } catch (Exception ex) {
            throw new ConsulException("Error connecting to Consul", ex);
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param ttl  Time to live for the Consul dead man's switch.
     * @param name Service name to register.
     * @param id   Service id to register.
     * @param tags Tags to register with.
     */
    public void register(int port, long ttl, String name, String id, String... tags) {
        Registration.Check check = new Registration.Check();

        check.setTtl(String.format("%ss", ttl));

        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port     The public facing port of the service to register with Consul.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param name     Service name to register.
     * @param id       Service id to register.
     * @param tags     Tags to register with.
     */
    public void register(int port, String script, long interval, String name, String id, String... tags) {
        Registration.Check check = new Registration.Check();

        check.setScript(script);
        check.setInterval(String.format("%ss", interval));

        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port     The public facing port of the service to register with Consul.
     * @param http     Health check URL.
     * @param interval Health script run interval in seconds.
     * @param name     Service name to register.
     * @param id       Service id to register.
     * @param tags     Tags to register with.
     */
    public void register(int port, URL http, long interval, String name, String id, String... tags) {
        Registration.Check check = new Registration.Check();

        check.setHttp(http.toExternalForm());
        check.setInterval(String.format("%ss", interval));

        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port  The public facing port of the service to register with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name  Service name to register.
     * @param id    Service id to register.
     * @param tags  Tags to register with.
     */
    public void register(int port, Registration.Check check, String name, String id, String... tags) {
        Registration registration = new Registration();

        registration.setPort(port);
        registration.setCheck(check);
        registration.setName(name);
        registration.setId(id);
        registration.setTags(tags);

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

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * De-register a particular service from the Consul Agent.
     */
    public void deregister(String serviceId) {
        Response response = webTarget.path("service").path("deregister").path(serviceId)
                .request().get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     */
    public void registerCheck(String checkId, String name, String script, long interval) {
        registerCheck(checkId, name, script, interval, null);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param http     Health check URL.
     * @param interval Health script run interval in seconds.
     */
    public void registerCheck(String checkId, String name, URL http, long interval) {
        registerCheck(checkId, name, http, interval, null);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, String script, long interval, String notes) {
        Check check = new Check();

        check.setId(checkId);
        check.setName(name);
        check.setScript(script);
        check.setInterval(String.format("%ss", interval));
        check.setNotes(notes);

        registerCheck(check);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param http     Health check URL.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, URL http, long interval, String notes) {
        Check check = new Check();

        check.setId(checkId);
        check.setName(name);
        check.setHttp(http.toExternalForm());
        check.setInterval(String.format("%ss", interval));
        check.setNotes(notes);

        registerCheck(check);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId The Check ID to use.  Must be unique for the Agent.
     * @param name    The Check Name.
     * @param ttl     Time to live for the Consul dead man's switch.
     */
    public void registerCheck(String checkId, String name, long ttl) {
        registerCheck(checkId, name, ttl, null);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId The Check ID to use.  Must be unique for the Agent.
     * @param name    The Check Name.
     * @param ttl     Time to live for the Consul dead man's switch.
     * @param notes   Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, long ttl, String notes) {
        Check check = new Check();

        check.setId(checkId);
        check.setName(name);
        check.setTtl(String.format("%ss", ttl));
        check.setNotes(notes);

        registerCheck(check);
    }



    /**
     * Registers a Health Check with the Agent.
     *
     * @param check The Check to register.
     */
    public void registerCheck(Check check) {
        Response response = webTarget.path("check").path("register").request()
                .put(Entity.entity(check, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * De-registers a Health Check with the Agent
     *
     * @param checkId the id of the Check to deregister
     */
    public void deregisterCheck(String checkId) {
        Response response = webTarget.path("check").path("deregister").path(checkId)
                .request().get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * Retrieves the Agent's configuration and member information.
     * <p/>
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
     * <p/>
     * GET /v1/agent/checks
     *
     * @return Map of Check ID to Checks.
     */
    public Map<String, HealthCheck> getChecks() {
        return webTarget.path("checks").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, HealthCheck>>() {
                });
    }

    /**
     * Retrieves all services registered with the Agent.
     * <p/>
     * GET /v1/agent/services
     *
     * @return Map of Service ID to Services.
     */
    public Map<String, Service> getServices() {
        return webTarget.path("services").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, Service>>() {
                });
    }

    /**
     * Retrieves all members that the Agent can see in the gossip pool.
     * <p/>
     * GET /v1/agent/members
     *
     * @return List of Members.
     */
    public List<Member> getMembers() {
        return webTarget.path("members").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Member>>() {
                });
    }

    /**
     * GET /v1/agent/force-leave/{node}
     * <p/>
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
     * @param state   The current state of the Check.
     * @param note    Any note to associate with the Check.
     */
    public void check(String checkId, State state, String note) throws NotRegisteredException {
        WebTarget resource = webTarget.path("check").path(state.getPath());

        if (note != null) {
            resource = resource.queryParam("note", note);
        }

        try {
            resource.path(checkId).request()
                    .get(String.class);
        } catch (InternalServerErrorException ex) {
            throw new NotRegisteredException();
        }
    }

    /**
     * Prepends the default TTL prefix to the serviceId to produce a check id,
     * then delegates to check(String checkId, State state, String note)
     * This method only works with TTL checks that have not been given a custom
     * name.
     *
     * @param serviceId
     * @param state
     * @param note
     * @throws NotRegisteredException
     */
    public void checkTtl(String serviceId, State state, String note) throws NotRegisteredException {
        check("service:" + serviceId, state, note);
    }

    /**
     * Sets a TTL service check to "passing" state
     */
    public void pass(String serviceId) throws NotRegisteredException {
        checkTtl(serviceId, State.PASS, null);
    }

    /**
     * Sets a TTL service check to "passing" state with a note
     */
    public void pass(String serviceId, String note) throws NotRegisteredException {
        checkTtl(serviceId, State.PASS, note);
    }

    /**
     * Sets a TTL service check to "warning" state.
     */
    public void warn(String serviceId) throws NotRegisteredException {
        checkTtl(serviceId, State.WARN, null);
    }

    /**
     * Sets a TTL service check to "warning" state with a note.
     */
    public void warn(String serviceId, String note) throws NotRegisteredException {
        checkTtl(serviceId, State.WARN, note);
    }

    /**
     * Sets a TTL service check to "critical" state.
     */
    public void fail(String serviceId) throws NotRegisteredException {
        checkTtl(serviceId, State.FAIL, null);
    }

    /**
     * Sets a TTL service check to "critical" state with a note.
     */
    public void fail(String serviceId, String note) throws NotRegisteredException {
        checkTtl(serviceId, State.FAIL, note);
    }

    /**
     * Sets a TTL check to "passing" state
     */
    public void passCheck(String checkId) throws NotRegisteredException {
        check(checkId, State.PASS, null);
    }

    /**
     * Sets a TTL check to "passing" state with a note
     */
    public void passCheck(String checkId, String note) throws NotRegisteredException {
        check(checkId, State.PASS, note);
    }

    /**
     * Sets a TTL check to "warning" state.
     */
    public void warnCheck(String checkId) throws NotRegisteredException {
        check(checkId, State.WARN, null);
    }

    /**
     * Sets a TTL check to "warning" state with a note.
     */
    public void warnCheck(String checkId, String note) throws NotRegisteredException {
        check(checkId, State.WARN, note);
    }

    /**
     * Sets a TTL check to "critical" state.
     */
    public void failCheck(String checkId) throws NotRegisteredException {
        check(checkId, State.FAIL, null);
    }

    /**
     * Sets a TTL check to "critical" state with a note.
     */
    public void failCheck(String checkId, String note) throws NotRegisteredException {
        check(checkId, State.FAIL, note);
    }

    /**
     * GET /v1/agent/join/{address}
     *
     * Instructs the agent to join a node.
     *
     * @param address The address to join.
     * @return <code>true</code> if successful, otherwise <code>false</code>.
     */
    public boolean join(String address) {
        return join(address, false);
    }

    /**
     * GET /v1/agent/join/{address}?wan=1
     *
     * Instructs the agent to join a node.
     *
     * @param address The address to join.
     * @param wan Use WAN pool.
     * @return <code>true</code> if successful, otherwise <code>false</code>.
     */
    public boolean join(String address, boolean wan) {
        WebTarget resource = webTarget.path("join").path(address);

        if (wan) {
            resource = resource.queryParam("wan", "1");
        }

        try {
            return resource.request().get().getStatus() == Response.Status.OK.getStatusCode();
        } catch (InternalServerErrorException ex) {
            return false;
        }
    }
}
