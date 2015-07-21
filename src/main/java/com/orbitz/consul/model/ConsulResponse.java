package com.orbitz.consul.model;

import java.math.BigInteger;

public class ConsulResponse<T> {

    private T response;
    private long lastContact;
    private boolean knownLeader;
    private BigInteger index;

    public ConsulResponse(T response, long lastContact, boolean knownLeader, BigInteger index) {
        this.response = response;
        this.lastContact = lastContact;
        this.knownLeader = knownLeader;
        this.index = index;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public long getLastContact() {
        return lastContact;
    }

    public void setLastContact(long lastContact) {
        this.lastContact = lastContact;
    }

    public boolean isKnownLeader() {
        return knownLeader;
    }

    public void setKnownLeader(boolean knownLeader) {
        this.knownLeader = knownLeader;
    }

    public BigInteger getIndex() {
        return index;
    }

    public void setIndex(BigInteger index) {
        this.index = index;
    }
}
