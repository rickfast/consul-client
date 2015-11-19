package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableNode.class)
@JsonDeserialize(as = ImmutableNode.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Node {

    @JsonProperty("Node")
    public abstract String getNode();

    @JsonProperty("Address")
    public abstract String getAddress();
}
