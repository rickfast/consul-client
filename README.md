![Consul](https://d13yacurqjgara.cloudfront.net/users/42318/screenshots/1514846/consul-logo-grad_teaser.png)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go here.

Downloading
-----------

TBD.

Basic Usage
-----------

Example 1: Register and check your service in with Consul.  Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".

```
Consul consul = Consul.newClient(); // connect to Consul on localhost
AgentClient agentClient = consul.agentClient();

String serviceName = "MyService";
String serviceId = "1";

agentClient.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
agentClient.pass(); // check in with Consul
```

Example 2: Find available (healthy) services.

```
Consul consul = Consul.newClient(); // connect to Consul on localhost
HealthClient healthClient = consul.healthClient();

List<ServiceHealth> nodes = healthClient.getHealthyNodes("DataService"); // discover only "passing" nodes
```