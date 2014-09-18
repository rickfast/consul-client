package com.orbitz.consul.util;

import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Collections;
import java.util.HashMap;

import static com.orbitz.consul.util.ClientUtil.queryParams;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClientUtilTests {

    @Test
    public void testQueryParams_none() {
        WebTarget target = ClientBuilder.newClient().target("http://localhost/");

        queryParams(target, Collections.<String, String>emptyMap());

        assertNull(target.getUri().getQuery());
    }

    @Test
    public void testQueryParams_null() {
        WebTarget target = ClientBuilder.newClient().target("http://localhost/");

        queryParams(target, null);

        assertNull(target.getUri().getQuery());
    }

    @Test
    public void testQueryParams() {
        WebTarget target = ClientBuilder.newClient().target("http://localhost/");

        target = queryParams(target, new HashMap<String, String>() {
            {
                put("hi", "bye");
            }
        });

        assertEquals("hi=bye", target.getUri().getQuery());
    }
}
