package ru.hh.consul.option;

import java.util.Optional;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;

import static ru.hh.consul.option.Options.optionallyAdd;

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

    public Map<String, String> toHeaders() {
        Map<String, String> result = new HashMap<>();

        ConsistencyMode consistencyMode = getConsistencyMode();
        result.putAll(consistencyMode.getAdditionalHeaders());
        return result;
    }
}
