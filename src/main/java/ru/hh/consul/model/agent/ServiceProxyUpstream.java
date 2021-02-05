package ru.hh.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableServiceProxyUpstream.class)
@JsonDeserialize(as = ImmutableServiceProxyUpstream.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ServiceProxyUpstream {

    @JsonProperty("DestinationType")
    public abstract String getDestinationType();

    @JsonProperty("DestinationName")
    public abstract String getDestinationName();

    @JsonProperty("LocalBindPort")
    public abstract int getLocalBindPort();
}
