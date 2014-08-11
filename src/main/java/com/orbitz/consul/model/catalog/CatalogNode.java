package com.orbitz.consul.model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogNode {

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Services")
    private Map<String, Service> services;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }
}
