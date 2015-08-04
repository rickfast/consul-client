package com.orbitz.consul.option;

import com.google.common.base.Optional;

import javax.ws.rs.client.WebTarget;

public class Options {
    private Options(){};

    static <T> WebTarget optionallyAdd(WebTarget input, String key, Optional<T> val) {
        return val.isPresent() ? input.queryParam(key, val.get()) : input;
    }
}
