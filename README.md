[ ![Codeship Status for OrbitzWorldwide/consul-client](https://codeship.com/projects/d1bec4e0-fff2-0132-2c7a-62f74f018091/status?branch=master)](https://codeship.com/projects/88244)
[ ![Download](https://api.bintray.com/packages/orbitz/consul-client/consul-client/images/download.svg) ](https://bintray.com/orbitz/consul-client/consul-client/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.orbitz.consul/consul-client)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

Installation
-----------

###Bintray:

Grab the latest binary (0.9.14) [here](http://dl.bintray.com/orbitz/consul-client/com/orbitz/consul/consul-client/0.9.14/#consul-client-0.9.14.jar).

###Gradle:

```groovy
repositories {
    jcenter() // or mavenCentral()
}

dependencies {
    compile 'com.orbitz.consul:consul-client:0.9.14'
    // include your preferred javax.ws.rs-api implementation, for example:
    compile 'org.apache.cxf:cxf-rt-rs-client:3.0.3'
    compile 'org.apache.cxf:cxf-rt-transports-http-hc:3.0.3'
}
```

###Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.orbitz.consul</groupId>
        <artifactId>consul-client</artifactId>
        <version>0.9.14</version>
    </dependency>
    <!-- include your preferred javax.ws.rs-api implementation -->
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

Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".

```java
Consul consul = Consul.builder().build(); // connect to Consul on localhost
AgentClient agentClient = consul.agentClient();

String serviceName = "MyService";
String serviceId = "1";

agentClient.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
agentClient.pass(serviceId); // check in with Consul, serviceId required only.  client will prepend "service:" for service level checks.
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

```java
        Consul consul = Consul.builder().build();
        final KeyValueClient kvClient = consul.keyValueClient();

        kvClient.putValue("foo", "bar");

        ConsulResponseCallback<Optional<Value>> callback = new ConsulResponseCallback<Optional<Value>>() {

            AtomicReference<BigInteger> index = new AtomicReference<BigInteger>(null);

            @Override
            public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {

                if (consulResponse.getResponse().isPresent()) {
                    Value v = consulResponse.getResponse().get();
                    LOGGER.info("Value is: {}", new String(BaseEncoding.base64().decode(v.getValue().toString())));
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

### Example 6: Subscribe to healthy services

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

### Example 7: Find Raft peers.

```java
StatusClient statusClient = Consul.builder().build().statusClient();

for(String peer : statusClient.getPeers()) {
	System.out.println(peer); // 127.0.0.1:8300
}
```

### Example 8: Find Raft leader.

```java
StatusClient statusClient = Consul.builder().build().statusClient();

System.out.println(statusClient.getLeader()); // 127.0.0.1:8300
```

Development Notes
-----------

`consul-client` makes use of [immutables](http://immutables.github.io/) to generate code for many of the value classes.
This provides a lot of functionality and benefit for little code, but it does require some additional development setup.

Official instructions are [here](http://immutables.github.io/apt.html), although you may want to change the target directories to the more gradle-like "generated/source/apt/main" and  "generated/source/apt/test" targets.

### Eclipse-specific notes

Their instructions for eclipse a bit difficult to grok, but I was able to get eclipse to compile by following the second part of the instructions. Essentially, enable annotation processing, then extend the M2_REPO variable to include the immutables annotation processor. One thing is that documentation is out of date in that it tells you the wrong jar to include - it should be org/immutables/value/2.0.16/value-2.0.16.jar.

![extending M2_REPO](http://cl.ly/image/3F3G2X1h3J3h/Image%202015-09-07%20at%2010%3A28%3A52.png)

### IntelliJ-specific notes

One caveat found using IntelliJ is that you must mark your source directory as a "Generated sources root" 
for IntelliJ to add the contents to your classpath. For example, if you setup your target directory as 
"generated/source/apt/main", right-click on the 'main' subfolde and click "Mark Directory as -> Generated sources root". 

Another issue is that upon changes to the build.gradle file or reimporting the gradle project, the "sources root" designation 
may be cleared, and it will need to be re-marked.

