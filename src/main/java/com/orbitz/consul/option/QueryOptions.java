package com.orbitz.consul.option;

import com.google.common.base.Optional;
import org.immutables.value.Value;

import javax.ws.rs.client.WebTarget;
import java.math.BigInteger;

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
    public abstract Optional<BigInteger> getIndex();

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

    @Value.Check
    void validate() {
        if (isBlocking()) {
            checkArgument(getIndex().isPresent(), "If wait is specified, index must also be specified");
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

    @Override
    public WebTarget apply(WebTarget input) {

        WebTarget added = input;
        switch (getConsistencyMode()) {
            case CONSISTENT:
                added = added.queryParam("consistent");
                break;
            case STALE:
                added = added.queryParam("stale");
                break;
        }

        if (isBlocking()) {
            added = added.queryParam("wait", getWait().get())
                    .queryParam("index", String.valueOf(getIndex().get()));
        }

        added = optionallyAdd(added, "token", getToken());
        return added;
    }
}
