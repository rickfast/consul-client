package com.orbitz.consul.option;

import com.google.common.base.Optional;

public enum ConsistencyMode {
    DEFAULT(null), STALE("stale"), CONSISTENT("consistent");

    private String param;

    ConsistencyMode(String param) {
        this.param = param;
    }

    public Optional<String> toParam() {
        return Optional.fromNullable(param);
    }
}
