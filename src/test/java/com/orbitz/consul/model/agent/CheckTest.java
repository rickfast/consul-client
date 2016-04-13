package com.orbitz.consul.model.agent;

import org.junit.Test;

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
}