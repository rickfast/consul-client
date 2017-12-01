package com.orbitz.consul.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutablePreparedQuery.class)
@JsonSerialize(as = ImmutablePreparedQuery.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PreparedQuery {
    
    @JsonProperty("Template")
    public abstract Optional<Template> getTemplate();

    @JsonProperty("Name")
    public abstract String getName();

    @JsonProperty("Session")
    public abstract Optional<String> getSession();

    @JsonProperty("Token")
    public abstract Optional<String> getToken();

    @JsonProperty("Service")
    public abstract ServiceQuery getService();

    @JsonProperty("DNS")
    public abstract Optional<DnsQuery> getDns();
}
