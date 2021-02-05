package ru.hh.consul;

import java.util.Collections;
import java.util.Optional;
import com.google.common.net.HostAndPort;
import ru.hh.consul.model.query.ImmutablePreparedQuery;
import ru.hh.consul.model.query.ImmutableServiceQuery;
import ru.hh.consul.model.query.PreparedQuery;
import ru.hh.consul.model.query.StoredQuery;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class PreparedQueryITest extends BaseIntegrationTest {

    @Test
    @Ignore
    public void shouldWork() {
        String service = UUID.randomUUID().toString();
        String query = UUID.randomUUID().toString();
        Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromParts("192.168.99.100", consulContainer.getFirstMappedPort())).build();
        consul.agentClient().register(8080, 10000L, service, service + "1", Collections.emptyList(), Collections.emptyMap());
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
