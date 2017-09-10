package com.orbitz.consul.model.operator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableRaftServer.class)
@JsonSerialize(as = ImmutableRaftServer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RaftServer {

    @JsonProperty("ID")
    public abstract String id();

    @JsonProperty("Node")
    public abstract String node();

    @JsonProperty("Address")
    public abstract String address();

    @JsonProperty("Leader")
    public abstract Boolean leader();

    @JsonProperty("Voter")
    public abstract Boolean voter();
}
