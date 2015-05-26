package com.orbitz.consul.model;

import com.orbitz.consul.model.event.Event;

import java.math.BigInteger;
import java.util.List;

public class EventResponse {

    private List<Event> events;
    private BigInteger index;

    public EventResponse(List<Event> events, BigInteger index) {
        this.events = events;
        this.index = index;
    }

    public List<Event> getEvents() {
        return events;
    }

    public BigInteger getIndex() {
        return index;
    }
}
