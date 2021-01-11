package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.util.Jackson;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DebugConfigTest {
    private final String JSON = "{\n" +
            "    \"ACLMasterToken\": \"hidden\",\n" +
            "    \"GRPCAddrs\": [\n" +
            "        \"tcp://0.0.0.0:8502\"\n" +
            "    ],\n" +
            "    \"ACLEnableKeyListPolicy\": false,\n" +
            "    \"HTTPSPort\": -1,\n" +
            "    \"AutoEncryptTLS\": false,\n" +
            "    \"AdvertiseAddrLAN\": \"127.0.0.1\",\n" +
            "    \"HTTPAddrs\": [\n" +
            "        \"tcp://0.0.0.0:8500\"\n" +
            "    ],\n" +
            "    \"LeaveOnTerm\": false,\n" +
            "    \"ClientAddrs\": [\n" +
            "        \"0.0.0.0\"\n" +
            "    ],\n" +
            "    \"RetryJoinIntervalLAN\": \"30s\",\n" +
            "    \"ACLTokenTTL\": \"30s\",\n" +
            "    \"ACLDefaultPolicy\": \"allow\",\n" +
            "    \"CheckReapInterval\": \"30s\",\n" +
            "    \"ConnectEnabled\": true,\n" +
            "    \"ConsulCoordinateUpdatePeriod\": \"100ms\",\n" +
            "    \"ConsulRaftHeartbeatTimeout\": \"35ms\",\n" +
            "    \"AutoEncryptAllowTLS\": false,\n" +
            "    \"RaftSnapshotThreshold\": 0,\n" +
            "    \"ACLTokenReplication\": false,\n" +
            "    \"GRPCPort\": 8502,\n" +
            "    \"AutopilotDisableUpgradeMigration\": false,\n" +
            "    \"BindAddr\": \"127.0.0.1\",\n" +
            "    \"VerifyIncomingRPC\": false,\n" +
            "    \"DNSAddrs\": [\n" +
            "        \"tcp://0.0.0.0:8600\",\n" +
            "        \"udp://0.0.0.0:8600\"\n" +
            "    ],\n" +
            "    \"ACLReplicationToken\": \"hidden\",\n" +
            "    \"AutopilotMinQuorum\": 0,\n" +
            "    \"SerfAdvertiseAddrLAN\": \"tcp://127.0.0.1:8301\",\n" +
            "    \"ConsulRaftLeaderLeaseTimeout\": \"20ms\",\n" +
            "    \"UIContentPath\": \"/ui/\",\n" +
            "    \"EnableCentralServiceConfig\": false,\n" +
            "    \"KVMaxValueSize\": 524288,\n" +
            "    \"ServerMode\": true,\n" +
            "    \"TxnMaxReqLen\": 524288,\n" +
            "    \"DisableRemoteExec\": true,\n" +
            "    \"HTTPMaxConnsPerClient\": 200,\n" +
            "    \"RPCHandshakeTimeout\": \"5s\",\n" +
            "    \"DNSMaxStale\": \"87600h0m0s\",\n" +
            "    \"Telemetry\": {\n" +
            "        \"CirconusAPIToken\": \"hidden\",\n" +
            "        \"Disable\": false,\n" +
            "        \"DisableHostname\": false,\n" +
            "        \"FilterDefault\": true,\n" +
            "        \"MetricsPrefix\": \"consul\",\n" +
            "        \"PrometheusRetentionTime\": \"0s\"\n" +
            "    },\n" +
            "    \"SegmentNameLimit\": 64,\n" +
            "    \"GossipLANGossipNodes\": 3,\n" +
            "    \"RaftTrailingLogs\": 0,\n" +
            "    \"RPCProtocol\": 2,\n" +
            "    \"DNSCacheMaxAge\": \"0s\",\n" +
            "    \"GossipLANRetransmitMult\": 4,\n" +
            "    \"AutopilotMaxTrailingLogs\": 250,\n" +
            "    \"ACLDatacenter\": \"dc1\",\n" +
            "    \"DefaultQueryTime\": \"5m0s\",\n" +
            "    \"Version\": \"1.8.4\",\n" +
            "    \"RPCRateLimit\": -1,\n" +
            "    \"Logging\": {\n" +
            "        \"EnableSyslog\": false,\n" +
            "        \"LogJSON\": false,\n" +
            "        \"LogLevel\": \"DEBUG\",\n" +
            "        \"LogRotateBytes\": 0,\n" +
            "        \"LogRotateDuration\": \"0s\",\n" +
            "        \"LogRotateMaxFiles\": 0,\n" +
            "        \"SyslogFacility\": \"LOCAL0\"\n" +
            "    },\n" +
            "    \"RPCMaxConnsPerClient\": 100,\n" +
            "    \"RejoinAfterLeave\": false,\n" +
            "    \"GossipWANSuspicionMult\": 3,\n" +
            "    \"DNSNodeMetaTXT\": true,\n" +
            "    \"ACLsEnabled\": false,\n" +
            "    \"SerfPortWAN\": 8302,\n" +
            "    \"GossipLANProbeInterval\": \"100ms\",\n" +
            "    \"PrimaryDatacenter\": \"dc1\",\n" +
            "    \"Cache\": {\n" +
            "        \"EntryFetchMaxBurst\": 2,\n" +
            "        \"EntryFetchRate\": 1.7976931348623157E308\n" +
            "    },\n" +
            "    \"DiscardCheckOutput\": false,\n" +
            "    \"ReconnectTimeoutWAN\": \"0s\",\n" +
            "    \"AdvertiseAddrWAN\": \"127.0.0.1\",\n" +
            "    \"ConnectSidecarMaxPort\": 21255,\n" +
            "    \"EnableUI\": true,\n" +
            "    \"EnableDebug\": true,\n" +
            "    \"KeyFile\": \"hidden\",\n" +
            "    \"RPCAdvertiseAddr\": \"tcp://127.0.0.1:8300\",\n" +
            "    \"SyncCoordinateIntervalMin\": \"15s\",\n" +
            "    \"EncryptVerifyOutgoing\": true,\n" +
            "    \"DNSUseCache\": false,\n" +
            "    \"Bootstrap\": false,\n" +
            "    \"TranslateWANAddrs\": false,\n" +
            "    \"ACLEnableTokenPersistence\": false,\n" +
            "    \"DNSNodeTTL\": \"0s\",\n" +
            "    \"ExposeMinPort\": 21500,\n" +
            "    \"DNSDomain\": \"consul.\",\n" +
            "    \"SegmentLimit\": 64,\n" +
            "    \"AutopilotLastContactThreshold\": \"200ms\",\n" +
            "    \"ConsulCoordinateUpdateMaxBatches\": 5,\n" +
            "    \"EnableRemoteScriptChecks\": true,\n" +
            "    \"DNSAllowStale\": true,\n" +
            "    \"AutopilotCleanupDeadServers\": true,\n" +
            "    \"GossipWANGossipNodes\": 3,\n" +
            "    \"NodeName\": \"484c56552d86\",\n" +
            "    \"HTTPUseCache\": true,\n" +
            "    \"DNSDisableCompression\": false,\n" +
            "    \"SyncCoordinateRateTarget\": 64,\n" +
            "    \"SerfAdvertiseAddrWAN\": \"tcp://127.0.0.1:8302\",\n" +
            "    \"VerifyServerHostname\": false,\n" +
            "    \"GossipWANProbeInterval\": \"100ms\",\n" +
            "    \"ExposeMaxPort\": 21755,\n" +
            "    \"VerifyOutgoing\": false,\n" +
            "    \"DevMode\": true,\n" +
            "    \"GossipWANProbeTimeout\": \"100ms\",\n" +
            "    \"RetryJoinMaxAttemptsWAN\": 0,\n" +
            "    \"DNSARecordLimit\": 0,\n" +
            "    \"EncryptVerifyIncoming\": true,\n" +
            "    \"DisableHTTPUnprintableCharFilter\": false,\n" +
            "    \"GossipLANSuspicionMult\": 3,\n" +
            "    \"DisableAnonymousSignature\": true,\n" +
            "    \"SerfBindAddrWAN\": \"tcp://127.0.0.1:8302\",\n" +
            "    \"ACLDownPolicy\": \"extend-cache\",\n" +
            "    \"ConsulServerHealthInterval\": \"10ms\",\n" +
            "    \"LeaveDrainTime\": \"5s\",\n" +
            "    \"PrimaryGatewaysInterval\": \"30s\",\n" +
            "    \"GossipWANRetransmitMult\": 4,\n" +
            "    \"DisableCoordinates\": false,\n" +
            "    \"EnableAgentTLSForChecks\": false,\n" +
            "    \"EnableLocalScriptChecks\": true,\n" +
            "    \"DisableHostNodeID\": true,\n" +
            "    \"RPCBindAddr\": \"tcp://127.0.0.1:8300\",\n" +
            "    \"ACLToken\": \"hidden\",\n" +
            "    \"DataDir\": \"/consul/data\",\n" +
            "    \"BootstrapExpect\": 0,\n" +
            "    \"ConsulCoordinateUpdateBatchSize\": 128,\n" +
            "    \"HTTPSHandshakeTimeout\": \"5s\",\n" +
            "    \"EncryptKey\": \"hidden\",\n" +
            "    \"ACLDisabledTTL\": \"2m0s\",\n" +
            "    \"SerfPortLAN\": 8301,\n" +
            "    \"ACLRoleTTL\": \"0s\",\n" +
            "    \"ServerPort\": 8300,\n" +
            "    \"ConnectTestCALeafRootChangeSpread\": \"0s\",\n" +
            "    \"VerifyIncomingHTTPS\": false,\n" +
            "    \"DiscoveryMaxStale\": \"0s\",\n" +
            "    \"GossipLANGossipInterval\": \"100ms\",\n" +
            "    \"GossipLANProbeTimeout\": \"100ms\",\n" +
            "    \"TLSPreferServerCipherSuites\": false,\n" +
            "    \"MaxQueryTime\": \"10m0s\",\n" +
            "    \"ReconnectTimeoutLAN\": \"0s\",\n" +
            "    \"CheckUpdateInterval\": \"5m0s\",\n" +
            "    \"TLSMinVersion\": \"tls12\",\n" +
            "    \"AutoConfig\": {\n" +
            "        \"Authorizer\": {\n" +
            "            \"AllowReuse\": false,\n" +
            "            \"AuthMethod\": {\n" +
            "                \"Config\": {\n" +
            "                    \"ClockSkewLeeway\": 0,\n" +
            "                    \"ExpirationLeeway\": 0,\n" +
            "                    \"NotBeforeLeeway\": 0\n" +
            "                },\n" +
            "                \"MaxTokenTTL\": \"0s\",\n" +
            "                \"Name\": \"Auto Config Authorizer\",\n" +
            "                \"RaftIndex\": {\n" +
            "                    \"CreateIndex\": 0,\n" +
            "                    \"ModifyIndex\": 0\n" +
            "                },\n" +
            "                \"Type\": \"jwt\"\n" +
            "            },\n" +
            "            \"Enabled\": false\n" +
            "        },\n" +
            "        \"Enabled\": false,\n" +
            "        \"IntroToken\": \"hidden\"\n" +
            "    },\n" +
            "    \"ACLAgentToken\": \"hidden\",\n" +
            "    \"DNSEnableTruncate\": false,\n" +
            "    \"DisableKeyringFile\": true,\n" +
            "    \"SkipLeaveOnInt\": true,\n" +
            "    \"RaftProtocol\": 0,\n" +
            "    \"CheckDeregisterIntervalMin\": \"1m0s\",\n" +
            "    \"DNSRecursorTimeout\": \"2s\",\n" +
            "    \"RPCMaxBurst\": 1000,\n" +
            "    \"Revision\": \"12b16df32\",\n" +
            "    \"CheckOutputMaxSize\": 4096,\n" +
            "    \"TaggedAddresses\": {\n" +
            "        \"lan\": \"127.0.0.1\",\n" +
            "        \"lan_ipv4\": \"127.0.0.1\",\n" +
            "        \"wan\": \"127.0.0.1\",\n" +
            "        \"wan_ipv4\": \"127.0.0.1\"\n" +
            "    },\n" +
            "    \"AutopilotServerStabilizationTime\": \"10s\",\n" +
            "    \"DNSOnlyPassing\": false,\n" +
            "    \"VerifyIncoming\": false,\n" +
            "    \"DNSSOA\": {\n" +
            "        \"Expire\": 86400,\n" +
            "        \"Minttl\": 0,\n" +
            "        \"Refresh\": 3600,\n" +
            "        \"Retry\": 600\n" +
            "    },\n" +
            "    \"GossipWANGossipInterval\": \"100ms\",\n" +
            "    \"ACLPolicyTTL\": \"30s\",\n" +
            "    \"DisableUpdateCheck\": false,\n" +
            "    \"DNSPort\": 8600,\n" +
            "    \"NodeID\": \"46c53c05-5d11-8e08-c49b-a1e0c4a485b2\",\n" +
            "    \"RetryJoinIntervalWAN\": \"30s\",\n" +
            "    \"SessionTTLMin\": \"0s\",\n" +
            "    \"NonVotingServer\": false,\n" +
            "    \"ConnectMeshGatewayWANFederationEnabled\": false,\n" +
            "    \"SerfBindAddrLAN\": \"tcp://127.0.0.1:8301\",\n" +
            "    \"RetryJoinMaxAttemptsLAN\": 0,\n" +
            "    \"ACLAgentMasterToken\": \"hidden\",\n" +
            "    \"HTTPPort\": 8500,\n" +
            "    \"ConsulRaftElectionTimeout\": \"52ms\",\n" +
            "    \"RaftSnapshotInterval\": \"0s\",\n" +
            "    \"ConnectSidecarMinPort\": 21000,\n" +
            "    \"DNSUDPAnswerLimit\": 3,\n" +
            "    \"AEInterval\": \"1m0s\",\n" +
            "    \"Datacenter\": \"dc1\",\n" +
            "    \"RPCHoldTimeout\": \"7s\"\n" +
            "}";
    @Test
    public void testDeserialization() throws IOException {
       ObjectMapper mapper = Jackson.MAPPER;
       final DebugConfig dbgConfig = mapper.readerFor(DebugConfig.class).readValue(JSON);
       assertEquals("DebugConfig contains 167 items", 167, dbgConfig.size());
    }
}
