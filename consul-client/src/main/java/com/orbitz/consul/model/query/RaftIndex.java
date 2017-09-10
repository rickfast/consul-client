package com.orbitz.consul.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@org.immutables.value.Value.Immutable
@JsonDeserialize(as = ImmutableRaftIndex.class)
@JsonSerialize(as = ImmutableRaftIndex.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RaftIndex {
}
