package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutableTelemetry.class)
@JsonDeserialize(as = ImmutableTelemetry.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Telemetry {
    @JsonProperty(value = "StatsiteAddr")
    public abstract String getStatsiteAddr();

    @JsonProperty(value = "StatsdAddr")
    public abstract String getStatsdAddr();

    @JsonProperty(value = "StatsitePrefix")
    public abstract String getStatsitePrefix();

    @JsonProperty(value = "DisableHostname")
    public abstract Boolean getDisableHostname();

    @JsonProperty(value = "DogStatsdAddr")
    public abstract String getDogStatsdAddr();

    @JsonProperty(value = "DogStatsdTags")
    public abstract Optional<List<String>> getDogStatsdTags();
}
