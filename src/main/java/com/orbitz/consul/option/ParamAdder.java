package com.orbitz.consul.option;

import com.google.common.base.Function;

import javax.ws.rs.client.WebTarget;

public interface ParamAdder extends Function<WebTarget, WebTarget> {}
