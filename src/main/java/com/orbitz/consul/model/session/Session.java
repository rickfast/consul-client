package com.orbitz.consul.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSession.class)
@JsonDeserialize(as = ImmutableSession.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Session {

    @JsonProperty("LockDelay")
    public abstract Optional<String> getLockDelay();

    @JsonProperty("Name")
    public abstract Optional<String> getName();

    @JsonProperty("Node")
    public abstract Optional<String> getNode();

    @JsonProperty("Checks")
    public abstract List<String> getChecks();

    @JsonProperty("Behavior")
    public abstract Optional<String> getBehavior();

    @JsonProperty("TTL")
    public abstract Optional<String> getTtl();
}
