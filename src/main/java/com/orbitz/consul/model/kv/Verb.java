package com.orbitz.consul.model.kv;

public enum Verb {

    SET("set"), CHECK_AND_SET("cas"), LOCK("lock"), UNLOCK("unlock"), GET("get"),
    GET_TREE("get-tree"), CHECK_INDEX("check-index"), CHECK_SESSION("check-session"),
    DELETE("delete"), DELETE_TREE("delete-tree"), DELETE_CHECK_AND_SET("delete-cas");

    private String value;

    Verb(String value) {
        this.value = value;
    }

    public String toValue() {
        return value;
    }
}
