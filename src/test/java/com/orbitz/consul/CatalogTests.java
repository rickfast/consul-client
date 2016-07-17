package com.orbitz.consul;

import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogNode;
import com.orbitz.consul.model.catalog.CatalogService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.option.ImmutableCatalogOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.Test;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CatalogTests extends BaseIntegrationTest {

    @Test
    public void shouldGetNodes() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        assertFalse(catalogClient.getNodes().getResponse().isEmpty());
    }

    @Test
    public void shouldGetNodesByDatacenter() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        assertFalse(catalogClient.getNodes(ImmutableCatalogOptions.builder().datacenter("dc1").build()).getResponse().isEmpty());
    }

    @Test
    public void shouldGetNodesByDatacenterBlock() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        long start = System.currentTimeMillis();
        ConsulResponse<List<Node>> response = catalogClient.getNodes(ImmutableCatalogOptions.builder().datacenter("dc1").build(),
                QueryOptions.blockSeconds(2, new BigInteger(Integer.toString(Integer.MAX_VALUE))).build());
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
    public void shouldGetTaggedAddressesForNodesLists() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        final List<Node> nodesResp = catalogClient.getNodes().getResponse();
        for (Node node : nodesResp) {
            assertNotNull(node.getTaggedAddresses());
            assertNotNull(node.getTaggedAddresses().getWan());
            assertFalse(node.getTaggedAddresses().getWan().isEmpty());
        }
    }

    @Test
    public void shouldGetTaggedAddressesForNode() throws UnknownHostException {
        CatalogClient catalogClient = client.catalogClient();

        final List<Node> nodesResp = catalogClient.getNodes().getResponse();
        for (Node tmp : nodesResp) {
            final Node node = catalogClient.getNode(tmp.getNode()).getResponse().getNode();
            assertNotNull(node.getTaggedAddresses());
            assertNotNull(node.getTaggedAddresses().getWan());
            assertFalse(node.getTaggedAddresses().getWan().isEmpty());
        }
    }
}
