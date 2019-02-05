package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableConfig.class)
@JsonDeserialize(as = ImmutableConfig.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Config {

    @JsonProperty("Datacenter")
    public abstract String getDatacenter();

    @JsonProperty("NodeName")
    public abstract String getNodeName();

    @JsonProperty("Revision")
    public abstract String getRevision();

    @JsonProperty("Server")
    public abstract boolean getServer();

    @JsonProperty("Version")
    public abstract String getVersion();
}
