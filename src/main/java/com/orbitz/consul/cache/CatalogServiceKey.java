package com.orbitz.consul.cache;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbitz.consul.model.catalog.CatalogService;
import org.immutables.value.Value;

@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CatalogServiceKey {

    public abstract String getServiceId();

    public abstract String getHost();

    public abstract Integer getPort();

    public static CatalogServiceKey fromCatalogService(CatalogService service) {

        return CatalogServiceKey.of(
                service.getServiceId()
                , service.getAddress()
                , service.getServicePort()
        );
    }

    public static CatalogServiceKey of(String serviceId, String host, int port) {
        return ImmutableCatalogServiceKey.builder()
                .serviceId(serviceId)
                .host(host)
                .port(port)
                .build();
    }
}