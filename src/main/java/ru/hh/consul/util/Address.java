package ru.hh.consul.util;


import java.util.Objects;
import static ru.hh.consul.util.Checks.checkState;

public class Address {
  private final String host;
  private final int port;

  public Address(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    checkState(port >= 0, "Port must be > 0");
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return port == address.port && host.equals(address.host);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port);
  }
}
