package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePorts.class)
@JsonDeserialize(as = ImmutablePorts.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Ports {

    @JsonProperty("DNS")
    public abstract int getDns();

    @JsonProperty("HTTP")
    public abstract int getHttp();

    @JsonProperty("RPC")
    public abstract int getRpc();

    @JsonProperty("SerfLan")
    public abstract int getSerfLan();

    @JsonProperty("SerfWan")
    public abstract int getSerfWan();

    @JsonProperty("Server")
    public abstract int getServer();
}
