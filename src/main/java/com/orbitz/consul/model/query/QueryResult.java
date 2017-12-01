package com.orbitz.consul.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableQueryResult.class)
@JsonDeserialize(as = ImmutableQueryResult.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class QueryResult {

    @JsonProperty("Node")
    public abstract Node getNode();

    @JsonProperty("Service")
    public abstract Service getService();

    @JsonProperty("Checks")
    @JsonDeserialize(as = ImmutableList.class, contentAs = HealthCheck.class)
    public abstract List<HealthCheck> getChecks();
    @JsonProperty("DNS")
    public abstract Optional<DnsQuery> getDns();

    @JsonProperty("Datacenters")
    public abstract Optional<String> datacenters();

    @JsonProperty("Failovers")
    public abstract Optional<Integer> failovers();
}
