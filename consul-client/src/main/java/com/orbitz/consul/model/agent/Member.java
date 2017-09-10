package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableMember.class)
@JsonDeserialize(as = ImmutableMember.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Member {

    @JsonProperty("Name")
    public abstract String getName();

    @JsonProperty("Addr")
    public abstract String getAddress();

    @JsonProperty("Port")
    public abstract int getPort();

    @JsonProperty("Tags")
    @JsonDeserialize(as = ImmutableMap.class, keyAs = String.class, contentAs = String.class)
    public abstract Map<String, String> getTags();

    @JsonProperty("Status")
    public abstract int getStatus();

    @JsonProperty("ProtocolMin")
    public abstract int getProtocolMin();

    @JsonProperty("ProtocolMax")
    public abstract int getProtocolMax();

    @JsonProperty("ProtocolCur")
    public abstract int getProtocolCur();

    @JsonProperty("DelegateMin")
    public abstract int getDelegateMin();

    @JsonProperty("DelegateMax")
    public abstract int getDelegateMax();

    @JsonProperty("DelegateCur")
    public abstract int getDelegateCur();
}
