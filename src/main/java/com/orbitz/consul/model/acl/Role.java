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
@JsonSerialize(as = ImmutableRole.class)
@JsonDeserialize(as = ImmutableRole.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Role {

    @JsonProperty("Name")
    public abstract String name();

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Description")
    public abstract Optional<String> description();

    @JsonProperty("Policies")
    @JsonDeserialize(as = ImmutableList.class, contentAs = RolePolicyLink.class)
    public abstract List<RolePolicyLink> policies();

    @JsonProperty("ServiceIdentities")
    @JsonDeserialize(as = ImmutableList.class, contentAs = RoleServiceIdentity.class)
    public abstract List<RoleServiceIdentity> serviceIdentities();

    @JsonProperty("NodeIdentities")
    @JsonDeserialize(as = ImmutableList.class, contentAs = RoleNodeIdentity.class)
    public abstract List<RoleNodeIdentity> nodeIdentities();

    @JsonProperty("Namespace")
    public abstract Optional<String> namespace();

    @Value.Immutable
    @JsonSerialize(as = ImmutableRolePolicyLink.class)
    @JsonDeserialize(as = ImmutableRolePolicyLink.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class RolePolicyLink {

        @JsonProperty("ID")
        public abstract Optional<String> id();

        @JsonProperty("Name")
        public abstract Optional<String> name();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableRoleServiceIdentity.class)
    @JsonDeserialize(as = ImmutableRoleServiceIdentity.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class RoleServiceIdentity {

        @JsonProperty("ServiceName")
        public abstract String name();

        @JsonProperty("Datacenters")
        @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
        public abstract List<String> datacenters();
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutableRoleNodeIdentity.class)
    @JsonDeserialize(as = ImmutableRoleNodeIdentity.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class RoleNodeIdentity {

        @JsonProperty("NodeName")
        public abstract String name();

        @JsonProperty("Datacenter")
        public abstract String datacenter();
    }
}
