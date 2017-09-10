package com.orbitz.consul.model.operator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigInteger;
import java.util.List;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableRaftConfiguration.class)
@JsonSerialize(as = ImmutableRaftConfiguration.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RaftConfiguration {

    @JsonProperty("Servers")
    public abstract List<RaftServer> servers();

    @JsonProperty("Index")
    public abstract BigInteger index();
}
