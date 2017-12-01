package com.orbitz.consul;

import java.util.Optional;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.model.query.ImmutablePreparedQuery;
import com.orbitz.consul.model.query.ImmutableServiceQuery;
import com.orbitz.consul.model.query.PreparedQuery;
import com.orbitz.consul.model.query.StoredQuery;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class PreparedQueryTests {

    @Test
    @Ignore
    public void shouldWork() {
        String service = UUID.randomUUID().toString();
        String query = UUID.randomUUID().toString();
        Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromParts("192.168.99.100", 8500)).build();
        consul.agentClient().register(8080, 10000L, service, service + "1");
        PreparedQueryClient preparedQueryClient = consul.preparedQueryClient();

        PreparedQuery preparedQuery = ImmutablePreparedQuery.builder()
                .name(query)
                .token("")
                .service(ImmutableServiceQuery.builder()
                        .service(service)
                        .onlyPassing(true)
                        .build())
                .build();

        String id = preparedQueryClient.createPreparedQuery(preparedQuery);

        Optional<StoredQuery> storedQuery = preparedQueryClient.getPreparedQuery(id);
        //ConsulResponse<List<ServiceHealth>> result = preparedQueryClient.executeQuery(id);

        //assertEquals(id, storedQuery.get().getId());
    }
}
