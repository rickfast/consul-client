package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AclTokenId {

    @JsonProperty("ID")
    public abstract String id();
}
