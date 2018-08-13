[![Build Status](https://travis-ci.org/rickfast/consul-client.svg?branch=master)](https://travis-ci.org/rickfast/consul-client)
[ ![Download](https://api.bintray.com/packages/orbitz/consul-client/consul-client/images/download.svg) ](https://bintray.com/orbitz/consul-client/consul-client/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

Installation
-----------

### Note

As of `1.0.0`, this library requires Java 8.

### Note

In 0.13.x, both shaded and non-shaded JARs are provided. The shaded JAR has a `shaded` classifier, while the non-shaded JAR has no classifier. Note that this is a change from 0.12 and 0.11.

In 0.11.X and 0.12.x, the Consul JAR is a shaded JAR, with most dependencies included. This was done because a number of issues being files were related to dependency conflicts. The JAR is a bit bigger, but the HTTP + JSON libraries are now internal to the JAR. Only Guava is still a transitive dependency.

### Bintray:

Grab the latest binary (1.2.3) [here](http://dl.bintray.com/orbitz/consul-client/com/orbitz/consul/consul-client/1.1.2/#consul-client-1.0.1.jar).

### Gradle:

```groovy
repositories {
    jcenter() // or mavenCentral()
}

dependencies {
    compile 'com.orbitz.consul:consul-client:1.2.3'
}
```

### Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.orbitz.consul</groupId>
        <artifactId>consul-client</artifactId>
        <version>1.2.3</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```


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

### Integration Tests

Integrations Tests rely on the assumption that a Consul server is running on localhost's default port 8500.

You can run a Consul server in docker using the following command line:
```
docker kill dev-consul ; docker rm dev-consul ; docker run -d -p 127.0.0.1:8500:8500 --name=dev-consul consul
```

### Eclipse-specific notes

Their instructions for eclipse a bit difficult to grok, but I was able to get eclipse to compile by following the second part of the instructions. Essentially, enable annotation processing, then extend the M2_REPO variable to include the immutables annotation processor. One thing is that documentation is out of date in that it tells you the wrong jar to include - it should be org/immutables/value/2.0.16/value-2.0.16.jar.

![extending M2_REPO](http://cl.ly/image/3F3G2X1h3J3h/Image%202015-09-07%20at%2010%3A28%3A52.png)

### IntelliJ-specific notes

One caveat found using IntelliJ is that you must mark your source directory as a "Generated sources root"
for IntelliJ to add the contents to your classpath. For example, if you setup your target directory as
"generated/source/apt/main", right-click on the 'main' subfolde and click "Mark Directory as -> Generated sources root".

Another issue is that upon changes to the build.gradle file or reimporting the gradle project, the "sources root" designation
may be cleared, and it will need to be re-marked.
