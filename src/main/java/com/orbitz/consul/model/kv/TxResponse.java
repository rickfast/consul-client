package com.orbitz.consul.model.kv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Map;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableTxResponse.class)
@JsonSerialize(as = ImmutableTxResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TxResponse {

    @JsonProperty("Results")
    public abstract List<Map<String, Value>> results();

    @JsonProperty("Errors")
    public abstract List<TxError> errors();
}
