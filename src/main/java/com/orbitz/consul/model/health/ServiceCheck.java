package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceCheck {

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Service")
    private Service service;

    @JsonProperty("Checks")
    private List<HealthCheck> checks;

    public Node getNode(){
        return node;
    }

    public Service getService(){
        return service;
    }

    public List<HealthCheck> getChecks(){
        return checks;
    }
}
