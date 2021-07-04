package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public abstract class BaseRoleResponse {

    @JsonProperty("ID")
    public abstract String id();

    @JsonProperty("Name")
    public abstract String name();

    @JsonProperty("Description")
    public abstract String description();

    @JsonProperty("Policies")
    public abstract List<Role.RolePolicyLink> policies();

    @JsonProperty("ServiceIdentities")
    public abstract List<Role.RoleServiceIdentity> serviceIdentities();

    @JsonProperty("NodeIdentities")
    public abstract List<Role.RoleNodeIdentity> nodeIdentities();

    @JsonProperty("CreateIndex")
    public abstract BigInteger createIndex();

    @JsonProperty("ModifyIndex")
    public abstract BigInteger modifyIndex();

    @JsonProperty("Hash")
    public abstract String hash();

}
