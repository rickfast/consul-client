package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePolicy.class)
@JsonDeserialize(as = ImmutablePolicy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Policy {

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Description")
    public abstract Optional<String> description();

    @JsonProperty("Name")
    public abstract String name();

    @JsonProperty("Rules")
    public abstract Optional<String> rules();

    @JsonProperty("Datacenters")
    @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
    public abstract Optional<List<String>> datacenters();

}
