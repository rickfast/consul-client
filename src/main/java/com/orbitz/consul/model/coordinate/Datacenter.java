package com.orbitz.consul.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(as = ImmutableDatacenter.class)
@JsonDeserialize(as = ImmutableDatacenter.class)
public abstract class Datacenter {

    @JsonProperty("Datacenter")
    public abstract String getDatacenter();

    @JsonProperty("Coordinates")
    public abstract List<Coordinate> getCoordinates();

}
