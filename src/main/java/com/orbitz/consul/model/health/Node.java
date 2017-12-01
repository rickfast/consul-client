package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.orbitz.consul.model.catalog.TaggedAddresses;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableNode.class)
@JsonDeserialize(as = ImmutableNode.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Node {

    @JsonProperty("Node")
    public abstract String getNode();

    @JsonProperty("Address")
    public abstract String getAddress();
    
    @JsonProperty("Datacenter")
    public abstract Optional<String> getDatacenter();

    @JsonProperty("TaggedAddresses")
    public abstract Optional<TaggedAddresses> getTaggedAddresses();

    @JsonProperty("Meta")
    public abstract Optional<Map<String,String>> getNodeMeta();
}
