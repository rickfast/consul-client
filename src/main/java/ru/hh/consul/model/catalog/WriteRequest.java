package ru.hh.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableWriteRequest.class)
@JsonDeserialize(as = ImmutableWriteRequest.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class WriteRequest {

    @JsonProperty("Token")
    public abstract String token();
}
