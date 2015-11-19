package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableService.class)
@JsonDeserialize(as = ImmutableService.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Service {

    @JsonProperty("ID")
    public abstract  String getId();

    @JsonProperty("Service")
    public abstract  String getService();

    @JsonProperty("Tags")
    @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
    public abstract List<String> getTags();
    
    @JsonProperty("Address")
    public abstract  String getAddress();

    @JsonProperty("Port")
    public abstract  int getPort();
}
