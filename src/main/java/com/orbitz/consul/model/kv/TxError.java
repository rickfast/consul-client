package com.orbitz.consul.model.kv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;

import java.math.BigInteger;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableTxError.class)
@JsonSerialize(as = ImmutableTxError.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TxError {

    @JsonProperty("OpIndex")
    public abstract Optional<BigInteger> opIndex();

    @JsonProperty("What")
    public abstract Optional<String> what();
}
