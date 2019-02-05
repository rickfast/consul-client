package com.orbitz.consul.option;

import java.util.Optional;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;

import static com.orbitz.consul.option.Options.optionallyAdd;

/**
 * Container for common transaction options used by the Consul API.
 */
@Value.Immutable
public abstract class TransactionOptions implements ParamAdder {

    public static final TransactionOptions BLANK = ImmutableTransactionOptions.builder().build();

    public abstract Optional<String> getDatacenter();

    @Value.Default
    public ConsistencyMode getConsistencyMode() {
        return ConsistencyMode.DEFAULT;
    }

    @Override
    public Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        Optional<String> consistencyMode = getConsistencyMode().toParam();
        consistencyMode.ifPresent(s -> result.put(s, "true"));

        optionallyAdd(result, "dc", getDatacenter());

        return result;
    }
}
