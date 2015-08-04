package com.orbitz.consul.option;

import com.google.common.base.Optional;
import org.immutables.value.Value;

import javax.ws.rs.client.WebTarget;

import static com.orbitz.consul.option.Options.optionallyAdd;

@Value.Immutable
public abstract class CatalogOptions implements ParamAdder {

    public abstract Optional<String> getDatacenter();
    public abstract Optional<String> getTag();

    public static final CatalogOptions BLANK = ImmutableCatalogOptions.builder().build();

    @Override
    public final WebTarget apply(final WebTarget input) {
        WebTarget added = optionallyAdd(input, "dc", getDatacenter());
        added = optionallyAdd(added, "tag", getTag());
        return added;
    }
}
