package com.orbitz.consul.model;

/**
 * Represents the possible Check states.
 */
public enum State {

    PASS("pass"), WARN("warn"), FAIL("fail");

    private String path;

    /**
     * Private constructor.
     *
     * @param path Consul API path value.
     */
    private State(String path) {
        this.path = path;
    }

    /**
     * Retrieve the path value for the Consul check API endpoints.
     *
     * @return The path value, e.g. "pass" for PASS.
     */
    public String getPath() {
        return path;
    }
}
