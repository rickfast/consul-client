package ru.hh.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableServiceCheck.class)
@JsonDeserialize(as = ImmutableServiceCheck.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ServiceCheck {

    @JsonProperty("Node")
    public abstract Node getNode();

    @JsonProperty("Service")
    public abstract Service getService();

    @JsonProperty("Checks")
    @JsonDeserialize(as = List.class, contentAs = HealthCheck.class)
    public abstract List<HealthCheck> getChecks();
}
