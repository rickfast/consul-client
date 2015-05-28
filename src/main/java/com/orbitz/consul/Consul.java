package com.orbitz.consul;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Client for interacting with the Consul HTTP API.
 *
 * @author rfast
 */
public class Consul {

    /**
     * Default Consul HTTP API host.
     */
    public static final String DEFAULT_HTTP_HOST = "localhost";

    /**
     * Default Consul HTTP API port.
     */
    public static final int DEFAULT_HTTP_PORT = 8500;

    private AgentClient agentClient;
    private HealthClient healthClient;
    private KeyValueClient keyValueClient;
    private CatalogClient catalogClient;
    private StatusClient statusClient;
    private SessionClient sessionClient;
    private EventClient eventClient;

    /**
     * Private constructor.
     *
     * @param url The full URL of a running Consul instance.
     * @param builder JAX-RS client builder instance.
     */
    private Consul(String url, ClientBuilder builder) {
        Client client = builder.register(JacksonJaxbJsonProvider.class).build();

        this.agentClient = new AgentClient(client.target(url).path("v1").path("agent"));
        this.healthClient = new HealthClient(client.target(url).path("v1").path("health"));
        this.keyValueClient = new KeyValueClient(client.target(url).path("v1").path("kv"));
        this.catalogClient = new CatalogClient(client.target(url).path("v1").path("catalog"));
        this.statusClient = new StatusClient(client.target(url).path("v1").path("status"));
        this.sessionClient = new SessionClient(client.target(url).path("v1").path("session"));
        this.eventClient = new EventClient(client.target(url).path("v1").path("event"));

        agentClient.ping();
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @param builder The JAX-RS client builder instance.
     * @return A new client.
     */
    public static Consul newClient(String host, int port, ClientBuilder builder) {
        try {
            return new Consul(new URL("http", host, port, "").toString(), builder);
        } catch (MalformedURLException e) {
            throw new ConsulException("Bad Consul URL", e);
        }
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @return A new client.
     */
    public static Consul newClient(String host, int port) {
        return newClient(host, port, ClientBuilder.newBuilder());
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @return A new client.
     */
    public static Consul newClient() {
        return newClient(DEFAULT_HTTP_HOST, DEFAULT_HTTP_PORT);
    }

    /**
     * Get the Agent HTTP client.
     *
     * /v1/agent
     *
     * @return The Agent HTTP client.
     */
    public AgentClient agentClient() {
        return agentClient;
    }

    /**
     * Get the Catalog HTTP client.
     *
     * /v1/catalog
     *
     * @return The Catalog HTTP client.
     */
    public CatalogClient catalogClient() {
        return catalogClient;
    }

    /**
     * Get the Health HTTP client.
     *
     * /v1/health
     *
     * @return The Health HTTP client.
     */
    public HealthClient healthClient() {
        return healthClient;
    }

    /**
     * Get the Key/Value HTTP client.
     *
     * /v1/kv
     *
     * @return The Key/Value HTTP client.
     */
    public KeyValueClient keyValueClient() {
        return keyValueClient;
    }

    /**
     * Get the Status HTTP client.
     *
     * /v1/status
     *
     * @return The Status HTTP client.
     */
    public StatusClient statusClient() {
        return statusClient;
    }

    /**
     * Get the SessionInfo HTTP client.
     *
     * /v1/session
     *
     * @return The SessionInfo HTTP client.
     */
    public SessionClient sessionClient() {
        return sessionClient;
    }

    /**
     * Get the Event HTTP client.
     *
     * /v1/event
     *
     * @return The Event HTTP client.
     */
    public EventClient eventClient() {
        return eventClient;
    }
}
