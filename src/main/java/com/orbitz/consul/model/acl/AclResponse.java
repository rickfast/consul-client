package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

import java.math.BigInteger;


@Value.Immutable
@JsonSerialize(as = ImmutableAclResponse.class)
@JsonDeserialize(as = ImmutableAclResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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
