package com.orbitz.consul.option;

import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.orbitz.consul.option.Options.optionallyAdd;

/**
 * Container for common query options used by the Consul API.
 */
@Value.Immutable
public abstract class RoleOptions implements ParamAdder {

    public static final RoleOptions BLANK = ImmutableRoleOptions.builder().build();

    public abstract Optional<String> getPolicy();
    public abstract Optional<String> getNamespace();


    @Override
    public Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        optionallyAdd(result, "policy", getPolicy());
        optionallyAdd(result, "ns", getNamespace());

        return result;
    }
}
