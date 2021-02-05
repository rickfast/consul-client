package ru.hh.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonSerialize(as = ImmutableServiceProxy.class)
@JsonDeserialize(as = ImmutableServiceProxy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ServiceProxy {

    @JsonProperty("DestinationServiceName")
    public abstract String getDestinationServiceName();

    @JsonProperty("DestinationServiceID")
    public abstract String getDestinationServiceId();

    @JsonProperty("LocalServiceAddress")
    public abstract String getLocalServiceAddress();

    @JsonProperty("LocalServicePort")
    public abstract int getLocalServicePort();

    @JsonProperty("Config")
    public abstract Map<String, String> getConfig();

    @JsonProperty("Upstreams")
    @JsonDeserialize(as = ImmutableList.class, contentAs = ServiceProxyUpstream.class)
    public abstract List<ServiceProxyUpstream> getUpstreams();

}
