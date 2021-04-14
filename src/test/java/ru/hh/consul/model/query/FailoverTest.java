package ru.hh.consul.model.query;

import java.util.List;
import org.junit.Test;

public class FailoverTest {

    @Test
    public void creatingFailoverWithDatacentersIsValid() {
        ImmutableFailover.builder()
                .datacenters(List.of("dc1", "dc2"))
                .build();
    }

    @Test
    public void creatingFailoverWithNearestIsValid() {
        ImmutableFailover.builder()
                .nearestN(2)
                .build();
    }

    @Test
    public void creatingFailoverWithNearestAndDatacentersIsValid() {
        ImmutableFailover.builder()
                .datacenters(List.of("dc1", "dc2"))
                .nearestN(2)
                .build();
    }
}
