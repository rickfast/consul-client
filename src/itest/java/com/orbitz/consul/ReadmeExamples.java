package com.orbitz.consul;

import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.kv.Value;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Examples for "README.md" file.
 * Update the "README.md" file after any change.
 */
public class ReadmeExamples extends BaseIntegrationTest {

    @Test
    @Ignore
    public void example1() {
        Consul client = Consul.builder().build(); // connect to Consul on localhost
    }

    @Test
    @Ignore
    public void example2() throws NotRegisteredException {
        AgentClient agentClient = client.agentClient();

        String serviceId = "1";
        Registration service = ImmutableRegistration.builder()
                .id(serviceId)
                .name("myService")
                .port(8080)
                .check(Registration.RegCheck.ttl(3L)) // registers with a TTL of 3 seconds
                .tags(Collections.singletonList("tag1"))
                .meta(Collections.singletonMap("version", "1.0"))
                .build();

        agentClient.register(service);

        // Check in with Consul (serviceId required only).
        // Client will prepend "service:" for service level checks.
        // Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".
        agentClient.pass(serviceId);
    }

    @Test
    @Ignore
    public void example3() {
        HealthClient healthClient = client.healthClient();

        // Discover only "passing" nodes
        List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("DataService").getResponse();
    }

    @Test
    @Ignore
    public void example4() {
        KeyValueClient kvClient = client.keyValueClient();

        kvClient.putValue("foo", "bar");
        String value = kvClient.getValueAsString("foo").get(); // bar
    }

    @Test
    @Ignore
    public void example5() {
        final KeyValueClient kvClient = client.keyValueClient();

        kvClient.putValue("foo", "bar");

        KVCache cache = KVCache.newCache(kvClient, "foo");
        cache.addListener(newValues -> {
            // Cache notifies all paths with "foo" the root path
            // If you want to watch only "foo" value, you must filter other paths
            Optional<Value> newValue = newValues.values().stream()
                    .filter(value -> value.getKey().equals("foo"))
                    .findAny();

            newValue.ifPresent(value -> {
                // Values are encoded in key/value store, decode it if needed
                Optional<String> decodedValue = newValue.get().getValueAsString();
                decodedValue.ifPresent(v -> System.out.println(String.format("Value is: %s", v))); //prints "bar"
            });
        });
        cache.start();
        // ...
        cache.stop();
    }

    @Test
    @Ignore
    public void example6() {
        HealthClient healthClient = client.healthClient();
        String serviceName = "my-service";

        ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);
        svHealth.addListener((Map<ServiceHealthKey, ServiceHealth> newValues) -> {
            // do something with updated server map
        });
        svHealth.start();
        // ...
        svHealth.stop();
    }

    @Test
    @Ignore
    public void example7() {
        StatusClient statusClient = client.statusClient();
        statusClient.getPeers().forEach(System.out::println);
    }

    @Test
    @Ignore
    public void example8() {
        StatusClient statusClient = client.statusClient();
        System.out.println(statusClient.getLeader()); // 127.0.0.1:8300
    }
}
