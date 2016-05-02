package com.orbitz.consul;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.util.Jackson;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
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
    private final PreparedQueryClient preparedQueryClient;

    /**
     * Private constructor.
     *
     * @param url     The full URL of a running Consul instance.
     * @param mapper  A Jackson {@link ObjectMapper}
     */
    private Consul(String url, SSLContext sslContext, ObjectMapper mapper) {

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            final URL consulUrl = new URL(url);

            if (sslContext != null) {
                builder.sslSocketFactory(sslContext.getSocketFactory());
            }

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(new URL(consulUrl.getProtocol(), consulUrl.getHost(),
                            consulUrl.getPort(), "/v1/").toExternalForm())
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .client(builder.build())
                    .build();

            this.agentClient = new AgentClient(retrofit);
            this.healthClient = new HealthClient(retrofit);
            this.keyValueClient = new KeyValueClient(retrofit);
            this.catalogClient = new CatalogClient(retrofit);
            this.statusClient = new StatusClient(retrofit);
            this.sessionClient = new SessionClient(retrofit);
            this.eventClient = new EventClient(retrofit);
            this.preparedQueryClient = new PreparedQueryClient(retrofit);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        agentClient.ping();
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
     * Get the Prepared Query HTTP client.
     * <p>
     * /v1/query
     *
     * @return The Prepared Query HTTP client.
     */
    public PreparedQueryClient preparedQueryClient() {
        return preparedQueryClient;
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
     * Used to create a default Consul client.
     *
     * @return A default {@link Consul} client.
     */
    @VisibleForTesting
    public static Consul newClient() {
        return builder().build();
    }

    /**
     * Builder for {@link Consul} client objects.
     */
    public static class Builder {
        private URL url;
        private SSLContext sslContext;
        private ObjectMapper objectMapper = Jackson.MAPPER;

        {
            try {
                url = new URL("http", DEFAULT_HTTP_HOST, DEFAULT_HTTP_PORT, "");
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
            this.sslContext = sslContext;

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
         * Constructs a new {@link Consul} client.
         *
         * @return A new Consul client.
         */
        public Consul build() {
            return new Consul(this.url.toExternalForm(), this.sslContext, this.objectMapper);
        }
    }
}
