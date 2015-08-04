package com.orbitz.consul.option;

import com.google.common.base.Optional;
import org.immutables.value.Value;

import javax.ws.rs.client.WebTarget;

import static com.orbitz.consul.option.Options.optionallyAdd;

@Value.Immutable
public abstract class PutOptions implements ParamAdder {
    
    public static PutOptions BLANK = ImmutablePutOptions.builder().build();
    
    public abstract Optional<Integer> getCas();
    public abstract Optional<String> getAcquire();
    public abstract Optional<String> getRelease();
    public abstract Optional<String> getDc();

    @Override
    public final WebTarget apply(final WebTarget input) {
        WebTarget added = optionallyAdd(input, "cas", getCas());

        added = optionallyAdd(added, "release", getRelease());
        added = optionallyAdd(added, "acquire", getAcquire());
        added = optionallyAdd(added, "dc", getDc());

        return added;
    }
}
