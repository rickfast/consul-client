package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableTokenResponse.class)
@JsonDeserialize(as = ImmutableTokenResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TokenResponse {

    @JsonProperty("AccessorID")
    public abstract String accessorId();

    @JsonProperty("SecretID")
    public abstract String secretId();

    @JsonProperty("Description")
    public abstract Optional<String> description();

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
