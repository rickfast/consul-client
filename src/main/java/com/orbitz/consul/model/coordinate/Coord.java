package com.orbitz.consul.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableCoord.class)
@JsonDeserialize(as = ImmutableCoord.class)
public abstract class Coord {

    @JsonProperty("Adjustment")
    public abstract double getAdjustment();

    @JsonProperty("Error")
    public abstract double getError();

    @JsonProperty("Height")
    public abstract double getHeight();

    @JsonProperty("Vec")
    public abstract double[] getVec();

}
