package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;

import java.math.BigInteger;
import java.util.List;

public class ServiceHealthCache extends ConsulCache<HostAndPort, ServiceHealth> {

    private ServiceHealthCache(Function<ServiceHealth, HostAndPort> keyConversion, CallbackConsumer<ServiceHealth> callbackConsumer) {
        super(keyConversion, callbackConsumer);
    }

    /**
     * Factory method to construct a string/{@link ServiceHealth} map for a particular service.
     * <p/>
     * Keys will be a {@link HostAndPort} object made up of the service's address/port combo
     *
     * @param healthClient the {@link HealthClient}
     * @param serviceName  the name of the service
     * @param passing      include only passing services?
     * @return a cache object
     */
    public static ServiceHealthCache newCache(
            final HealthClient healthClient,
            final String serviceName,
            final boolean passing,
            final CatalogOptions catalogOptions,
            final int watchSeconds) {
        Function<ServiceHealth, HostAndPort > keyExtractor = new Function<ServiceHealth, HostAndPort>() {
            @Override
            public HostAndPort apply(ServiceHealth input) {
                return HostAndPort.fromParts(input.getNode().getAddress(), input.getService().getPort());
            }
        };

        CallbackConsumer<ServiceHealth> callbackConsumer = new CallbackConsumer<ServiceHealth>() {
            @Override
            public void consume(BigInteger index, ConsulResponseCallback<List<ServiceHealth>> callback) {
                if (passing) {
                    healthClient.getHealthyServiceInstances(serviceName, catalogOptions, watchParams(index, watchSeconds), callback);
                } else {
                    healthClient.getAllServiceInstances(serviceName, catalogOptions, watchParams(index, watchSeconds), callback);
                }
            }
        };

        return new ServiceHealthCache(keyExtractor, callbackConsumer);

    }

    public static ServiceHealthCache newCache(final HealthClient healthClient, final String serviceName) {
        return newCache(healthClient, serviceName, true, CatalogOptions.BLANK, 10);
    }

}
