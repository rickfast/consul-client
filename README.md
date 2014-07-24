![Consul](https://d13yacurqjgara.cloudfront.net/users/42318/screenshots/1514846/consul-logo-grad_teaser.png)

Consul Client for Java
======================

Simple client for the Consul HTTP API.  For more information about the Consul HTTP API, go here.

Downloading
-----------

TBD.

Basic Usage
-----------

```
ConsulClient client = ConsulClient.newClient(); // connect to Consul on localhost
String serviceName = "MyService";
String serviceId = "1";

client.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
```