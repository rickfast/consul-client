package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableAgent.class)
@JsonDeserialize(as = ImmutableAgent.class)
public abstract class Agent {

    @JsonProperty("Config")
    public abstract Config getConfig();

    @JsonProperty("Member")
    public abstract Member getMember();

}