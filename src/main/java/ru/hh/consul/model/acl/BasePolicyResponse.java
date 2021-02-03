package ru.hh.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Optional;

public abstract class BasePolicyResponse {

    @JsonProperty("ID")
    public abstract String id();

    @JsonProperty("Name")
    public abstract String name();

    @JsonProperty("Datacenters")
    public abstract Optional<String> datacenters();

    @JsonProperty("Hash")
    public abstract String hash();

    @JsonProperty("CreateIndex")
    public abstract BigInteger createIndex();

    @JsonProperty("ModifyIndex")
    public abstract BigInteger modifyIndex();
}
