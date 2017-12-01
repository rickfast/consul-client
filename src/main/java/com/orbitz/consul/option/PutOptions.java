package com.orbitz.consul.option;

import java.util.Optional;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;

import static com.orbitz.consul.option.Options.optionallyAdd;

@Value.Immutable
public abstract class PutOptions implements ParamAdder {
    
    public static final PutOptions BLANK = ImmutablePutOptions.builder().build();
    
    public abstract Optional<Long> getCas();
    public abstract Optional<String> getAcquire();
    public abstract Optional<String> getRelease();
    public abstract Optional<String> getDc();
    public abstract Optional<String> getToken();

    @Override
    public final Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        optionallyAdd(result, "dc", getDc());
        optionallyAdd(result, "cas", getCas());
        optionallyAdd(result, "acquire", getAcquire());
        optionallyAdd(result, "release", getRelease());
        optionallyAdd(result, "token", getToken());

        return result;
    }
}
