package ru.hh.consul.option;

import java.util.Map;

public interface ParamAdder {

    Map<String, Object> toQuery();

    Map<String, String> toHeaders();
}
