[![Build Status](https://api.shippable.com/projects/543452637a7fb11eaa64a5c8/badge?branchName=master)](https://app.shippable.com/projects/543452637a7fb11eaa64a5c8/builds/latest)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go [here](http://www.consul.io/docs/agent/http.html).

***Warning***: Versions prior to 0.5 use Apache CXF's JAX-RS client, which has a memory leak.  0.5 and beyond use Jersey, which does not.  See [https://git-wip-us.apache.org/repos/asf?p=cxf.git;a=commitdiff;h=c9e85e76](https://git-wip-us.apache.org/repos/asf?p=cxf.git;a=commitdiff;h=c9e85e76)

Installation
-----------

###Bintray:

Grab the latest binary (0.5) [here](http://dl.bintray.com/orbitz/consul-client/com/orbitz/consul/consul-client/0.5/#consul-client-0.5.jar).

###Gradle:

**Note:** Maven Central inclusion pending.  Should be available soon.

```groovy
repositories {
    jcenter() // or mavenCentral()
}

dependencies {
    compile 'com.orbitz.consul:consul-client:0.5'
}
```

###Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.orbitz.consul</groupId>
        <artifactId>consul-client</artifactId>
        <version>0.5</version>
    <dependency>
<dependencies>
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

<List<ServiceHealth> nodes = healthClient.getHealthyNodes("DataService").getResponse(); // discover only "passing" nodes
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
import static com.orbitz.consul.option.QueryOptionsBuilder;

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

healthClient.getHealthyNodes("my-service", builder().blockMinutes(1, 0).build());
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