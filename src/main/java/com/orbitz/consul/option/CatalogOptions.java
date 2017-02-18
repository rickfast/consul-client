package com.orbitz.consul.option;

import com.google.common.base.Optional;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.orbitz.consul.option.Options.optionallyAdd;

@Value.Immutable
@Deprecated
/**
 * @deprecated Will be rolled into {@link QueryOptions} in the next major
 *  release.
 */
public abstract class CatalogOptions implements ParamAdder {

    public abstract Optional<String> getDatacenter();
    public abstract List<String> getTag();

    public static final CatalogOptions BLANK = ImmutableCatalogOptions.builder().build();

    @Override
    public final Map<String, Object> toQuery() {
        Map<String, Object> result = new HashMap<>();

        optionallyAdd(result, "dc", getDatacenter());

        return result;
    }
}
