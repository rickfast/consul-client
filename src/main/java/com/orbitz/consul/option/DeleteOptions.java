package com.orbitz.consul.option;

import static com.orbitz.consul.option.Options.optionallyAdd;

import java.util.HashMap;
import java.util.Map;

import org.immutables.value.Value;

import com.google.common.base.Optional;

@Value.Immutable
public abstract class DeleteOptions implements ParamAdder {

	public static final DeleteOptions BLANK = ImmutableDeleteOptions.builder().build();
	public static final DeleteOptions RECURSE = ImmutableDeleteOptions.builder().recurse(true).build();

	public abstract Optional<Long> getCas();

	public abstract Optional<Boolean> getRecurse();
	
	public abstract Optional<String> getDatacenter();

	@Value.Derived
	public boolean isRecurse() {
		return getRecurse().isPresent();
	}

	@Override
	public Map<String, Object> toQuery() {
		final Map<String, Object> result = new HashMap<>();

		if (isRecurse()) {
			result.put("recurse", "");
		}

		optionallyAdd(result, "cas", getCas());
		optionallyAdd(result, "dc", getDatacenter());

		return result;
	}
}