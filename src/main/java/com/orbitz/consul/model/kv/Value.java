package com.orbitz.consul.model.kv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.BaseEncoding;
import com.orbitz.consul.util.UnsignedLongDeserializer;

import java.nio.charset.Charset;
import java.util.Optional;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableValue.class)
@JsonSerialize(as = ImmutableValue.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Value {

    @JsonProperty("CreateIndex")
    public abstract long getCreateIndex();

    @JsonProperty("ModifyIndex")
    public abstract long getModifyIndex();

    @JsonProperty("LockIndex")
    public abstract long getLockIndex();

    @JsonProperty("Key")
    public abstract String getKey();

    @JsonProperty("Flags")
    @JsonDeserialize(using = UnsignedLongDeserializer.class)
    public abstract long getFlags();

    @JsonProperty("Value")
    public abstract Optional<String> getValue();

    @JsonProperty("Session")
    public abstract Optional<String> getSession();

    @JsonIgnore
    @org.immutables.value.Value.Lazy
    public Optional<String> getValueAsString() {
        return getValueAsString(Charset.defaultCharset());
    }

    @JsonIgnore
    @org.immutables.value.Value.Lazy
    public Optional<String> getValueAsString(Charset charset) {
        return getValue().map(s -> new String(BaseEncoding.base64().decode(s), charset));
    }

    @JsonIgnore
    @org.immutables.value.Value.Lazy
    public Optional<byte[]> getValueAsBytes() {
        return getValue().map(s -> BaseEncoding.base64().decode(s));
    }
}
