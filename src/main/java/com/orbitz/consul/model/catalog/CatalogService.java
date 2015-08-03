package com.orbitz.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableCatalogService.class)
@JsonDeserialize(as = ImmutableCatalogService.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogService {

    @JsonProperty("Node")
    public abstract String getNode();

    @JsonProperty("Address")
    public abstract String getAddress();

    @JsonProperty("ServiceName")
    public abstract String getServiceName();

    @JsonProperty("ServiceID")
    public abstract String getServiceId();

	@JsonProperty("ServiceAddress")
    public abstract String getServiceAddress();

    @JsonProperty("ServicePort")
    public abstract int getServicePort();

    @JsonProperty("ServiceTags")
    public abstract List<String> getServiceTags();
}
