package com.orbitz.consul.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.orbitz.consul.util.Base64EncodingDeserializer;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEvent.class)
@JsonDeserialize(as = ImmutableEvent.class)
public abstract class Event {

    @JsonProperty("ID")
    public abstract String getId();

    @JsonProperty("Name")
    public abstract String getName();

    @JsonProperty("Payload")
    @JsonDeserialize(using = Base64EncodingDeserializer.class)
    public abstract Optional<String> getPayload();

    @JsonProperty("NodeFilter")
    public abstract Optional<String> getNodeFilter();

    @JsonProperty("ServiceFilter")
    public abstract Optional<String> getServiceFilter();

    @JsonProperty("TagFilter")
    public abstract Optional<String> getTagFilter();

    @JsonProperty("Version")
    public abstract int getVersion();

    @JsonProperty("LTime")
    public abstract Long getLTime();

}
