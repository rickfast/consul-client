package com.orbitz.consul.option;

/**
 * Container for common query options used by the Consul API.
 */
public class QueryOptions {

    private boolean blocking;
    private String wait;
    private int index;
    private ConsistencyMode consistencyMode;
    private boolean passing;

    public static QueryOptions BLANK = new QueryOptions(null, 0, ConsistencyMode.DEFAULT, false);

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
    
    /**
     * @param wait Wait string, e.g. "10s" or "10m"
     * @param index Lock index.
     * @param consistencyMode Consistency mode to use for query.
     * @param passing query parameter, added in Consul 0.2, will filter results to only nodes with all checks in the passing state. This can be used to avoid extra filtering logic on the client side
     */
    QueryOptions(String wait, int index, ConsistencyMode consistencyMode, boolean passing) {
        this(wait, index, consistencyMode);
        this.passing = passing;
    }
    
    public boolean passing(){
    	return passing;
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
