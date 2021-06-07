package com.orbitz.consul.option;

import java.util.List;
import java.util.Map;

public interface ParamAdder {

    Map<String, Object> toQuery();

    List<String> toQueryParameters();

    Map<String, String> toHeaders();
}
