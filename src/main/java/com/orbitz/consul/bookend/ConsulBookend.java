package com.orbitz.consul.bookend;

public interface ConsulBookend {

    void pre(String url, ConsulBookendContext context);
    void post(boolean success);
}
