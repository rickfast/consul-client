package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("Bootstrap")
    private boolean bootstrap;

    @JsonProperty("Server")
    private boolean server;

    @JsonProperty("Datacenter")
    private String datacenter;

    @JsonProperty("DataDir")
    private String dataDir;

    @JsonProperty("DNSRecursor")
    private String dnsRecursor;

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("LogLevel")
    private String logLevel;

    @JsonProperty("NodeName")
    private String nodeName;

    @JsonProperty("ClientAddr")
    private String clientAddr;

    @JsonProperty("BindAddr")
    private String bindAddr;

    @JsonProperty("AdvertiseAddr")
    private String advertiseAddr;

    @JsonProperty("Ports")
    private Ports ports;

    @JsonProperty("LeaveOnTerm")
    private boolean leaveOnTerm;

    @JsonProperty("SkipLeaveOnInt")
    private boolean skipLeaveOnInt;

    @JsonProperty("StatsiteAddr")
    private String statsiteAddr;

    @JsonProperty("Protocol")
    private int protocol;

    @JsonProperty("EnableDebug")
    private boolean enableDebug;

    @JsonProperty("VerifyIncoming")
    private boolean verifyIncoming;

    @JsonProperty("VerifyOutgoing")
    private boolean verifyOutgoing;

    @JsonProperty("CAFile")
    private String caFile;

    @JsonProperty("CertFile")
    private String certFile;

    @JsonProperty("KeyFile")
    private String keyFile;

    @JsonProperty("StartJoin")
    private String[] startJoin;

    @JsonProperty("UiDir")
    private String uiDir;

    @JsonProperty("PidFile")
    private String pidFile;

    @JsonProperty("EnableSyslog")
    private boolean enableSyslog;

    @JsonProperty("RejoinAfterLeave")
    private boolean rejoinAfterLeave;

    public boolean isBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getDnsRecursor() {
        return dnsRecursor;
    }

    public void setDnsRecursor(String dnsRecursor) {
        this.dnsRecursor = dnsRecursor;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public String getAdvertiseAddr() {
        return advertiseAddr;
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    public Ports getPorts() {
        return ports;
    }

    public void setPorts(Ports ports) {
        this.ports = ports;
    }

    public boolean isLeaveOnTerm() {
        return leaveOnTerm;
    }

    public void setLeaveOnTerm(boolean leaveOnTerm) {
        this.leaveOnTerm = leaveOnTerm;
    }

    public boolean isSkipLeaveOnInt() {
        return skipLeaveOnInt;
    }

    public void setSkipLeaveOnInt(boolean skipLeaveOnInt) {
        this.skipLeaveOnInt = skipLeaveOnInt;
    }

    public String getStatsiteAddr() {
        return statsiteAddr;
    }

    public void setStatsiteAddr(String statsiteAddr) {
        this.statsiteAddr = statsiteAddr;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public boolean isVerifyIncoming() {
        return verifyIncoming;
    }

    public void setVerifyIncoming(boolean verifyIncoming) {
        this.verifyIncoming = verifyIncoming;
    }

    public boolean isVerifyOutgoing() {
        return verifyOutgoing;
    }

    public void setVerifyOutgoing(boolean verifyOutgoing) {
        this.verifyOutgoing = verifyOutgoing;
    }

    public String getCaFile() {
        return caFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String[] getStartJoin() {
        return startJoin;
    }

    public void setStartJoin(String[] startJoin) {
        this.startJoin = startJoin;
    }

    public String getUiDir() {
        return uiDir;
    }

    public void setUiDir(String uiDir) {
        this.uiDir = uiDir;
    }

    public String getPidFile() {
        return pidFile;
    }

    public void setPidFile(String pidFile) {
        this.pidFile = pidFile;
    }

    public boolean isEnableSyslog() {
        return enableSyslog;
    }

    public void setEnableSyslog(boolean enableSyslog) {
        this.enableSyslog = enableSyslog;
    }

    public boolean isRejoinAfterLeave() {
        return rejoinAfterLeave;
    }

    public void setRejoinAfterLeave(boolean rejoinAfterLeave) {
        this.rejoinAfterLeave = rejoinAfterLeave;
    }
}
