package com.orbitz.consul.model;

import com.google.common.base.Objects;

import java.math.BigInteger;

public class ConsulResponse<T> {

    private final T response;
    private final long lastContact;
    private final boolean knownLeader;
    private final BigInteger index;

    public ConsulResponse(T response, long lastContact, boolean knownLeader, BigInteger index) {
        this.response = response;
        this.lastContact = lastContact;
        this.knownLeader = knownLeader;
        this.index = index;
    }

    public T getResponse() {
        return response;
    }

    public long getLastContact() {
        return lastContact;
    }

    public boolean isKnownLeader() {
        return knownLeader;
    }

    public BigInteger getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "ConsulResponse{" +
                "response=" + response +
                ", lastContact=" + lastContact +
                ", knownLeader=" + knownLeader +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsulResponse that = (ConsulResponse) o;

        return Objects.equal(this.response, that.response) &&
                Objects.equal(this.lastContact, that.lastContact) &&
                Objects.equal(this.knownLeader, that.knownLeader) &&
                Objects.equal(this.index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(response, lastContact, knownLeader, index);
    }
}
