package com.orbitz.consul.option;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

public class ConsistencyMode {
    public final static ConsistencyMode DEFAULT = new ConsistencyMode("DEFAULT", 0, null);
    public final static ConsistencyMode STALE = new ConsistencyMode("STALE", 1, "stale");
    public final static ConsistencyMode CONSISTENT = new ConsistencyMode("CONSISTENT", 2, "consistent");

    private final String name;
    private final int ordinal;
    private final String param;
    private final Map<String, String> additionalHeaders;

    private ConsistencyMode(final String name, int ordinal, final String param) {
        this(name, ordinal, param, Collections.emptyMap());
    }

    private ConsistencyMode(final String name, int ordinal, final String param, final Map<String, String> headers) {
        this.name = name;
        this.ordinal = ordinal;
        this.param = param;
        this.additionalHeaders = headers;
    }

    public final Optional<String> toParam() {
        return Optional.ofNullable(param);
    }

    /**
     * Get the Additional HTTP headers to add to request.
     *
     * @return a not null but possibly empty map
     */
    public final Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    /**
     * Creates a cached Consistency.
     *
     * @param maxAgeInSeconds   Optional duration in seconds after which data is
     *                          considered too old
     * @param maxStaleInSeconds Optional duration for which data can be late
     *                          compared to Consul Server leader.
     * @return a not null ConsistencyMode
     * @see https://www.consul.io/api/features/caching.html#simple-caching
     */
    public final static ConsistencyMode createCachedConsistencyWithMaxAgeAndStale(final Optional<Long> maxAgeInSeconds,
            final Optional<Long> maxStaleInSeconds) {
        String maxAge = "";
        if (maxAgeInSeconds.isPresent()) {
            final long v = maxAgeInSeconds.get().longValue();
            if (v < 0) {
                throw new IllegalArgumentException("maxAgeInSeconds must greater or equal to 0");
            }
            maxAge += String.format("max-age=%d", v);
        }

        if (maxStaleInSeconds.isPresent()) {
            final long v = maxStaleInSeconds.get().longValue();
            if (v < 0){
                throw new IllegalArgumentException("maxStaleInSeconds must greater or equal to 0");
            }
            if (!maxAge.isEmpty()){
                maxAge += ",";
            }
            maxAge += String.format("stale-if-error=%d", v);
        }
        Map<String, String> headers;
        if (maxAge.isEmpty()) {
            headers = Collections.emptyMap();
        } else {
            headers = ImmutableMap.of("Cache-Control", maxAge);
        }
        return new ConsistencyMode("CACHED", 3, "cached", headers);
    }

    // The next methods are for compatibility with old enum type
    /**
     * ConsistencyMode used t be an enum, implement it.
     * @return the old enum name
     */
    public final String name(){
        return name;
    }

    @Override
    public final String toString() {
        String s = name();
        for (Map.Entry<String, String> en : getAdditionalHeaders().entrySet()) {
            s += String.format("[%s=%s]", en.getKey(), en.getValue());
        }
        return s;
    }

    public int ordinal(){
        return ordinal;
    }

    public static final ConsistencyMode[] values(){
        ConsistencyMode[] res = new ConsistencyMode[3];
        res[0] = DEFAULT;
        res[1] = STALE;
        res[2] = CONSISTENT;
        // Don't push CACHED as it is just to keep backward compatibility
        return res;
    }
}
