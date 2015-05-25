package com.orbitz.consul.option;

public class EventOptionsBuilder {

    private String datacenter;
    private String nodeFilter;
    private String serviceFilter;
    private String tagFilter;

    private EventOptionsBuilder() {
        
    }
    
    public static EventOptionsBuilder builder() {
        return new EventOptionsBuilder();
    }

    public EventOptionsBuilder datacenter(String datacenter) {
        this.datacenter = datacenter;
        
        return this;
    }

    public EventOptionsBuilder nodeFilter(String nodeFilter) {
        this.nodeFilter = nodeFilter;

        return this;
    }

    public EventOptionsBuilder serviceFilter(String serviceFilter) {
        this.serviceFilter = serviceFilter;

        return this;
    }

    public EventOptionsBuilder tagFilter(String tagFilter) {
        this.tagFilter = tagFilter;

        return this;
    }

    public EventOptions build() {
        return new EventOptions(datacenter, nodeFilter, serviceFilter, tagFilter);
    }
}
