package com.orbitz.consul.option;

/**
 * Container for common query options used by the Consul API.
 */
public class QueryOptions {

    private boolean blocking;
    private String wait;
    private int index;
    private ConsistencyMode consistencyMode;

    public static QueryOptions BLANK = new QueryOptions(null, 0, ConsistencyMode.DEFAULT);

    /**
     * @param wait Wait string, e.g. "10s" or "10m"
     * @param index Lock index.
     * @param consistencyMode Consistency mode to use for query.
     */
    QueryOptions(String wait, int index, ConsistencyMode consistencyMode) {
        this.wait = wait;
        this.index = index;
        this.consistencyMode = consistencyMode;
        this.blocking = wait != null;
    }

    public String getWait() {
        return wait;
    }

    public int getIndex() {
        return index;
    }

    public ConsistencyMode getConsistencyMode() {
        return consistencyMode;
    }

    public boolean isBlocking() {
        return blocking;
    }
}
