package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableToken.class)
@JsonDeserialize(as = ImmutableToken.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Token {

    @JsonProperty("AccessorID")
    public abstract Optional<String> id();

    @JsonProperty("SecretID")
    public abstract Optional<String> secretId();

    @JsonProperty("Description")
    public abstract Optional<String> description();

    @JsonProperty("Policies")
    @JsonDeserialize(as = ImmutableList.class, contentAs = PolicyLink.class)
    public abstract List<PolicyLink> policies();

    @JsonProperty("Roles")
    @JsonDeserialize(as = ImmutableList.class, contentAs = RoleLink.class)
    public abstract List<RoleLink> roles();

    @JsonProperty("ServiceIdentities")
    @JsonDeserialize(as = ImmutableList.class, contentAs = ServiceIdentity.class)
    public abstract List<ServiceIdentity> serviceIdentities();

    @JsonProperty("NodeIdentities")
    @JsonDeserialize(as = ImmutableList.class, contentAs = NodeIdentity.class)
    public abstract List<NodeIdentity> nodeIdentities();

    @JsonProperty("Local")
    public abstract boolean local();

    @JsonProperty("ExpirationTime")
    public abstract Optional<String> expirationTime();

    @JsonProperty("ExpirationTTL")
    public abstract Optional<String> expirationTTL();

    @JsonProperty("Namespace")
    public abstract Optional<String> namespace();

    @Value.Immutable
    @JsonSerialize(as = ImmutablePolicyLink.class)
    @JsonDeserialize(as = ImmutablePolicyLink.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class PolicyLink {

        @JsonProperty("ID")
        public abstract Optional<String> id();

        @JsonProperty("Name")
        public abstract Optional<String> name();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableRoleLink.class)
    @JsonDeserialize(as = ImmutableRoleLink.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class RoleLink {

        @JsonProperty("ID")
        public abstract Optional<String> id();

        @JsonProperty("Name")
        public abstract Optional<String> name();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableServiceIdentity.class)
    @JsonDeserialize(as = ImmutableServiceIdentity.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class ServiceIdentity {

        @JsonProperty("ServiceName")
        public abstract String name();

        @JsonProperty("Datacenters")
        @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
        public abstract List<String> datacenters();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableNodeIdentity.class)
    @JsonDeserialize(as = ImmutableNodeIdentity.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class NodeIdentity {

        @JsonProperty("NodeName")
        public abstract String name();

        @JsonProperty("Datacenter")
        public abstract String datacenter();
    }
}
