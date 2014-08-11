package com.orbitz.consul.option;

public class PutOptionsBuilder {

    private Integer cas;
    private String acquire;
    private String release;

    private PutOptionsBuilder() {

    }

    public static PutOptionsBuilder builder() {
        return new PutOptionsBuilder();
    }

    public PutOptionsBuilder cas(int cas) {
        this.cas = cas;

        return this;
    }

    public PutOptionsBuilder acquire(String acquire) {
        this.acquire = acquire;

        return this;
    }

    public PutOptionsBuilder release(String release) {
        this.release = release;

        return this;
    }
}
