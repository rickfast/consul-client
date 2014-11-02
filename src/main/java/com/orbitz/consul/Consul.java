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

    private AgentClient agentClient;
    private HealthClient healthClient;
    private KeyValueClient keyValueClient;
    private CatalogClient catalogClient;
    private StatusClient statusClient;

    /**
     * Private constructor.
     *
     * @param url The full URL of a running Consul instance.
     */
    private Consul(String url) {
        Client client = ClientBuilder.newBuilder().register(JacksonJaxbJsonProvider.class).build();

        this.agentClient = new AgentClient(client.target(url).path("v1").path("agent"));
        this.healthClient = new HealthClient(client.target(url).path("v1").path("health"));
        this.keyValueClient = new KeyValueClient(client.target(url).path("v1").path("kv"));
        this.catalogClient = new CatalogClient(client.target(url).path("v1").path("catalog"));
        this.statusClient = new StatusClient(client.target(url).path("v1").path("status"));
        agentClient.ping();
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @return A new client.
     */
    public static Consul newClient(String host, int port) {
        try {
            return new Consul(new URL("http", host, port, "").toString());
        } catch (MalformedURLException e) {
            throw new ConsulException("Bad Consul URL", e);
        }
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @return A new client.
     */
    public static Consul newClient() {
        return newClient("localhost", 8500);
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
}
