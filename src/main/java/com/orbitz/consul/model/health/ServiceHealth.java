package com.orbitz.consul.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceHealth {

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Service")
    private Service service;

    @JsonProperty("Checks")
    private Check[] checks;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Check[] getChecks() {
        return checks;
    }

    public void setChecks(Check[] checks) {
        this.checks = checks;
    }
}
