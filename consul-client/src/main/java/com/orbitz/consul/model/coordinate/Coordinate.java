package com.orbitz.consul.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableCoordinate.class)
@JsonDeserialize(as = ImmutableCoordinate.class)
public abstract class Coordinate {

    @JsonProperty("Node")
    public abstract String getNode();

    @JsonProperty("Coord")
    public abstract Coord getCoord();

}
