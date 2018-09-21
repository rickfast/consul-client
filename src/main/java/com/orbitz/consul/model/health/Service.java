package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.orbitz.consul.model.catalog.ServiceWeights;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableService.class)
@JsonDeserialize(as = ImmutableService.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Service {

    @JsonProperty("ID")
    public abstract String getId();

    @JsonProperty("Service")
    public abstract String getService();

    @JsonProperty("EnableTagOverride")
    public abstract Optional<Boolean> getEnableTagOverride();

    @JsonProperty("Tags")
    @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
    public abstract List<String> getTags();
    
    @JsonProperty("Address")
    public abstract String getAddress();

    @JsonProperty("Meta")
    public abstract Map<String,String> getMeta();

    @JsonProperty("Port")
    public abstract int getPort();

    @JsonProperty("Weights")
    public abstract Optional<ServiceWeights> getWeights();
}
