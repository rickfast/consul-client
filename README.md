[![Build Status](https://travis-ci.org/OrbitzWorldwide/consul-client.svg?branch=master)](https://travis-ci.org/OrbitzWorldwide/consul-client)
[ ![Download](https://api.bintray.com/packages/orbitz/consul-client/consul-client/images/download.svg) ](https://bintray.com/orbitz/consul-client/consul-client/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

Installation
-----------

### Note

In 0.13.x, both shaded and non-shaded JARs are provided. The shaded JAR has a `shaded` classifier, while the non-shaded JAR has no classifier. Note that this is a change from 0.12 and 0.11.

In 0.11.X and 0.12.x, the Consul JAR is a shaded JAR, with most dependencies included. This was done because a number of issues being files were related to dependency conflicts. The JAR is a bit bigger, but the HTTP + JSON libraries are now internal to the JAR. Only Guava is still a transitive dependency.

### Bintray:

Grab the latest binary (0.17.0) [here](http://dl.bintray.com/orbitz/consul-client/com/orbitz/consul/consul-client/0.13.11/#consul-client-0.13.11.jar).

### Gradle:

```groovy
repositories {
    jcenter() // or mavenCentral()
}

dependencies {
    compile 'com.orbitz.consul:consul-client:0.17.0'
}
```

### Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.orbitz.consul</groupId>
        <artifactId>consul-client</artifactId>
        <version>0.17.0</version>
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

### Example 1: Register and check your service in with Consul.  

```java
Consul consul = Consul.builder().build(); // connect to Consul on localhost
AgentClient agentClient = consul.agentClient();

String serviceName = "MyService";
String serviceId = "1";

agentClient.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
agentClient.pass(serviceId); // check in with Consul, serviceId required only.  client will prepend "service:" for service level checks.
// Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".
```

### Example 2: Find available (healthy) services.

```java
Consul consul = Consul.builder().build(); // connect to Consul on localhost
HealthClient healthClient = consul.healthClient();

List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("DataService").getResponse(); // discover only "passing" nodes
```

### Example 3: Store key/values.

```java
Consul consul = Consul.builder().build(); // connect to Consul on localhost
KeyValueClient kvClient = consul.keyValueClient();

kvClient.putValue("foo", "bar");

String value = kvClient.getValueAsString("foo").get(); // bar
```

### Example 4: Blocking call for value.

A blocking is used to wait for a potential changes in the key value store. 

```java
// Set a read timeout to a larger value then we will block.
Consul consul = Consul.builder()
    .withReadTimeoutMillis(TimeUnit.SECONDS.toMillis(315))
    .build();
final KeyValueClient kvClient = consul.keyValueClient();

kvClient.putValue("foo", "bar");

ConsulResponseCallback<Optional<Value>> callback = new ConsulResponseCallback<Optional<Value>>() {

    AtomicReference<BigInteger> index = new AtomicReference<BigInteger>(null);

    @Override
    public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {

        if (consulResponse.getResponse().isPresent()) {
            Value v = consulResponse.getResponse().get();
            LOGGER.info("Value is: {}", v.getValue());
        }
        
	index.set(consulResponse.getIndex());
        watch();
    }

    void watch() {
        kvClient.getValue("foo", QueryOptions.blockMinutes(5, index.get()).build(), this);
    }

    @Override
        public void onFailure(Throwable throwable) {
            LOGGER.error("Error encountered", throwable);
            watch();
        }
    };

    kvClient.getValue("foo", QueryOptions.blockMinutes(5, new BigInteger("0")).build(), callback);
```

### Example 5: Subscribe to healthy services

You can also use the ConsulCache implementations to easily subscribe to healthy service changes or Key-Value changes.

```java

Agent agent = client.agentClient().getAgent();
String serviceName = "my-service";

ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);

svHealth.addListener(new ConsulCache.Listener<HostAndPort, ServiceHealth>() {
    @Override
    public void notify(Map<HostAndPort, ServiceHealth> newValues) {
        // do Something with updated server map
    }
});
svHealth.start();
```         

### Example 6: Find Raft peers.

```java
StatusClient statusClient = Consul.builder().build().statusClient();

for(String peer : statusClient.getPeers()) {
	System.out.println(peer); // 127.0.0.1:8300
}
```

### Example 7: Find Raft leader.

```java
StatusClient statusClient = Consul.builder().build().statusClient();

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
