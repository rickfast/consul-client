package com.orbitz.consul.model.agent;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CheckTest {

    @Test(expected = IllegalStateException.class)
    public void buildingCheckThrowsIfMissingMethod() {
        ImmutableCheck.builder()
                .id("id")
                .interval("10s")
                .name("name")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildingCheckWithHttpThrowsIfMissingInterval() {
        ImmutableCheck.builder()
                .id("id")
                .http("http://foo.local:1337/health")
                .name("name")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildingCheckWithGrpcThrowsIfMissingInterval() {
        ImmutableCheck.builder()
                .id("id")
                .grpc("localhost:12345")
                .name("name")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildingCheckWithArgsThrowsIfMissingInterval() {
        ImmutableCheck.builder()
                .id("id")
                .args(Collections.singletonList("/bin/echo \"hi\""))
                .name("name")
                .build();
    }

    @Test
    public void severalArgsCanBeAddedToCheck() {
        Check check = ImmutableCheck.builder()
                .id("id")
                .args(Lists.newArrayList("/bin/echo \"hi\"", "/bin/echo \"hello\""))
                .interval("1s")
                .name("name")
                .build();

        assertTrue("Args should be present in check", check.getArgs().isPresent());
        assertEquals("Check should contain 2 args", 2, check.getArgs().get().size());
    }

    @Test
    public void serviceTagsAreNotNullWhenNotSpecified() {
        Check check = ImmutableCheck.builder()
                .ttl("")
                .name("name")
                .id("id")
                .build();

        assertEquals(Collections.emptyList(), check.getServiceTags());
    }

    @Test
    public void serviceTagsCanBeAddedToCheck() {
        Check check = ImmutableCheck.builder()
                .ttl("")
                .name("name")
                .id("id")
                .addServiceTags("myTag")
                .build();

        assertEquals(Collections.singletonList("myTag"), check.getServiceTags());
    }
}
