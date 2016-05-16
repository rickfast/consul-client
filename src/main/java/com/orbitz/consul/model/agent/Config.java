package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableConfig.class)
@JsonDeserialize(as = ImmutableConfig.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Config {

    @JsonProperty("Bootstrap")
    public abstract boolean getBootstrap();

    @JsonProperty("Server")
    public abstract boolean getServer();

    @JsonProperty("Datacenter")
    public abstract String getDatacenter();

    @JsonProperty("DataDir")
    public abstract String getDataDir();

    @JsonProperty("DNSRecursor")
    public abstract String dnsRecursor();

    @JsonProperty("Domain")
    public abstract String getDomain();

    @JsonProperty("LogLevel")
    public abstract String getLogLevel();

    @JsonProperty("NodeName")
    public abstract String getNodeName();

    @JsonProperty("ClientAddr")
    public abstract String getClientAddr();

    @JsonProperty("BindAddr")
    public abstract String getBindAddr();

    @JsonProperty("AdvertiseAddr")
    public abstract String getAdvertiseAddr();

    @JsonProperty("Ports")
    public abstract Ports getPorts();

    @JsonProperty("LeaveOnTerm")
    public abstract boolean getLeaveOnTerm();

    @JsonProperty("SkipLeaveOnInt")
    public abstract boolean getSkipLeaveOnInt();

    /**
     *
     * @deprecated GET /v1/agent/self from v0.6.4 does not have this JSON field
     */
    @Deprecated
    @JsonProperty("StatsiteAddr")
    public abstract Optional<String> getStatsiteAddr();

    @JsonProperty("Protocol")
    public abstract int getProtocol();

    @JsonProperty("EnableDebug")
    public abstract boolean getEnableDebug();

    @JsonProperty("VerifyIncoming")
    public abstract boolean getVerifyIncoming();

    @JsonProperty("VerifyOutgoing")
    public abstract boolean getVerifyOutgoing();

    @JsonProperty("CAFile")
    public abstract String getCaFile();

    @JsonProperty("CertFile")
    public abstract String getCertFile();

    @JsonProperty("KeyFile")
    public abstract String getKeyFile();

    @JsonProperty("StartJoin")
    @JsonDeserialize(as = ImmutableList.class, contentAs = String.class)
    public abstract List<String> getStartJoin();

    @JsonProperty("UiDir")
    public abstract String getUiDir();

    @JsonProperty("PidFile")
    public abstract String getPidFile();

    @JsonProperty("EnableSyslog")
    public abstract boolean getEnableSyslog();

    @JsonProperty("RejoinAfterLeave")
    public abstract boolean getRejoinAfterLeave();

    /**
     * New version of consul has Telemetry field
     * TODO: Have to think about back compatibility (I think we shouldn't)
     */
    @JsonProperty("Telemetry")
    public abstract Optional<Telemetry> getTelemetry();
}
