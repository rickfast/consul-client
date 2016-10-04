package com.orbitz.consul.cache;

import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.orbitz.consul.BaseIntegrationTest;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.model.health.Node;
import com.pszymczyk.consul.ConsulPorts;
import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.LogLevel;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.pszymczyk.consul.ConsulStarterBuilder.consulStarter;
import static org.junit.Assert.*;

public class NodesCatalogCacheTest extends BaseIntegrationTest {

    @Test
    public void nodeCacheHealthCheckTest() throws Exception {

        CatalogClient catalogClient = client.catalogClient();

        NodesCatalogCache nodesCatalogCache = NodesCatalogCache.newCache(catalogClient);

        final CountDownLatch secondNodeRegistrationLatch = new CountDownLatch(1);
        final CountDownLatch secondNodeDeregistrationLatch = new CountDownLatch(1);

        nodesCatalogCache.addListener(new ConsulCache.Listener<String, Node>() {
            Map<String, Node> internalNodes;
            @Override
            public void notify(Map<String, Node> newValues) {

                try {
                    // this function is called a first time at initialisation phase,
                    // the initial nodes being retained locally for later diffing.

                    if (internalNodes != null) {

                        System.out.println(newValues);

                        MapDifference<String, Node> difference = Maps.difference(
                                internalNodes, newValues, new NodeEquivalence());

                        Map<String, Node> registeredNodes = difference.entriesOnlyOnRight();
                        Map<String, Node> deregisteredNodes = difference.entriesOnlyOnLeft();

//                        assertEquals(1, registeredNodes.size() + deregisteredNodes.size());

                        if (!registeredNodes.isEmpty()) {
                            assertTrue(registeredNodes.containsKey("test-second-node"));
                            secondNodeRegistrationLatch.countDown();
                        }

                        if (!deregisteredNodes.isEmpty()) {
                            assertTrue(deregisteredNodes.containsKey("test-second-node"));
                            secondNodeDeregistrationLatch.countDown();
                        }

                    }
                } finally {
                    internalNodes = newValues;
                }

            }
        });

        nodesCatalogCache.start();
        nodesCatalogCache.awaitInitialized(3, TimeUnit.SECONDS);

        Map<String, Node> nodes = nodesCatalogCache.getMap();
        assertEquals(1, nodes.size());

        // Start a second consul process, as agent.
        // Ports are monotonically following the first consul process.
        int lastPort = consul.getServerPort();

        ConsulPorts consulPorts = ConsulPorts.consulPorts()
                .withDnsPort(-1)
                .withRpcPort(++lastPort)
                .withSerfLanPort(++lastPort)
                .withSerfWanPort(++lastPort)
                .withServerPort(++lastPort).build();

        // We have to force another node name to avoid collision with the consul server.
        // Also, we start the second one as an agent to keep an odd number of servers :)
        String customConfig = "{\n" +
            "  \"start_join\": [\"localhost:" + consul.getSerfLanPort() + "\"], \n" +
            "  \"node_name\": \"test-second-node\", \n" +
            "  \"server\": false, \n" +
            "  \"leave_on_terminate\": true \n" + // so that the node leaving the cluster will be notified immediately
            "}";

        try (ConsulProcess secondConsulAgent = consulStarter().withConsulPorts(consulPorts).withCustomConfig(customConfig).withLogLevel(LogLevel.DEBUG).build().start()) {

            boolean registerSuccess = secondNodeRegistrationLatch.await(5, TimeUnit.SECONDS);
            assertTrue(registerSuccess);
            assertEquals(2, nodesCatalogCache.getMap().size());
        }

        boolean deregisterSuccess = secondNodeDeregistrationLatch.await(5, TimeUnit.SECONDS);
        assertTrue(deregisterSuccess);

        assertEquals(1, nodesCatalogCache.getMap().size());

    }

    // Note: cache is notified on any state change, i.e.
    // is usuallly notified twice on node addition, once when the
    // node is joining the local cluster, and a second time
    // when it get it's WAN connection initialized.
    // This Equivalence class avoid to diff the second notification.
    public static class NodeEquivalence extends Equivalence<Node> {

        @Override
        protected boolean doEquivalent(Node a, Node b) {
            return a.getNode().equals(b.getNode()) && a.getAddress().equals(b.getAddress());
        }

        @Override
        protected int doHash(Node node) {
            return HashCode.fromString(node.getNode() + node.getAddress()).hashCode();
        }
    }
}