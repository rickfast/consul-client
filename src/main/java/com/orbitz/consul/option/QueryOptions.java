package com.orbitz.consul.option;

/**
 * Container for common query options used by the Consul API.
 */
public class QueryOptions {

    private boolean blocking;
    private String wait;
    private long index;
    private ConsistencyMode consistencyMode;
    private boolean authenticated;
    private String token;

    public static QueryOptions BLANK = new QueryOptions(null, 0, ConsistencyMode.DEFAULT, null);

    /**
     * @param wait Wait string, e.g. "10s" or "10m"
     * @param index Lock index.
     * @param consistencyMode Consistency mode to use for query.
     */
    QueryOptions(String wait, long index, ConsistencyMode consistencyMode, String token) {
        this.wait = wait;
        this.index = index;
        this.consistencyMode = consistencyMode;
        this.blocking = wait != null;
        this.token = token;
        this.authenticated = token != null;
    }

    public String getWait() {
        return wait;
    }

    public long getIndex() {
        return index;
    }

    public ConsistencyMode getConsistencyMode() {
        return consistencyMode;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public boolean hasToken() { return authenticated; }

    public String getToken() { return token; }
}
