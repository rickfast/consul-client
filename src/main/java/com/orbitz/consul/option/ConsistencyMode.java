package com.orbitz.consul.option;

import java.util.Optional;

public enum ConsistencyMode {
    DEFAULT(null), STALE("stale"), CONSISTENT("consistent");

    private String param;

    ConsistencyMode(String param) {
        this.param = param;
    }

    public Optional<String> toParam() {
        return Optional.ofNullable(param);
    }
}
