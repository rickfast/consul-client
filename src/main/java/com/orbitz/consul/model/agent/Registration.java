package com.orbitz.consul.model.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;


@Value.Immutable
@JsonSerialize(as = ImmutableRegistration.class)
@JsonDeserialize(as = ImmutableRegistration.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Registration {

    @JsonProperty("Name")
    public abstract String getName();

    @JsonProperty("Id")
    public abstract String getId();

    @JsonProperty("Address")
    public abstract Optional<String> getAddress();

    @JsonProperty("Port")
    public abstract Optional<Integer> getPort();

    @JsonProperty("Check")
    public abstract Optional<RegCheck> getCheck();

    @JsonProperty("Checks")
    public abstract List<RegCheck> getChecks();

    @JsonProperty("Tags")
    public abstract List<String> getTags();

    @JsonProperty("EnableTagOverride")
    public abstract Optional<Boolean> getEnableTagOverride();

    @Value.Immutable
    @JsonSerialize(as = ImmutableRegCheck.class)
    @JsonDeserialize(as = ImmutableRegCheck.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public abstract static class RegCheck {

        @JsonProperty("Script")
        public abstract Optional<String> getScript();

        @JsonProperty("Interval")
        public abstract Optional<String> getInterval();

        @JsonProperty("TTL")
        public abstract Optional<String> getTtl();

        @JsonProperty("HTTP")
        public abstract Optional<String> getHttp();

        @JsonProperty("TCP")
        public abstract Optional<String> getTcp();

        @JsonProperty("GRPC")
        public abstract Optional<String> getGrpc();

        @JsonProperty("GRPCUseTLS")
        public abstract Optional<Boolean> getGrpcUseTls();

        @JsonProperty("Timeout")
        public abstract Optional<String> getTimeout();
        
        @JsonProperty("Notes")
        public abstract Optional<String> getNotes();

        @JsonProperty("DeregisterCriticalServiceAfter")
        public abstract Optional<String> getDeregisterCriticalServiceAfter();

        @JsonProperty("TLSSkipVerify")
        public abstract Optional<Boolean> getTlsSkipVerify();

        @JsonProperty("Status")
        public abstract Optional<String> getStatus();

        public static RegCheck ttl(long ttl) {
            return ImmutableRegCheck
                    .builder()
                    .ttl(String.format("%ss", ttl))
                    .build();
        }

        public static RegCheck script(String script, long interval) {
            return ImmutableRegCheck
                    .builder()
                    .script(script)
                    .interval(String.format("%ss", interval))
                    .build();
        }

        public static RegCheck script(String script, long interval, long timeout) {
            return ImmutableRegCheck
                    .builder()
                    .script(script)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .build();
        }
        
        public static RegCheck script(String script, long interval, long timeout, String notes) {
            return ImmutableRegCheck
                    .builder()
                    .script(script)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .notes(notes)
                    .build();
        }

        public static RegCheck http(String http, long interval) {
            return ImmutableRegCheck
                    .builder()
                    .http(http)
                    .interval(String.format("%ss", interval))
                    .build();
        }

        public static RegCheck http(String http, long interval, long timeout) {
            return ImmutableRegCheck
                    .builder()
                    .http(http)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .build();
        }
        
        public static RegCheck http(String http, long interval, long timeout, String notes) {
            return ImmutableRegCheck
                    .builder()
                    .http(http)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .notes(notes)
                    .build();
        }

        public static RegCheck tcp(String tcp, long interval) {
            return ImmutableRegCheck
                    .builder()
                    .tcp(tcp)
                    .interval(String.format("%ss", interval))
                    .build();
        }

        public static RegCheck tcp(String tcp, long interval, long timeout) {
            return ImmutableRegCheck
                    .builder()
                    .tcp(tcp)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .build();
        }
        
        public static RegCheck tcp(String tcp, long interval, long timeout, String notes) {
            return ImmutableRegCheck
                    .builder()
                    .tcp(tcp)
                    .interval(String.format("%ss", interval))
                    .timeout(String.format("%ss", timeout))
                    .notes(notes)
                    .build();
        }

        public static RegCheck grpc(String grpc, long interval) {
            return RegCheck.grpc(grpc, interval, false);
        }

        public static RegCheck grpc(String grpc, long interval, boolean useTls) {
            return ImmutableRegCheck
                    .builder()
                    .grpc(grpc)
                    .grpcUseTls(useTls)
                    .interval(String.format("%ss", interval))
                    .build();
        }

        @Value.Check
        protected void validate() {

            checkState(getHttp().isPresent() || getTtl().isPresent()
                || getScript().isPresent() || getTcp().isPresent() || getGrpc().isPresent(),
                    "Check must specify either http, tcp, ttl, grpc or script");

            if (getHttp().isPresent() || getScript().isPresent() || getTcp().isPresent() || getGrpc().isPresent()) {
                checkState(getInterval().isPresent(),
                        "Interval must be set if check type is http, tcp, grpc or script");
            }
        }

    }

}
