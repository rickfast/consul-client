package com.orbitz.consul;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.*;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class CatalogITest extends BaseIntegrationTest {

    @Test
    public void shouldGetNodes() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        assertFalse(catalogClient.getNodes().getResponse().isEmpty());
    }

    @Test
    public void shouldGetNodesByDatacenter() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        assertFalse(catalogClient.getNodes(ImmutableQueryOptions.builder().datacenter("dc1").build()).getResponse().isEmpty());
    }

    @Test
    public void shouldGetNodesByDatacenterBlock() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        long start = System.currentTimeMillis();
        ConsulResponse<List<Node>> response = catalogClient.getNodes(QueryOptions.blockSeconds(2,
                new BigInteger(Integer.toString(Integer.MAX_VALUE))).datacenter("dc1").build());
        long time = System.currentTimeMillis() - start;

        assertTrue(time >= 2000);
        assertFalse(response.getResponse().isEmpty());
    }

    @Test
    public void shouldGetDatacenters() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();
        List<String> datacenters = catalogClient.getDatacenters();

        assertEquals(1, datacenters.size());
        assertEquals("dc1", datacenters.iterator().next());
    }

    @Test
    public void shouldGetServices() throws Exception {
        CatalogClient catalogClient = client.catalogClient();
        ConsulResponse<Map<String, List<String>>> services = catalogClient.getServices();

        assertTrue(services.getResponse().containsKey("consul"));
    }

    @Test
    public void shouldGetService() throws Exception {
        CatalogClient catalogClient = client.catalogClient();
        ConsulResponse<List<CatalogService>> services = catalogClient.getService("consul");

        assertEquals("consul", services.getResponse().iterator().next().getServiceName());
    }

    @Test
    public void shouldGetNode() throws Exception {
        CatalogClient catalogClient = client.catalogClient();
        ConsulResponse<CatalogNode> node = catalogClient.getNode(catalogClient.getNodes()
                .getResponse().iterator().next().getNode());

        assertNotNull(node);
    }

    @Test
    @Ignore
    public void shouldGetTaggedAddressesForNodesLists() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        final List<Node> nodesResp = catalogClient.getNodes().getResponse();
        for (Node node : nodesResp) {
            assertNotNull(node.getTaggedAddresses());
            assertNotNull(node.getTaggedAddresses().get().getWan());
            assertFalse(node.getTaggedAddresses().get().getWan().isEmpty());
        }
    }

    @Test
    @Ignore
    public void shouldGetTaggedAddressesForNode() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        final List<Node> nodesResp = catalogClient.getNodes().getResponse();
        for (Node tmp : nodesResp) {
            final Node node = catalogClient.getNode(tmp.getNode()).getResponse().getNode();
            assertNotNull(node.getTaggedAddresses());
            assertNotNull(node.getTaggedAddresses().get().getWan());
            assertFalse(node.getTaggedAddresses().get().getWan().isEmpty());
        }
    }

    @Test
    public void shouldRegisterService() {
        String service = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        createAndCheckService(
                ImmutableCatalogService.builder()
                        .address("localhost")
                        .datacenter("dc1")
                        .node("node")
                        .serviceAddress("localhost")
                        .addServiceTags("sometag")
                        .serviceId(serviceId)
                        .serviceName(service)
                        .servicePort(8080)
                        .serviceMeta(Collections.singletonMap("metakey", "metavalue"))
                        .serviceEnableTagOverride(true)
                        .serviceWeights(ImmutableServiceWeights.builder().passing(42).warning(21).build())
                        .build(),
                ImmutableCatalogRegistration.builder()
                        .address("localhost")
                        .datacenter("dc1")
                        .node("node")
                        .service(ImmutableService.builder()
                                .address("localhost")
                                .addTags("sometag")
                                .id(serviceId)
                                .service(service)
                                .port(8080)
                                .putMeta("metakey", "metavalue")
                                .enableTagOverride(true) //setting this request flag sets the ServiceEnableTagOverride in the response
                                .weights(ImmutableServiceWeights.builder().passing(42).warning(21).build())
                                .build())
                        .build()
        );
    }

    @Test
    public void shouldRegisterServiceNoWeights() {
        String service = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        createAndCheckService(
                ImmutableCatalogService.builder()
                        .address("localhost")
                        .datacenter("dc1")
                        .node("node")
                        .serviceAddress("localhost")
                        .addServiceTags("sometag")
                        .serviceId(serviceId)
                        .serviceName(service)
                        .servicePort(8080)
                        .serviceMeta(Collections.singletonMap("metakey", "metavalue"))
                        .serviceEnableTagOverride(true)
                        .serviceWeights(ImmutableServiceWeights.builder().passing(1).warning(1).build())
                        .build(),
                ImmutableCatalogRegistration.builder()
                        .address("localhost")
                        .datacenter("dc1")
                        .node("node")
                        .service(ImmutableService.builder()
                                .address("localhost")
                                .addTags("sometag")
                                .id(serviceId)
                                .service(service)
                                .port(8080)
                                .putMeta("metakey", "metavalue")
                                .enableTagOverride(true) //setting this request flag sets the ServiceEnableTagOverride in the response
                                .build())
                        .build()
        );
    }


    @Test
    public void shouldDeregisterWithDefaultDC() throws InterruptedException {
        CatalogClient catalogClient = client.catalogClient();

        String service = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();

        CatalogRegistration registration = ImmutableCatalogRegistration.builder()
                .address("localhost")
                .datacenter("dc1")
                .node("node")
                .service(ImmutableService.builder()
                        .address("localhost")
                        .addTags("sometag")
                        .id(serviceId)
                        .service(service)
                        .port(8080)
                        .putMeta("metakey", "metavalue")
                        .build())
                .build();

        catalogClient.register(registration);

        CatalogDeregistration deregistration = ImmutableCatalogDeregistration.builder()
                .node("node")
                .serviceId(serviceId)
                .build();

        catalogClient.deregister(deregistration);

        Synchroniser.pause(Duration.ofSeconds(1));
        boolean found = false;

        for (ServiceHealth health : client.healthClient().getAllServiceInstances(service).getResponse()) {
            if (health.getService().getId().equals(serviceId)) {
                found = true;
            }
        }

        assertFalse(found);
    }

    @Test
    public void shouldGetServicesInCallback() throws ExecutionException, InterruptedException, TimeoutException {
        CatalogClient catalogClient = client.catalogClient();

        String serviceName = UUID.randomUUID().toString();
        String serviceId = createAutoDeregisterServiceId();
        client.agentClient().register(20001, 20, serviceName, serviceId, Collections.emptyList(), Collections.emptyMap());

        CompletableFuture<Map<String, List<String>>> cf = new CompletableFuture<>();
        catalogClient.getServices(QueryOptions.BLANK, callbackFuture(cf));

        Map<String, List<String>> result = cf.get(1, TimeUnit.SECONDS);

        assertTrue(result.containsKey(serviceName));
    }

    @Test
    public void shouldGetServiceInCallback() throws ExecutionException, InterruptedException, TimeoutException {
        CatalogClient catalogClient = client.catalogClient();

        String serviceName = UUID.randomUUID().toString();
        String serviceId = createAutoDeregisterServiceId();
        client.agentClient().register(20001, 20, serviceName, serviceId, Collections.emptyList(), Collections.emptyMap());

        CompletableFuture<List<CatalogService>> cf = new CompletableFuture<>();
        catalogClient.getService(serviceName, QueryOptions.BLANK, callbackFuture(cf));

        List<CatalogService> result = cf.get(1, TimeUnit.SECONDS);

        assertEquals(1, result.size());
        CatalogService service = result.get(0);

        assertEquals(serviceId, service.getServiceId());
    }

    @Test
    public void shouldGetNodeInCallback() throws ExecutionException, InterruptedException, TimeoutException {
        CatalogClient catalogClient = client.catalogClient();

        String nodeName = "node";
        String serviceName = UUID.randomUUID().toString();
        String serviceId = createAutoDeregisterServiceId();

        CatalogRegistration registration = ImmutableCatalogRegistration.builder()
                .address("localhost")
                .node(nodeName)
                .service(ImmutableService.builder()
                        .address("localhost")
                        .id(serviceId)
                        .service(serviceName)
                        .port(20001)
                        .build())
                .build();

        catalogClient.register(registration);

        CompletableFuture<CatalogNode> cf = new CompletableFuture<>();
        catalogClient.getNode(nodeName, QueryOptions.BLANK, callbackFuture(cf));

        CatalogNode node = cf.get(1, TimeUnit.SECONDS);

        assertEquals(nodeName, node.getNode().getNode());

        Service service = node.getServices().get(serviceId);
        assertNotNull(service);
        assertEquals(serviceName, service.getService());
    }

    private static <T> ConsulResponseCallback<T> callbackFuture(CompletableFuture<T> cf) {
        return new ConsulResponseCallback<T>() {
            @Override
            public void onComplete(ConsulResponse<T> consulResponse) {
                cf.complete(consulResponse.getResponse());
            }

            @Override
            public void onFailure(Throwable throwable) {
                cf.completeExceptionally(throwable);
            }
        };
    }

    private void createAndCheckService(CatalogService expectedService, CatalogRegistration registration) {
        CatalogClient catalogClient = client.catalogClient();
        catalogClient.register(registration);
        Synchroniser.pause(Duration.ofMillis(100));

        String serviceName = registration.service().get().getService();

        ConsulResponse<List<CatalogService>> response = catalogClient.getService(serviceName);

        assertFalse(response.getResponse().isEmpty());

        CatalogService registeredService = null;
        for (CatalogService catalogService : response.getResponse()) {
            if (catalogService.getServiceName().equals(serviceName)) {
                registeredService = catalogService;
            }
        }
        assertNotNull(String.format("Service \"%s\" not found", serviceName), registeredService);
        assertEquals(expectedService, registeredService);
    }
}
