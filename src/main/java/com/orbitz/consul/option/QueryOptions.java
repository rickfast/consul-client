package com.orbitz.consul.option;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Container for common query options used by the Consul API.
 */
public class QueryOptions {

    private boolean blocking;
    private String wait;
    private BigInteger index;
    private ConsistencyMode consistencyMode;
    private boolean authenticated;
    private String token;

    public static QueryOptions BLANK = new QueryOptions(null, new BigInteger("0"), ConsistencyMode.DEFAULT, null);

    /**
     * @param wait Wait string, e.g. "10s" or "10m"
     * @param index Lock index.
     * @param consistencyMode Consistency mode to use for query.
     */
    QueryOptions(String wait, BigInteger index, ConsistencyMode consistencyMode, String token) {

        this.wait = wait;
        this.index = index;
        this.consistencyMode = consistencyMode;
        this.blocking = wait != null;
        this.token = token;
        this.authenticated = token != null;
        if (blocking) {
            checkArgument(index != null, "If wait is specified, index must also be specified");
        }
    }

    public String getWait() {
        return wait;
    }

    public BigInteger getIndex() {
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
