package com.orbitz.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public abstract class AclToken {

    @JsonProperty("ID")
    public abstract Optional<String> id();

    @JsonProperty("Name")
    public abstract Optional<String> name();

    @JsonProperty("Type")
    public abstract Optional<String> type();

    @JsonProperty("Rules")
    public abstract Optional<String> rules();
}
