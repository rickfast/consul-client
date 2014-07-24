package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ports {

    @JsonProperty("DNS")
    private int dns;

    @JsonProperty("HTTP")
    private int http;

    @JsonProperty("RPC")
    private int rpc;

    @JsonProperty("SerfLan")
    private int serfLan;

    @JsonProperty("SerfWan")
    private int serfWan;

    @JsonProperty("Server")
    private int server;

    public int getDns() {
        return dns;
    }

    public void setDns(int dns) {
        this.dns = dns;
    }

    public int getHttp() {
        return http;
    }

    public void setHttp(int http) {
        this.http = http;
    }

    public int getRpc() {
        return rpc;
    }

    public void setRpc(int rpc) {
        this.rpc = rpc;
    }

    public int getSerfLan() {
        return serfLan;
    }

    public void setSerfLan(int serfLan) {
        this.serfLan = serfLan;
    }

    public int getSerfWan() {
        return serfWan;
    }

    public void setSerfWan(int serfWan) {
        this.serfWan = serfWan;
    }

    public int getServer() {
        return server;
    }

    public void setServer(int server) {
        this.server = server;
    }
}
