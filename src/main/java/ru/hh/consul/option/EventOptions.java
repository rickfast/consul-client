package ru.hh.consul.option;

import java.util.Optional;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;

import static ru.hh.consul.option.Options.optionallyAdd;

@Value.Immutable
@Value.Style(jdkOnly = true)
public abstract class EventOptions implements ParamAdder {

    public static final EventOptions BLANK = ImmutableEventOptions.builder().build();

    public abstract Optional<String> getDatacenter();
    public abstract Optional<String> getNodeFilter();
    public abstract Optional<String> getServiceFilter();
    public abstract Optional<String> getTagFilter();

    @Override
    public Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        optionallyAdd(result, "node", getNodeFilter());
        optionallyAdd(result, "service", getServiceFilter());
        optionallyAdd(result, "tag", getTagFilter());
        optionallyAdd(result, "dc", getDatacenter());

        return result;
    }
}
