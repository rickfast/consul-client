package com.orbitz.consul;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.base.Predicate;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.util.Jackson;
import com.orbitz.consul.util.ObjectMapperContextResolver;

import javax.net.ssl.SSLContext;
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

    private final AgentClient agentClient;
    private final HealthClient healthClient;
    private final KeyValueClient keyValueClient;
    private final CatalogClient catalogClient;
    private final StatusClient statusClient;
    private final SessionClient sessionClient;
    private final EventClient eventClient;

    /**
     * Private constructor.
     *
     * @param url     The full URL of a running Consul instance.
     * @param builder JAX-RS client builder instance.
     */
    private Consul(String url, ClientBuilder builder, ObjectMapper mapper) {

        if (!FluentIterable.from(builder.getConfiguration().getClasses())
                      .filter(new Predicate<Class<?>>() {
                        @Override
                        public boolean apply(final Class<?> clazz) {
                            return JacksonJaxbJsonProvider.class.isAssignableFrom(clazz);
                        }
                    }).first().isPresent()) {
            builder.register(JacksonJaxbJsonProvider.class);
        }
        final Client client = builder
                .register(new ObjectMapperContextResolver(mapper))
                .build();

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
     * Creates a new client given a complete URL.
     *
     * @deprecated Use {@link Consul.Builder}
     *
     * @param url     The Consul API URL.
     * @param builder The JAX-RS client builder instance.
     * @return A new client.
     */
    @Deprecated
    public static Consul newClient(String url, ClientBuilder builder, ObjectMapper mapper) {
        return new Consul(url, builder, mapper);
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @deprecated Use {@link Consul.Builder}
     *
     * @param host    The Consul API hostname or IP.
     * @param port    The Consul port.
     * @param builder The JAX-RS client builder instance.
     * @return A new client.
     */
    @Deprecated
    public static Consul newClient(String host, int port, ClientBuilder builder, ObjectMapper mapper) {
        try {
            return new Consul(new URL("http", host, port, "").toString(), builder, mapper);
        } catch (MalformedURLException e) {
            throw new ConsulException("Bad Consul URL", e);
        }
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @deprecated Use {@link Consul.Builder}
     *
     * @param host The Consul API hostname or IP.
     * @param port The Consul port.
     * @return A new client.
     */
    @Deprecated
    public static Consul newClient(String host, int port) {
        return newClient(host, port, ClientBuilder.newBuilder(), Jackson.MAPPER);
    }

    /**
     * Creates a new client given a host and a port.
     *
     * @deprecated Use {@link Consul.Builder}
     *
     * @return A new client.
     */
    @Deprecated
    public static Consul newClient() {
        return newClient(DEFAULT_HTTP_HOST, DEFAULT_HTTP_PORT);
    }

    /**
     * Get the Agent HTTP client.
     * <p>
     * /v1/agent
     *
     * @return The Agent HTTP client.
     */
    public AgentClient agentClient() {
        return agentClient;
    }

    /**
     * Get the Catalog HTTP client.
     * <p>
     * /v1/catalog
     *
     * @return The Catalog HTTP client.
     */
    public CatalogClient catalogClient() {
        return catalogClient;
    }

    /**
     * Get the Health HTTP client.
     * <p>
     * /v1/health
     *
     * @return The Health HTTP client.
     */
    public HealthClient healthClient() {
        return healthClient;
    }

    /**
     * Get the Key/Value HTTP client.
     * <p>
     * /v1/kv
     *
     * @return The Key/Value HTTP client.
     */
    public KeyValueClient keyValueClient() {
        return keyValueClient;
    }

    /**
     * Get the Status HTTP client.
     * <p>
     * /v1/status
     *
     * @return The Status HTTP client.
     */
    public StatusClient statusClient() {
        return statusClient;
    }

    /**
     * Get the SessionInfo HTTP client.
     * <p>
     * /v1/session
     *
     * @return The SessionInfo HTTP client.
     */
    public SessionClient sessionClient() {
        return sessionClient;
    }

    /**
     * Get the Event HTTP client.
     * <p>
     * /v1/event
     *
     * @return The Event HTTP client.
     */
    public EventClient eventClient() {
        return eventClient;
    }

    /**
     * Creates a new {@link Builder} object.
     *
     * @return A new Consul builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Consul} client objects.
     */
    public static class Builder {
        private URL url;
        private Optional<SSLContext> sslContext = Optional.absent();
        private ObjectMapper objectMapper = Jackson.MAPPER;
        private ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        {
            try {
                url = new URL("http", "localhost", 8500, "");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Constructs a new builder.
         */
        Builder() {

        }

        /**
         * Sets the URL from a {@link URL} object.
         *
         * @param url The Consul agent URL.
         * @return The builder.
         */
        public Builder withUrl(URL url) {
            this.url = url;

            return this;
        }

        /**
         * Sets the URL from a {@link HostAndPort} object.
         *
         * @param hostAndPort The Consul agent host and port.
         * @return The builder.
         */
        public Builder withHostAndPort(HostAndPort hostAndPort) {
            try {
                this.url = new URL("http", hostAndPort.getHostText(), hostAndPort.getPort(), "");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            return this;
        }

        /**
         * Sets the URL from a string.
         *
         * @param url The Consul agent URL.
         * @return The builder.
         */
        public Builder withUrl(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            return this;
        }

        /**
         * Sets the {@link SSLContext} for the client.
         *
         * @param sslContext The SSL context for HTTPS agents.
         * @return The builder.
         */
        public Builder withSslContext(SSLContext sslContext) {
            this.sslContext = Optional.of(sslContext);

            return this;
        }

        /**
         * Sets the {@link ObjectMapper} for the client.
         *
         * @param objectMapper The {@link ObjectMapper} to use.
         * @return The builder.
         */
        public Builder withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;

            objectMapper.registerModule(new GuavaModule());

            return this;
        }

        /**
         * Sets the JAX-RS {@link ClientBuilder} to use.
         *
         * @param clientBuilder The JAX-RS builder.
         * @return This builder.
         */
        public Builder withClientBuilder(ClientBuilder clientBuilder) {
            this.clientBuilder = clientBuilder;

            return this;
        }

        /**
         * Constructs a new {@link Consul} client.
         *
         * @return A new Consul client.
         */
        public Consul build() {
            if (this.sslContext.isPresent()) {
                this.clientBuilder.sslContext(this.sslContext.get());
            }

            return new Consul(this.url.toExternalForm(), this.clientBuilder, this.objectMapper);
        }
    }
}
