package com.orbitz.consul.model;

import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.HealthCheck;

/**
 * Represents the possible Check states.
 */
public enum State {

    PASS("pass", "passing"), WARN("warn", "warning"), FAIL("fail", "critical"), ANY("any", "any"),
    UNKNOWN("unknown", "unknown");

    private String path;
    private String name;

    /**
     * Private constructor.
     *
     * @param path Consul API path value.
     */
    private State(String path, String name) {
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
	 * get {@link State} by name that could be return via
	 * {@link HealthClient#getHealthyServiceInstances} or
	 * {@link HealthClient#getAllServiceInstances} in {@link HealthCheck#status}
	 * 
	 * @param name
	 * @return
	 */
	public static State getByName(String name) {
		switch (name) {
			case "critical":
				return State.FAIL;
			case "warning":
				return State.WARN;
			case "passing":
				return State.PASS;
			case "any":
				return State.ANY;
			default:
				return State.UNKNOWN;
		}
	}
}
