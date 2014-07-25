package com.orbitz.consul;

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

    /**
     * Private constructor.
     *
     * @param url The full URL of a running Consul instance.
     */
    private Consul(String url) {
        Client client = ClientBuilder.newClient();
        this.agentClient = new AgentClient(client.target(url).path("v1").path("agent"));
        this.healthClient = new HealthClient(client.target(url).path("v1").path("health"));
        this.keyValueClient = new KeyValueClient(client.target(url).path("v1").path("kv"));
        this.catalogClient = new CatalogClient(client.target(url).path("v1").path("catalog"));
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

    public AgentClient agentClient() {
        return agentClient;
    }

    public CatalogClient catalogClient() {
        return catalogClient;
    }

    public HealthClient healthClient() {
        return healthClient;
    }

    public KeyValueClient keyValueClient() {
        return keyValueClient;
    }
}
