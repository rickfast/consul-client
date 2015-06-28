[ ![Codeship Status for OrbitzWorldwide/consul-client](https://codeship.com/projects/d1bec4e0-fff2-0132-2c7a-62f74f018091/status?branch=master)](https://codeship.com/projects/88244)
[ ![Download](https://api.bintray.com/packages/orbitz/consul-client/consul-client/images/download.svg) ](https://bintray.com/orbitz/consul-client/consul-client/_latestVersion)
Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

Installation
-----------

###Bintray:

Grab the latest binary (0.9.6) [here](http://dl.bintray.com/orbitz/consul-client/com/orbitz/consul/consul-client/0.9.6/#consul-client-0.9.6.jar).

###Gradle:

```groovy
repositories {
    jcenter() // or mavenCentral()
}

dependencies {
    compile 'com.orbitz.consul:consul-client:0.9.6'
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
        <version>0.9.6</version>
    </dependency>
    <!-- include your preferred javax.ws.rs-api implementation -->
</dependencies>
```

Basic Usage
-----------

Example 1: Register and check your service in with Consul.  Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".

```java
Consul consul = Consul.newClient(); // connect to Consul on localhost
AgentClient agentClient = consul.agentClient();

String serviceName = "MyService";
String serviceId = "1";

agentClient.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
agentClient.pass(); // check in with Consul
```

Example 2: Find available (healthy) services.

```java
Consul consul = Consul.newClient(); // connect to Consul on localhost
HealthClient healthClient = consul.healthClient();

List<ServiceHealth> nodes = healthClient.getHealthyNodes("DataService").getResponse(); // discover only "passing" nodes
```

Example 3: Store key/values.

```java
Consul consul = Consul.newClient(); // connect to Consul on localhost
KeyValueClient kvClient = consul.keyValueClient();

kvClient.putValue("foo", "bar");

String value = kvClient.getValueAsString("foo").get(); // bar
```

Example 4: Blocking call for value.

```java
import static com.orbitz.consul.option.QueryOptionsBuilder.builder;

Consul consul = Consul.newClient();
KeyValueClient kvClient = consul.keyValueClient();

kvClient.putValue("foo", "bar");

Value value = kvClient.getValue("foo", builder().blockMinutes(10, 120).build()).get(); // will block (long poll) for 10 minutes or until "foo"'s value changes.
```

Example 5: Blocking call for healthy services using callback.

```java
Consul consul = Consul.newClient();
final HealthClient healthClient = consul.healthClient();

ConsulResponseCallback<List<ServiceHealth>> callback = new ConsulResponseCallback<List<ServiceHealth>>() {

    int index;

    @Override
    public void onComplete(ConsulResponse<List<ServiceHealth>> consulResponse) {
        for(ServiceHealth health : consulResponse.getResponse()) {
            String host = health.getNode().getAddress();
            int port = health.getService().getPort();

            // do something with this service information
        }

        index = consulResponse.getIndex();

        // blocking request with new index
        healthClient.getHealthyNodes("my-service", builder().blockMinutes(5, index).build(), this);
    }

    @Override
    public void onFailure(Throwable throwable) {
        throwable.printStackTrace();
        healthClient.getHealthyNodes("my-service", builder().blockMinutes(5, index).build(), this);
    }
};

healthClient.getHealthyNodes("my-service", builder().blockMinutes(1, 0).build(), callback);
```         

Example 6: Find Raft peers.

```java
StatusClient statusClient = Consul.newClient().statusClient();

for(String peer : statusClient.getPeers()) {
	System.out.println(peer); // 127.0.0.1:8300
}
```

Example 7: Find Raft leader.

```java
StatusClient statusClient = Consul.newClient().statusClient();

System.out.println(statusClient.getLeader()); // 127.0.0.1:8300
```
