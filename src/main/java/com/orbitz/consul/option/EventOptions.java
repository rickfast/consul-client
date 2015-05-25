package com.orbitz.consul.option;

/**
 * Created by rfast on 5/25/15.
 */
public class EventOptions {

    private String datacenter;
    private String nodeFilter;
    private String serviceFilter;
    private String tagFilter;

    public static EventOptions BLANK = new EventOptions();

    private EventOptions() {}

    EventOptions(String datacenter, String nodeFilter, String serviceFilter, String tagFilter) {
        this.datacenter = datacenter;
        this.nodeFilter = nodeFilter;
        this.serviceFilter = serviceFilter;
        this.tagFilter = tagFilter;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public String getNodeFilter() {
        return nodeFilter;
    }

    public String getServiceFilter() {
        return serviceFilter;
    }

    public String getTagFilter() {
        return tagFilter;
    }
}
