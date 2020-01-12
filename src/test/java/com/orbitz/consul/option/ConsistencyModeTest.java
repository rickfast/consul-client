package com.orbitz.consul.option;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

public class ConsistencyModeTest {

    @Test
    public void checkCompatinbilityWithOldEnum(){
        assertEquals(ConsistencyMode.values().length, 3);
        for (int i = 0; i < ConsistencyMode.values().length; i++) {
            assertEquals(ConsistencyMode.values()[i].ordinal(), i);
        }
        assertEquals(ConsistencyMode.values()[0], ConsistencyMode.DEFAULT);
        assertEquals(ConsistencyMode.values()[0].name(), "DEFAULT");
        assertEquals(ConsistencyMode.values()[1], ConsistencyMode.STALE);
        assertEquals(ConsistencyMode.values()[1].name(), "STALE");
        assertEquals(ConsistencyMode.values()[2], ConsistencyMode.CONSISTENT);
        assertEquals(ConsistencyMode.values()[2].name(), "CONSISTENT");
    }

    @Test
    public void checkHeadersForCached() {
        ConsistencyMode consistency = ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.of(Long.valueOf(30)), Optional.of(60L));
        assertEquals("cached", consistency.toParam().get());
        assertEquals(1, consistency.getAdditionalHeaders().size());
        assertEquals("max-age=30,stale-if-error=60", consistency.getAdditionalHeaders().get("Cache-Control"));

        consistency = ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.of(30L), Optional.empty());
        assertEquals("max-age=30", consistency.getAdditionalHeaders().get("Cache-Control"));

        consistency = ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.empty(), Optional.of(60L));
        assertEquals("stale-if-error=60", consistency.getAdditionalHeaders().get("Cache-Control"));

        // Consistency cache without Cache-Control directives
        consistency = ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.empty(), Optional.empty());
        assertEquals("cached", consistency.toParam().get());
        assertEquals(0, consistency.getAdditionalHeaders().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkBadMaxAge() {
        ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.of(-1L), Optional.empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkBadMaxStaleError() {
        ConsistencyMode.createCachedConsistencyWithMaxAgeAndStale(Optional.empty(), Optional.of(-2L));
    }
}