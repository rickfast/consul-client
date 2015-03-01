package com.orbitz.consul.model.kv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.orbitz.consul.util.UnsignedLongDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {

    @JsonProperty("CreateIndex")
    private long createIndex;

    @JsonProperty("ModifyIndex")
    private long modifyIndex;

    @JsonProperty("LockIndex")
    private long lockIndex;

    @JsonProperty("Key")
    private String key;

    @JsonProperty("Flags")
    @JsonDeserialize(using=UnsignedLongDeserializer.class)
    private long flags;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("Session")
    private String session;

    public long getCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(long createIndex) {
        this.createIndex = createIndex;
    }

    public long getModifyIndex() {
        return modifyIndex;
    }

    public void setModifyIndex(long modifyIndex) {
        this.modifyIndex = modifyIndex;
    }

    public long getLockIndex() {
        return lockIndex;
    }

    public void setLockIndex(long lockIndex) {
        this.lockIndex = lockIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
