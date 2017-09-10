package com.orbitz.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableCatalogNode.class)
@JsonDeserialize(as = ImmutableCatalogNode.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogNode {

    @JsonProperty("Node")
    public abstract Node getNode();

    @JsonProperty("Services")
    public abstract Map<String, Service> getServices();

}
