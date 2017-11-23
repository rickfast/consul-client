package com.orbitz.consul.model.agent;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CheckTest {

    @Test(expected = IllegalStateException.class)
    public void testCheckType() throws Exception {

        ImmutableCheck.builder()
                .id("id")
                .interval("10s")
                .name("name")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCheckIntervalHttp() throws Exception {

        ImmutableCheck.builder()
                .id("id")
                .http("http://foo.local:1337/health")
                .name("name")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCheckIntervalScript() throws Exception {

        ImmutableCheck.builder()
                .id("id")
                .script("/bin/echo \"hi\"")
                .name("name")
                .build();
    }

    @Test
    public void serviceTagsAreNotNullWhenNotSpecified() {
        Check check = ImmutableCheck.builder()
                .name("name")
                .id("id")
                .build();

        assertEquals(Collections.emptyList(), check.getServiceTags());
    }

    @Test
    public void serviceTagsCanBeAddedToCheck() {
        Check check = ImmutableCheck.builder()
                .name("name")
                .id("id")
                .addServiceTags("myTag")
                .build();

        assertEquals(Collections.singletonList("myTag"), check.getServiceTags());
    }
}
