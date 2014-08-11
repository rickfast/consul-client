package com.orbitz.consul.query;

public class QueryOptions {

    private boolean blocking;
    private String wait;
    private int index;
    private ConsistencyMode consistencyMode;

    public static QueryOptions BLANK = new QueryOptions(null, 0, ConsistencyMode.DEFAULT);

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
