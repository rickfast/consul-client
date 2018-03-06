package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.agent.*;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.QueryOptions;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP Client for /v1/agent/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#agent">The Consul API Docs</a>
 */
public class AgentClient extends BaseClient {

    private static String CLIENT_NAME = "agent";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    AgentClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
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
        try {
            retrofit2.Response<Void> response = api.ping().execute();

            if (!response.isSuccessful()) {
                throw new ConsulException(String.format("Error pinging Consul: %s",
                        response.message()));
            }
        } catch (Exception ex) {
            throw new ConsulException("Error connecting to Consul", ex);
        }
    }

    /**
     * Registers the client as a service with Consul with a ttl check.
     *
     * @param port The public facing port of the service to register with Consul.
     * @param ttl  Time to live in seconds for the Consul dead man's switch.
     * @param name Service name to register.
     * @param id   Service id to register.
     * @param tags Tags to register with.
     */
    public void register(int port, long ttl, String name, String id, String... tags) {
        Registration.RegCheck check = Registration.RegCheck.ttl(ttl);
        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul with a script based check.
     *
     * @param port     The public facing port of the service to register with Consul.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param name     Service name to register.
     * @param id       Service id to register.
     * @param tags     Tags to register with.
     */
    public void register(int port, String script, long interval, String name, String id, String... tags) {
        Registration.RegCheck check = Registration.RegCheck.script(script, interval);
        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul with an http based check
     *
     * @param port     The public facing port of the service to register with Consul.
     * @param http     Health check URL.
     * @param interval Health script run interval in seconds.
     * @param name     Service name to register.
     * @param id       Service id to register.
     * @param tags     Tags to register with.
     */
    public void register(int port, URL http, long interval, String name, String id, String... tags) {
        Registration.RegCheck check = Registration.RegCheck.http(http.toExternalForm(), interval);
        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul with a TCP based check
     *
     * @param port     The public facing port of the service to register with Consul.
     * @param tcp      Health check TCP host and port.
     * @param interval Health script run interval in seconds.
     * @param name     Service name to register.
     * @param id       Service id to register.
     * @param tags     Tags to register with.
     */
    public void register(int port, HostAndPort tcp, long interval, String name, String id, String... tags) {
        Registration.RegCheck check = Registration.RegCheck.tcp(tcp.toString(), interval);
        register(port, check, name, id, tags);
    }

    /**
     * Registers the client as a service with Consul with an existing {@link com.orbitz.consul.model.agent.Registration.RegCheck}
     *
     * @param port  The public facing port of the service to register with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name  Service name to register.
     * @param id    Service id to register.
     * @param tags  Tags to register with.
     */
    public void register(int port, Registration.RegCheck check, String name, String id, String... tags) {
        Registration registration = ImmutableRegistration
                .builder()
                .port(port)
                .check(Optional.ofNullable(check))
                .name(name)
                .id(id)
                .addTags(tags)
                .build();

        register(registration);
    }

    /**
     * Registers the client as a service with Consul with multiple checks
     *
     * @param port  The public facing port of the service to register with Consul.
     * @param checks The health checks to run periodically.
     * @param name  Service name to register.
     * @param id    Service id to register.
     * @param tags  Tags to register with.
     */
    public void register(int port, List<Registration.RegCheck> checks, String name, String id, String... tags) {
        Registration registration = ImmutableRegistration
                .builder()
                .port(port)
                .checks(checks)
                .name(name)
                .id(id)
                .addTags(tags)
                .build();

        register(registration);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param registration The registration payload.
     * @param options An optional QueryOptions instance.
     */
    public void register(Registration registration, QueryOptions options) {
        http.handle(api.register(registration, options.toQuery()));
    }

    public void register(Registration registration) {
        register(registration, QueryOptions.BLANK);
    }


    /**
     * De-register a particular service from the Consul Agent.
     */
    public void deregister(String serviceId, QueryOptions options) {
        http.handle(api.deregister(serviceId, options.toQuery()));
    }

    /**
     * De-register a particular service from the Consul Agent.
     */
    public void deregister(String serviceId) {
        deregister(serviceId, QueryOptions.BLANK);
    }

    /**
     * Registers a script Health Check with the Agent.
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
     * Registers an HTTP Health Check with the Agent.
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
     * Registers a TCP Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param tcp      Health check TCP host and port.
     * @param interval Health script run interval in seconds.
     */
    public void registerCheck(String checkId, String name, HostAndPort tcp, long interval) {
        registerCheck(checkId, name, tcp, interval, null);
    }

    /**
     * Registers a script Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, String script, long interval, String notes) {
        Check check = ImmutableCheck.builder()
            .id(checkId)
            .name(name)
            .script(script)
            .interval(String.format("%ss", interval))
            .notes(Optional.ofNullable(notes))
            .build();

        registerCheck(check);
    }

    /**
     * Registers a HTTP Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param http     Health check URL.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, URL http, long interval, String notes) {

        Check check = ImmutableCheck.builder()
                .id(checkId)
                .name(name)
                .http(http.toExternalForm())
                .interval(String.format("%ss", interval))
                .notes(Optional.ofNullable(notes))
                .build();

        registerCheck(check);
    }

    /**
     * Registers a TCP Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param tcp      Health check TCP host and port.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, HostAndPort tcp, long interval, String notes) {

        Check check = ImmutableCheck.builder()
                .id(checkId)
                .name(name)
                .tcp(tcp.toString())
                .interval(String.format("%ss", interval))
                .notes(Optional.ofNullable(notes))
                .build();

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

        Check check = ImmutableCheck.builder()
                .id(checkId)
                .name(name)
                .ttl(String.format("%ss", ttl))
                .notes(Optional.ofNullable(notes))
                .build();

        registerCheck(check);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param check The Check to register.
     */
    public void registerCheck(Check check) {
        http.handle(api.registerCheck(check));
    }

    /**
     * De-registers a Health Check with the Agent
     *
     * @param checkId the id of the Check to deregister
     */
    public void deregisterCheck(String checkId) {
        http.handle(api.deregisterCheck(checkId));
    }

    /**
     * Retrieves the Agent's configuration and member information.
     * <p/>
     * GET /v1/agent/self
     *
     * @return The Agent information.
     */
    public Agent getAgent() {
        return http.extract(api.getAgent());
    }

    /**
     * Retrieves all checks registered with the Agent.
     * <p/>
     * GET /v1/agent/checks
     *
     * @return Map of Check ID to Checks.
     */
    public Map<String, HealthCheck> getChecks() {
        return http.extract(api.getChecks());
    }

    /**
     * Retrieves all services registered with the Agent.
     * <p/>
     * GET /v1/agent/services
     *
     * @return Map of Service ID to Services.
     */
    public Map<String, Service> getServices() {
        return http.extract(api.getServices());
    }

    /**
     * Retrieves all members that the Agent can see in the gossip pool.
     * <p/>
     * GET /v1/agent/members
     *
     * @return List of Members.
     */
    public List<Member> getMembers() {
        return http.extract(api.getMembers());
    }

    /**
     * GET /v1/agent/force-leave/{node}
     * <p/>
     * Instructs the agent to force a node into the "left" state.
     *
     * @param node
     */
    public void forceLeave(String node) {
        http.handle(api.forceLeave());
    }

    /**
     * Checks in with Consul.
     *
     * @param checkId The Check ID to check in.
     * @param state   The current state of the Check.
     * @param note    Any note to associate with the Check.
     */
    public void check(String checkId, State state, String note) throws NotRegisteredException {
        try {
            Map<String, String> query = note == null ? Collections.emptyMap() : ImmutableMap.of("note", note);

            http.handle(api.check(state.getPath(), checkId, query));
        } catch (Exception ex) {
            throw new NotRegisteredException("Error checking state", ex);
        }
    }

    /**
     * Prepends the default TTL prefix to the serviceId to produce a check id,
     * then delegates to check(String checkId, State state, String note)
     * This method only works with TTL checks that have not been given a custom
     * name.
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
        Map<String, String> query = wan ? ImmutableMap.of("wan", "1") : Collections.emptyMap();
        boolean result = true;

        try {
            http.handle(api.join(address, query));
        } catch(Exception ex) {
            result = false;
        }

        return result;
    }

    /**
     * Toggles maintenance mode for a service ID.
     *
     * @param serviceId The service ID.
     * @param enable <code>true</code> if the service should be in
     *               maintenance mode, otherwise <code>false</code>.
     */
    public void toggleMaintenanceMode(String serviceId, boolean enable) {
        http.handle(api.toggleMaintenanceMode(serviceId,
                ImmutableMap.of("enable", Boolean.toString(enable))));
    }

    /**
     * Toggles maintenance mode for a service ID.
     *
     * @param serviceId The service ID.
     * @param enable <code>true</code> if the service should be in
     *               maintenance mode, otherwise <code>false</code>.
     * @param reason The reason for maintenance mode.
     */
    public void toggleMaintenanceMode(String serviceId,
                                      boolean enable,
                                      String reason) {
        http.handle(api.toggleMaintenanceMode(serviceId,
                ImmutableMap.of("enable", Boolean.toString(enable),
                                "reason", reason)));
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @PUT("agent/service/register")
        Call<Void> register(@Body Registration registration,
                            @QueryMap Map<String, Object> options);

        @PUT("agent/service/deregister/{serviceId}")
        Call<Void> deregister(@Path("serviceId") String serviceId, @QueryMap Map<String, Object> options);

        @PUT("agent/check/register")
        Call<Void> registerCheck(@Body Check check);

        @PUT("agent/check/deregister/{checkId}")
        Call<Void> deregisterCheck(@Path("checkId") String checkId);

        @GET("agent/self")
        Call<Void> ping();

        @GET("agent/self")
        Call<Agent> getAgent();

        @GET("agent/checks")
        Call<Map<String, HealthCheck>> getChecks();

        @GET("agent/services")
        Call<Map<String, Service>> getServices();

        @GET("agent/members")
        Call<List<Member>> getMembers();

        @PUT("agent/force-leave")
        Call<Void> forceLeave();

        @PUT("agent/check/{state}/{checkId}")
        Call<Void> check(@Path("state") String state,
                         @Path("checkId") String checkId,
                         @QueryMap Map<String, String> query);

        @PUT("agent/join/{address}")
        Call<Void> join(String address, Map<String, String> query);

        @PUT("agent/service/maintenance/{serviceId}")
        Call<Void> toggleMaintenanceMode(@Path("serviceId") String serviceId,
                                         @QueryMap Map<String, String> query);
    }
}
