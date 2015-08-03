package com.orbitz.consul.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orbitz.consul.model.event.Event;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableEventResponse.class)
@JsonDeserialize(as = ImmutableEventResponse.class)
public abstract class EventResponse {

    @Value.Parameter
    public abstract List<Event> getEvents();
    @Value.Parameter
    public abstract BigInteger getIndex();

}
