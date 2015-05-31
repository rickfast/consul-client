package com.orbitz.consul.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.orbitz.consul.util.Base64EncodingDeserializer;

public class Event {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Payload")
    @JsonDeserialize(using = Base64EncodingDeserializer.class)
    private String payload;

    @JsonProperty("NodeFilter")
    private String nodeFilter;

    @JsonProperty("ServiceFilter")
    private String serviceFilter;

    @JsonProperty("TagFilter")
    private String tagFilter;

    @JsonProperty("Version")
    private int version;

    @JsonProperty("LTime")
    private long lTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getNodeFilter() {
        return nodeFilter;
    }

    public void setNodeFilter(String nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public String getServiceFilter() {
        return serviceFilter;
    }

    public void setServiceFilter(String serviceFilter) {
        this.serviceFilter = serviceFilter;
    }

    public String getTagFilter() {
        return tagFilter;
    }

    public void setTagFilter(String tagFilter) {
        this.tagFilter = tagFilter;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getlTime() {
        return lTime;
    }

    public void setlTime(long lTime) {
        this.lTime = lTime;
    }
}
