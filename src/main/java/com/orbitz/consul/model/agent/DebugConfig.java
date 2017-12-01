package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDebugConfig.class)
@JsonDeserialize(as = ImmutableDebugConfig.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class DebugConfig {

    @JsonProperty("Bootstrap")
    public abstract boolean getBootstrap();

    @JsonProperty("Datacenter")
    public abstract String getDatacenter();

    @JsonProperty("DataDir")
    public abstract String getDataDir();

    @JsonProperty("DNSRecursors")
    public abstract List<String> dnsRecursors();

    @JsonProperty("DNSDomain")
    public abstract String getDnsDomain();

    @JsonProperty("LogLevel")
    public abstract String getLogLevel();

    @JsonProperty("NodeName")
    public abstract String getNodeName();

    @JsonProperty("ClientAddrs")
    public abstract List<String> getClientAddrs();

    @JsonProperty("BindAddr")
    public abstract String getBindAddr();

    @JsonProperty("LeaveOnTerm")
    public abstract boolean getLeaveOnTerm();

    @JsonProperty("SkipLeaveOnInt")
    public abstract boolean getSkipLeaveOnInt();

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

    @JsonProperty("UiDir")
    public abstract Optional<String> getUiDir();

    @JsonProperty("PidFile")
    public abstract String getPidFile();

    @JsonProperty("EnableSyslog")
    public abstract boolean getEnableSyslog();

    @JsonProperty("RejoinAfterLeave")
    public abstract boolean getRejoinAfterLeave();

    @JsonProperty("AdvertiseAddrLAN")
    public abstract String getAdvertiseAddrLAN();

    @JsonProperty("AdvertiseAddrWAN")
    public abstract String getAdvertiseAddrWAN();
}
