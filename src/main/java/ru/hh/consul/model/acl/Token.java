package ru.hh.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableToken.class)
@JsonDeserialize(as = ImmutableToken.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Token {

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Description")
    public abstract Optional<String> description();

    @JsonProperty("Local")
    public abstract boolean local();

    @JsonProperty("Policies")
    @JsonDeserialize(as = List.class, contentAs = PolicyLink.class)
    public abstract List<PolicyLink> policies();

    @Value.Immutable
@Value.Style(jdkOnly = true)
    @JsonSerialize(as = ImmutablePolicyLink.class)
    @JsonDeserialize(as = ImmutablePolicyLink.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class PolicyLink {

        @JsonProperty("ID")
        public abstract Optional<String> id();

        @JsonProperty("Name")
        public abstract Optional<String> name();
    }
}
