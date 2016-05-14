package com.orbitz.consul;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.util.Jackson;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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
     */
    private Consul(AgentClient agentClient, HealthClient healthClient, KeyValueClient keyValueClient, CatalogClient catalogClient, StatusClient statusClient, SessionClient sessionClient, EventClient eventClient, PreparedQueryClient preparedQueryClient) {
        this.agentClient = agentClient;
        this.healthClient = healthClient;
        this.keyValueClient = keyValueClient;
        this.catalogClient = catalogClient;
        this.statusClient = statusClient;
        this.sessionClient = sessionClient;
        this.eventClient = eventClient;
        this.preparedQueryClient = preparedQueryClient;
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
        private boolean ping = true;
        private Interceptor basicAuthInterceptor;
        private Interceptor aclTokenInterceptor;
        private OkHttpClient.Builder httpClientBuilder;
        private Long connectTimeoutMillis;
        private Long readTimeoutMillis;
        private Long writeTimeoutMillis;

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
         * Instructs the builder that the AgentClient should attempt a ping before returning the Consul instance
         *
         * @param ping Whether the ping should be done or not
         * @return The builder.
         */
        public Builder withPing(boolean ping) {
            this.ping = ping;

            return this;
        }

        /**
         * Sets the username and password to be used for basic authentication
         *
         * @param username the value of the username
         * @param password the value of the password
         * @return The builder.
         */
        public Builder withBasicAuth(String username, String password) {
            String credentials = username + ":" + password;
            final String basic = "Basic " + BaseEncoding.base64().encode(credentials.getBytes());
            basicAuthInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            return this;
        }

        /**
         * Sets the ACL token to be used with Consul
         *
         * @param token the value of the token
         * @return The builder.
         */
        public Builder withAclToken(final String token) {
            aclTokenInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    HttpUrl originalUrl = original.url();
                    String rewrittenUrl;
                    if (originalUrl.queryParameterNames().isEmpty()) {
                        rewrittenUrl = originalUrl.url().toExternalForm() + "?token=" + token;
                    } else {
                        rewrittenUrl = originalUrl.url().toExternalForm() + "&token=" + token;
                    }

                    Request.Builder requestBuilder = original.newBuilder()
                            .url(rewrittenUrl)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

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
         * Sets the {@link OkHttpClient.Builder} for the client.
         * @param builder OoHttpClient builder instance
         * @return The builder
         */
        public Builder withHttpClientBuilder(OkHttpClient.Builder builder) {
            Preconditions.checkNotNull(builder);
            this.httpClientBuilder = builder;

            return this;
        }


        /**
         * Connect timeout for OkHttpClient
         * @param timeoutMillis timeout values in milliseconds
         * @return The builder
         */
        public Builder withConnectTimeoutMillis(long timeoutMillis) {
            Preconditions.checkArgument(timeoutMillis >= 0, "Negative value");
            this.connectTimeoutMillis = timeoutMillis;

            return this;
        }

        /**
         * Read timeout for OkHttpClient
         * @param timeoutMillis timeout value in milliseconds
         * @return The builder
         */
        public Builder withReadTimeoutMillis(long timeoutMillis) {
            Preconditions.checkArgument(timeoutMillis >= 0, "Negative value");
            this.readTimeoutMillis = timeoutMillis;

            return this;
        }

        /**
         * Write timeout for OkHttpClient
         * @param timeoutMillis timeout value in milliseconds
         * @return The builder
         */
        public Builder withWriteTimeoutMillis(long timeoutMillis) {
            Preconditions.checkArgument(timeoutMillis >= 0, "Negative value");
            this.writeTimeoutMillis = timeoutMillis;

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
            final Retrofit retrofit;
            try {
                retrofit = createRetrofit(
                        this.url.toExternalForm(),
                        this.sslContext,
                        this.objectMapper,
                        this.httpClientBuilder);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            AgentClient agentClient = new AgentClient(retrofit);
            HealthClient healthClient = new HealthClient(retrofit);
            KeyValueClient keyValueClient = new KeyValueClient(retrofit);
            CatalogClient catalogClient = new CatalogClient(retrofit);
            StatusClient statusClient = new StatusClient(retrofit);
            SessionClient sessionClient = new SessionClient(retrofit);
            EventClient eventClient = new EventClient(retrofit);
            PreparedQueryClient preparedQueryClient = new PreparedQueryClient(retrofit);

            if (ping) {
                agentClient.ping();
            }
            return new Consul(agentClient, healthClient, keyValueClient, catalogClient, statusClient, sessionClient, eventClient, preparedQueryClient);
        }


        private Retrofit createRetrofit(String url, SSLContext sslContext, ObjectMapper mapper, OkHttpClient.Builder httpClientBuilder) throws MalformedURLException {
            final OkHttpClient.Builder builder;
            if (null != httpClientBuilder) {
                builder = httpClientBuilder;
            } else {
                builder = new OkHttpClient.Builder();

                if (basicAuthInterceptor != null) {
                    builder.addInterceptor(basicAuthInterceptor);
                }
                if (aclTokenInterceptor != null) {
                    builder.addInterceptor(aclTokenInterceptor);
                }

                if (sslContext != null) {
                    builder.sslSocketFactory(sslContext.getSocketFactory());
                }

                if (connectTimeoutMillis != null) {
                    builder.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS);
                }

                if (readTimeoutMillis != null) {
                    builder.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
                }

                if (writeTimeoutMillis != null) {
                    builder.writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS);
                }
            }

            final URL consulUrl = new URL(url);

            return new Retrofit.Builder()
                    .baseUrl(new URL(consulUrl.getProtocol(), consulUrl.getHost(),
                            consulUrl.getPort(), "/v1/").toExternalForm())
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .client(builder.build())
                    .build();
        }

    }
}
