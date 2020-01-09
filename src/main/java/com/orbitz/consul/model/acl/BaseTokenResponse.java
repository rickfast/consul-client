package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public abstract class BaseTokenResponse {

    @JsonProperty("AccessorID")
    public abstract String accessorId();

    @JsonProperty("Description")
    public abstract String description();

    @JsonProperty("Policies")
    public abstract List<Token.PolicyLink> policies();

    @JsonProperty("CreateIndex")
    public abstract BigInteger createIndex();

    @JsonProperty("ModifyIndex")
    public abstract BigInteger modifyIndex();

    @JsonProperty("Local")
    public abstract boolean local();

    @JsonProperty("CreateTime")
    public abstract Date createTime();

    @JsonProperty("Hash")
    public abstract String hash();

}
