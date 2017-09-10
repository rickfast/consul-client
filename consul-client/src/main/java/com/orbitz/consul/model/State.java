package com.orbitz.consul.model;

/**
 * Represents the possible Check states.
 */
public enum State {

    PASS("pass", "passing"), WARN("warn", "warning"), FAIL("fail", "critical"), ANY("any", "any"),
    UNKNOWN("unknown", "unknown");

    private final String path;
    private final String name;

    /**
     * @param path Consul API path value.
     */
    State(String path, String name) {
        this.path = path;
        this.name = name;
    }

    /**
     * Retrieve the path value for the Consul check API endpoints.
     *
     * @return The path value, e.g. "pass" for PASS.
     */
    public String getPath() {
        return path;
    }

    /**
     * Retrieve the name value for the Consul check API endpoints.  This is the value
     * to use for querying services by health state.
     *
     * @return The name, e.g. "passing" for PASS.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the appropriate {@link com.orbitz.consul.model.State} given the
     * name.
     *
     * @param name The state name e.g. "passing".
     * @return The state.
     */
    public static State fromName(String name) {
        for(State state : values()) {
            if(state.getName().equals(name)) {
                return state;
            }
        }

        throw new IllegalArgumentException("Invalid State name");
    }
}
