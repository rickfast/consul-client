package com.orbitz.consul.model.catalog;

import org.junit.Test;

public class TaggedAddressesTest {

    @Test
    public void buildingTaggedAddressWithAllAttributesShouldSucceed() {
        ImmutableTaggedAddresses.builder()
                .lan("127.0.0.1")
                .wan("172.217.17.110")
                .build();
    }

    @Test
    public void buildingTaggedAddressWithoutLanAddressShouldSucceed() {
        ImmutableTaggedAddresses.builder()
                .wan("172.217.17.110")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildingTaggedAddressWithoutWanAddressShouldThrow() {
        ImmutableTaggedAddresses.builder()
                .lan("127.0.0.1")
                .build();
    }

}
