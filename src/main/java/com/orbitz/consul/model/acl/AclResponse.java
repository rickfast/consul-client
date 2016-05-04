package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.math.BigInteger;

/**
 * Created by rfast on 5/4/16.
 */
public abstract class AclResponse {

    @JsonProperty("CreateIndex")
    public abstract BigInteger createIndex();

    @JsonProperty("ModifyIndex")
    public abstract BigInteger modifyIndex();

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Name")
    public abstract Optional<String> name();

    @JsonProperty("Type")
    public abstract Optional<String> type();

    @JsonProperty("Rules")
    public abstract Optional<String> rules();
}
