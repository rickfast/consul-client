package com.orbitz.consul.option;

import java.util.Optional;

import java.util.HashMap;
import java.util.Map;

public class Options {
    private Options(){}

    static void optionallyAdd(Map<String, Object> data, String key, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<?> val) {
        val.ifPresent(value -> data.put(key, value.toString()));
    }

    public static Map<String, Object> from(ParamAdder... options) {
        Map<String, Object> result = new HashMap<>();

        for (ParamAdder adder : options) {
            if (adder != null) {
                result.putAll(adder.toQuery());
            }
        }

        return result;
    }
}
