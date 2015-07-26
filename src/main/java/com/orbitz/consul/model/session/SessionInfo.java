package com.orbitz.consul.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orbitz.consul.util.SecondsDeserializer;
import com.orbitz.consul.util.SecondsSerializer;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionInfo {

    @JsonProperty("CreateIndex")
    private Long createIndex;

    @JsonProperty("LockDelay")
    @JsonSerialize(using = SecondsSerializer.class)
    @JsonDeserialize(using = SecondsDeserializer.class)
    private Long lockDelay;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Node")
    private String node;

    @JsonProperty("Checks")
    private List<String> checks;

    @JsonProperty("Behavior")
    private String behavior;

    @JsonProperty("TTL")
    @JsonSerialize(using = SecondsSerializer.class)
    @JsonDeserialize(using = SecondsDeserializer.class)
    private Long ttl;

    @JsonProperty("ID")
    private String id;

    public Long getCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(Long createIndex) {
        this.createIndex = createIndex;
    }

    public Long getLockDelay() {
        return lockDelay;
    }

    public void setLockDelay(Long lockDelay) {
        this.lockDelay = lockDelay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<String> getChecks() {
        return checks;
    }

    public void setChecks(List<String> checks) {
        this.checks = checks;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
