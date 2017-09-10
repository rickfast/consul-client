package com.orbitz.consul.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbitz.consul.model.health.ServiceHealth;
import org.immutables.value.Value;

/**
 * Provides a unique key for a {@link ServiceHealth} entry in a {@link ServiceHealthCache}
 */
@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ServiceHealthKey {

    public abstract String getServiceId();

    public abstract String getHost();

    public abstract Integer getPort();

    public static ServiceHealthKey fromServiceHealth(ServiceHealth serviceHealth) {

        return ServiceHealthKey.of(
                serviceHealth.getService().getId()
                , serviceHealth.getNode().getAddress()
                , serviceHealth.getService().getPort()
        );
    }

    public static ServiceHealthKey of(String serviceId, String host, int port) {
        return ImmutableServiceHealthKey.builder()
                .serviceId(serviceId)
                .host(host)
                .port(port)
                .build();
    }
}
