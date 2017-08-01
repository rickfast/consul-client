package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAclTokenId.class)
@JsonDeserialize(as = ImmutableAclTokenId.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AclTokenId {

    @JsonProperty("ID")
    public abstract String id();
}
