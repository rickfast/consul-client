[![Build Status](https://travis-ci.org/hhru/consul-client.svg?branch=master)](https://travis-ci.org/hhru/consul-client)

> Originally developed by https://github.com/rickfast


Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

Basic Usage
-----------

### Example 1: Connect to Consul.

```java
Consul client = Consul.builder().build(); // connect on localhost
```

### Example 2: Register and check your service in with Consul.

```java
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
```

### Example 3: Find available (healthy) services.

```java
HealthClient healthClient = client.healthClient();

// Discover only "passing" nodes
List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("DataService").getResponse();
```

### Example 4: Store key/values.

```java
KeyValueClient kvClient = client.keyValueClient();

kvClient.putValue("foo", "bar");
String value = kvClient.getValueAsString("foo").get(); // bar
```

### Example 5: Subscribe to value change.

You can use the ConsulCache implementations to easily subscribe to Key-Value changes.

```java
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
```

### Example 6: Subscribe to healthy services

You can also use the ConsulCache implementations to easily subscribe to healthy service changes.

```java
HealthClient healthClient = client.healthClient();
String serviceName = "my-service";

ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);
svHealth.addListener((Map<ServiceHealthKey, ServiceHealth> newValues) -> {
    // do something with updated server map
});
svHealth.start();
// ...
svHealth.stop();
```

### Example 7: Find Raft peers.

```java
StatusClient statusClient = client.statusClient();
statusClient.getPeers().forEach(System.out::println);
```

### Example 8: Find Raft leader.

```java
StatusClient statusClient = client.statusClient();
System.out.println(statusClient.getLeader()); // 127.0.0.1:8300
```

Development Notes
-----------

`consul-client` makes use of [immutables](http://immutables.github.io/) to generate code for many of the value classes.
This provides a lot of functionality and benefit for little code, but it does require some additional development setup.

Official instructions are [here](http://immutables.github.io/apt.html), although you may want to change the target directories to the more gradle-like "generated/source/apt/main" and  "generated/source/apt/test" targets.
