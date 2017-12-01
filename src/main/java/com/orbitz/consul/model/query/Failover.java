package com.orbitz.consul.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableFailover.class)
@JsonSerialize(as = ImmutableFailover.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Failover {

    @JsonProperty("NearestN")
    public abstract Optional<Integer> getNearestN();

    @JsonProperty("Datacenters")
    public abstract Optional<String> datacenters();
}
