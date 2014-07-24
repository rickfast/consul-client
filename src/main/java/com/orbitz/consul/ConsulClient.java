package com.orbitz.consul;

import com.orbitz.consul.model.Registration;
import com.orbitz.consul.model.ServiceHealth;
import com.orbitz.consul.model.ServiceNode;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.Agent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Client for interacting with the Consul HTTP API.
 *
 * @author rfast
 */
public class ConsulClient {

    private Client client;
    private String url;
    private Optional<String> checkId;
    private Optional<Registration.Check> check = Optional.empty();
    private Optional<Registration> registration = Optional.empty();

    /**
     * Private constructor.
     *
     * @param url The full URL of a running Consul instance.
     */
    private ConsulClient(String url) {
        this.client = ClientBuilder.newClient();
        this.url = url;
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @return A new client.
     */
    public static ConsulClient newClient(String host, int port) {
        try {
            return new ConsulClient(new URL("http", host, port, "").toString());
        } catch (MalformedURLException e) {
            throw new ConsulException("Bad Consul URL", e);
        }
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @return A new client.
     */
    public static ConsulClient newClient() {
        return newClient("localhost", 8500);
    }

    /**
     * Indicates whether or not this client instance is registered with
     * Consul.
     *
     * @return <code>true</code> if this client instance is registered with
     * Consul, otherwise <code>false</code>.
     */
    public boolean isRegistered() {
        return check.isPresent() && registration.isPresent();
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param ttl Time to live for the Consul dead man's switch.
     * @param name Service name to register.
     * @param id Service id to register.
     *
     * @see #check(java.util.Optional, com.orbitz.consul.model.State, String)
     */
    public void register(int port, long ttl, String name, String id) {
        check = Optional.of(new Registration.Check());
        checkId = Optional.of(String.format("service:%s", id));

        check.get().setTtl(String.format("%ss", ttl));

        register(port, check.get(), name, id);
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
     *
     * @see #check(java.util.Optional, com.orbitz.consul.model.State, String)
     */
    public void register(int port, String script, long interval, String name, String id) {
        check = Optional.of(new Registration.Check());
        checkId = Optional.of(String.format("service:%s", id));

        check.get().setScript(script);
        check.get().setInterval(String.format("%ss", interval));

        register(port, check.get(), name, id);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name Service name to register.
     * @param id Service id to register.
     *
     * @see #check(java.util.Optional, com.orbitz.consul.model.State, String)
     */
    public void register(int port, Registration.Check check, String name, String id) {
        Registration registration = new Registration();

        registration.setPort(port);
        registration.setCheck(check);
        registration.setName(name);
        registration.setId(id);

        register(registration);

        this.registration = Optional.of(registration);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param registration The registration payload.
     */
    public void register(Registration registration) {
        Response response = getAgentPath().path("service").path("register").request()
                .put(Entity.entity(registration, "application/json"));

        if(response.getStatus() != 200) {
            throw new ConsulException(response.readEntity(String.class));
        };
    }

    /**
     * Retrieves the Agent's configuration and member information.
     *
     * /v1/agent/self
     *
     * @return The Agent information.
     */
    public Agent getAgent() {
        return getAgentPath().path("self").request().accept("application/json")
                .get(Agent.class);
    }

    /**
     * Checks in with Consul.
     *
     * @param checkId The Check ID to check in.
     * @param state The current state of the Check.
     * @param note Any note to associate with the Check.
     */
    public void check(Optional<String> checkId, State state, String note) {
        if(isRegistered()) {
            WebTarget resource = getAgentPath().path("check").path(state.getPath());

            if(note != null) {
                resource.queryParam("note", note);
            }

            resource.path(checkId.orElse(this.checkId.get())).request().get();
        }
    }

    /**
     * Checks in with Consul for the default Check.
     *
     * @param state The current state of the Check.
     * @param note Any note to associate with the Check.
     */
    public void check(State state, String note) {
        check(Optional.<String>empty(), state, note);
    }

    /**
     * Checks in with Consul for the default Check and "pass" state.
     */
    public void pass() {
        check(Optional.<String>empty(), State.PASS, null);
    }

    /**
     * Checks in with Consul for the default Check and "warn" state.
     */
    public void warn(String note) {
        check(Optional.<String>empty(), State.WARN, note);
    }

    /**
     * Checks in with Consul for the default Check and "fail" state.
     */
    public void fail(String note) {
        check(Optional.<String>empty(), State.FAIL, note);
    }

    /**
     * Retrieves all nodes for a given service.
     *
     * /v1/catalog/service/{service}
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.ServiceNode} objects.
     */
    public ServiceNode[] getServiceNodes(String service) {
        return getCatalogPath().path("service").path(service).request()
                .accept("application/json").get(ServiceNode[].class);
    }

    /**
     * Retrieves all passing nodes for a given service.
     *
     * /v1/health/service/{service}?passing=true
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.ServiceHealth} objects.
     */
    public ServiceHealth[] getPassingNodes(String service) {
        return getHealthPath().path("service").path(service).queryParam("passing", "true").request()
                .accept("application/json").get(ServiceHealth[].class);
    }

    /**
     * Retrieves all nodes and health for a given service.
     *
     * /v1/health/service/{service}
     *
     * @param service The name of the service to retrieve.
     * @return An array of {@link com.orbitz.consul.model.ServiceHealth} objects.
     */
    public ServiceHealth[] getServiceHealth(String service) {
        return getHealthPath().path("service").path(service).request()
                .accept("application/json").get(ServiceHealth[].class);
    }

    /**
     * Creates a {@link javax.ws.rs.client.WebTarget} for the Consul agent
     * endpoints.
     *
     * /v1/agent
     *
     * @return A {@link javax.ws.rs.client.WebTarget} for the Consul agent
     * endpoints.
     */
    private WebTarget getAgentPath() {
        return client.target(url).path("v1").path("agent");
    }

    /**
     * Creates a {@link javax.ws.rs.client.WebTarget} for the Consul catalog
     * endpoints.
     *
     * /v1/catalog
     *
     * @return A {@link javax.ws.rs.client.WebTarget} for the Consul catalog
     * endpoints.
     */
    private WebTarget getCatalogPath() {
        return client.target(url).path("v1").path("catalog");
    }

    /**
     * Creates a {@link javax.ws.rs.client.WebTarget} for the Consul health
     * endpoints.
     *
     * /v1/health
     *
     * @return A {@link javax.ws.rs.client.WebTarget} for the Consul health
     * endpoints.
     */
    private WebTarget getHealthPath() {
        return client.target(url).path("v1").path("health");
    }
}
