package com.orbitz.consul.option;

import com.google.common.base.Optional;
import org.immutables.value.Value;

import javax.ws.rs.client.WebTarget;

import static com.orbitz.consul.option.Options.optionallyAdd;

@Value.Immutable
public abstract class EventOptions implements ParamAdder {

    public static final EventOptions BLANK = ImmutableEventOptions.builder().build();

    public abstract Optional<String> getDatacenter();
    public abstract Optional<String> getNodeFilter();
    public abstract Optional<String> getServiceFilter();
    public abstract Optional<String> getTagFilter();

    @Override
    public final WebTarget apply(final WebTarget input) {
        WebTarget added = optionallyAdd(input, "dc", getDatacenter());

        added = optionallyAdd(added, "node", getNodeFilter());
        added = optionallyAdd(added, "service", getServiceFilter());
        added = optionallyAdd(added, "tag", getTagFilter());

        return added;
    }
}
