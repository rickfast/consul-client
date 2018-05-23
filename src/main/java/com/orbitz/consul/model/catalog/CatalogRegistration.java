package com.orbitz.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.orbitz.consul.model.agent.Check;
import com.orbitz.consul.model.health.Service;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCatalogRegistration.class)
@JsonDeserialize(as = ImmutableCatalogRegistration.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogRegistration {

    @JsonProperty("Datacenter")
    public abstract Optional<String> datacenter();

    @JsonProperty("Node")
    public abstract String node();

    @JsonProperty("Address")
    public abstract String address();

    @JsonProperty("TaggedAddresses")
    public abstract Optional<TaggedAddresses> taggedAddresses();

    @JsonProperty("Service")
    public abstract Optional<Service> service();

    @JsonProperty("Check")
    public abstract Optional<Check> check();

    @JsonProperty("WriteRequest")
    public abstract Optional<WriteRequest> writeRequest();
}
