package com.orbitz.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCatalogDeregistration.class)
@JsonDeserialize(as = ImmutableCatalogDeregistration.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogDeregistration {

    @JsonProperty("Datacenter")
    public abstract Optional<String> datacenter();

    @JsonProperty("Node")
    public abstract String node();

    @JsonProperty("CheckID")
    public abstract Optional<String> checkId();

    @JsonProperty("ServiceID")
    public abstract Optional<String> serviceId();

    @JsonProperty("WriteRequest")
    public abstract Optional<WriteRequest> writeRequest();
}
