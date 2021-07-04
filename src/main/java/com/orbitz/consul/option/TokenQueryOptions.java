package com.orbitz.consul.option;

import org.immutables.value.Value;

import java.util.*;

import static com.orbitz.consul.option.Options.optionallyAdd;

/**
 * Container for token query options used by the Consul ACL API.
 */
@Value.Immutable
public abstract class TokenQueryOptions implements ParamAdder {

    public static final TokenQueryOptions BLANK = ImmutableTokenQueryOptions.builder().build();

    public abstract Optional<String> getPolicy();
    public abstract Optional<String> getRole();
    public abstract Optional<String> getAuthMethod();
    public abstract Optional<String> getAuthMethodNamespace();
    public abstract Optional<String> getNamespace();

    @Override
    public Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        optionallyAdd(result, "policy", getPolicy());
        optionallyAdd(result, "role", getRole());
        optionallyAdd(result, "authmethod", getAuthMethod());
        optionallyAdd(result, "authmethod-ns", getAuthMethodNamespace());
        optionallyAdd(result, "ns", getNamespace());

        return result;
    }
}
