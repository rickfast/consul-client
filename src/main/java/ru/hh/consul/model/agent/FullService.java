package ru.hh.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import ru.hh.consul.model.catalog.ServiceWeights;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableFullService.class)
@JsonDeserialize(as = ImmutableFullService.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class FullService {

    @JsonProperty("Kind")
    public abstract Optional<String> getKind();

    @JsonProperty("ID")
    public abstract String getId();

    @JsonProperty("Service")
    public abstract String getService();

    @JsonProperty("Tags")
    @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
    public abstract List<String> getTags();

    @JsonProperty("Meta")
    public abstract Map<String, String> getMeta();

    @JsonProperty("Port")
    public abstract int getPort();

    @JsonProperty("Address")
    public abstract String getAddress();

    @JsonProperty("Weights")
    public abstract Optional<ServiceWeights> getWeights();

    @JsonProperty("EnableTagOverride")
    public abstract Optional<Boolean> getEnableTagOverride();

    @JsonProperty("ContentHash")
    public abstract String getContentHash();

    @JsonProperty("Proxy")
    public abstract Optional<ServiceProxy> getProxy();
}
