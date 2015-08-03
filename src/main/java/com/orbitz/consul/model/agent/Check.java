package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.immutables.value.Value;

import static com.google.common.base.Preconditions.checkState;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableCheck.class)
@JsonDeserialize(as = ImmutableCheck.class)
public abstract class Check {

    @JsonProperty("ID")
    public abstract String getId();

    @JsonProperty("Name")
    public abstract String getName();

    @JsonProperty("Notes")
    public abstract Optional<String> getNotes();

    @JsonProperty("Script")
    public abstract Optional<String> getScript();

    @JsonProperty("Interval")
    public abstract Optional<String> getInterval();

    @JsonProperty("TTL")
    public abstract Optional<String> getTtl();

    @JsonProperty("HTTP")
    public abstract Optional<String> getHttp();

    @JsonProperty("ServiceID")
    public abstract Optional<String> getServiceId();

    @Value.Check
    protected void validate() {

        checkState(getHttp().isPresent() || getTtl().isPresent() || getScript().isPresent(),
                "Check must specify either http, ttl, or script");

        if (getHttp().isPresent() || getScript().isPresent()) {
            checkState(getInterval().isPresent(),
                    "Interval must be set if check type is http or script");
        }

    }
    
}
