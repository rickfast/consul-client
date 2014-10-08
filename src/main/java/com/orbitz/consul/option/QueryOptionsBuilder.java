package com.orbitz.consul.option;

/**
 * Builder for generating Query Options for Consul API calls.
 */
public class QueryOptionsBuilder {

    private String wait;
    private int index;
    private ConsistencyMode consistencyMode = ConsistencyMode.DEFAULT;

    private QueryOptionsBuilder() {

    }

    public static QueryOptionsBuilder builder() {
        return new QueryOptionsBuilder();
    }

    public QueryOptionsBuilder blockMinutes(int minutes, int index) {
        this.wait = String.format("%sm", minutes);
        this.index = index;

        return this;
    }

    public QueryOptionsBuilder blockSeconds(int seconds, int index) {
        this.wait = String.format("%ss", seconds);
        this.index = index;

        return this;
    }

    public QueryOptionsBuilder consistencyMode(ConsistencyMode consistencyMode) {
        this.consistencyMode = consistencyMode;

        return this;
    }

    public QueryOptionsBuilder queryOptions(QueryOptions queryOptions) {
        this.wait = queryOptions.getWait();
        this.index = queryOptions.getIndex();
        this.consistencyMode = queryOptions.getConsistencyMode();

        return this;
    }

    public QueryOptions build() {
        return new QueryOptions(wait, index, consistencyMode);
    }
}
