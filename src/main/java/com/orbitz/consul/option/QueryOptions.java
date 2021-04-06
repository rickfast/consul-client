package com.orbitz.consul.option;

import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.orbitz.consul.option.Options.optionallyAdd;

/**
 * Container for common query options used by the Consul API.
 */
@Value.Immutable
public abstract class QueryOptions implements ParamAdder {

    public static final QueryOptions BLANK = ImmutableQueryOptions.builder().build();

    public abstract Optional<String> getWait();
    public abstract Optional<String> getToken();
    public abstract Optional<String> getHash();
    public abstract Optional<BigInteger> getIndex();
    public abstract Optional<String> getNear();
    public abstract Optional<String> getDatacenter();
    public abstract Optional<String> getNamespace();
    public abstract List<String> getNodeMeta();
    public abstract List<String> getTag();

    @Value.Default
    public ConsistencyMode getConsistencyMode() {
        return ConsistencyMode.DEFAULT;
    }

    @Value.Derived
    public boolean isBlocking() {
        return getWait().isPresent();
    }

    @Value.Derived
    public boolean hasToken() {
        return getToken().isPresent();
    }

    @Value.Derived
    public List<String> getNodeMetaQuery() {
        return getNodeMeta() == null
                ? Collections.emptyList()
                : ImmutableList.copyOf(getNodeMeta());
    }

    @Value.Derived
    public List<String> getTagsQuery() {
        return getTag() == null
                ? Collections.emptyList()
                : ImmutableList.copyOf(getTag());
    }

    @Value.Check
    void validate() {
        if (isBlocking()) {
            checkArgument(getIndex().isPresent() || getHash().isPresent(), "If wait is specified, index/hash must also be specified");
            checkArgument(!(getIndex().isPresent() && getHash().isPresent()), "Cannot specify index and hash ath the same time");
        }
    }

    public static ImmutableQueryOptions.Builder blockSeconds(int seconds, BigInteger index) {
        return blockBuilder("s", seconds, index);
    }

    public static ImmutableQueryOptions.Builder blockMinutes(int minutes, BigInteger index) {
        return blockBuilder("m", minutes, index);
    }

    private static ImmutableQueryOptions.Builder blockBuilder(String identifier, int qty, BigInteger index) {
        return ImmutableQueryOptions.builder()
                .wait(String.format("%s%s", qty, identifier))
                .index(index);
    }

    public static ImmutableQueryOptions.Builder blockSeconds(int seconds, String hash) {
        return blockBuilder("s", seconds, hash);
    }

    public static ImmutableQueryOptions.Builder blockMinutes(int minutes, String hash) {
        return blockBuilder("m", minutes, hash);
    }

    private static ImmutableQueryOptions.Builder blockBuilder(String identifier, int qty, String hash) {
        return ImmutableQueryOptions.builder()
                .wait(String.format("%s%s", qty, identifier))
                .hash(hash);
    }

    @Override
    public Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        Optional<String> consistency = getConsistencyMode().toParam();
        if (consistency.isPresent()) {
            result.put(consistency.get(), "");
        }

        if (isBlocking()) {
            optionallyAdd(result, "wait", getWait());
            optionallyAdd(result, "index", getIndex());
            optionallyAdd(result, "hash", getHash());
        }

        optionallyAdd(result, "token", getToken());
        optionallyAdd(result, "near", getNear());
        optionallyAdd(result, "dc", getDatacenter());
        optionallyAdd(result, "ns", getNamespace());

        return result;
    }

    @Override
    public Map<String, String> toHeaders() {
        return getConsistencyMode().getAdditionalHeaders();
    }
}
