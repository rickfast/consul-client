package ru.hh.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Optional;

import ru.hh.consul.model.agent.Check;
import ru.hh.consul.model.health.Service;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableCatalogRegistration.class)
@JsonDeserialize(as = ImmutableCatalogRegistration.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogRegistration {

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Datacenter")
    public abstract Optional<String> datacenter();

    @JsonProperty("Node")
    public abstract String node();

    @JsonProperty("Address")
    public abstract String address();

    @JsonProperty("NodeMeta")
    public abstract Map<String, String> nodeMeta();

    @JsonProperty("TaggedAddresses")
    public abstract Optional<TaggedAddresses> taggedAddresses();

    @JsonProperty("Service")
    public abstract Optional<Service> service();

    @JsonProperty("Check")
    public abstract Optional<Check> check();

    @JsonProperty("WriteRequest")
    public abstract Optional<WriteRequest> writeRequest();

    @JsonProperty("SkipNodeUpdate")
    public abstract Optional<Boolean> skipNodeUpdate();
}
