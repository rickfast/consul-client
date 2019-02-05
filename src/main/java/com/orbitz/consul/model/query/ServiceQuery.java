package com.orbitz.consul.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;

import java.util.List;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableServiceQuery.class)
@JsonSerialize(as = ImmutableServiceQuery.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ServiceQuery {

    @JsonProperty("Service")
    public abstract String getService();

    @JsonProperty("OnlyPassing")
    public abstract Optional<Boolean> getOnlyPassing();

    @JsonProperty("Tags")
    public abstract Optional<List<String>> getTags();

    @JsonProperty("Failover")
    public abstract Optional<Failover> getFailover();
}
