package ru.hh.consul.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class TrustManagerUtilsTest {
    @Test
    public void shouldTrustManagerReturnCorrectResult() {
        assertNotNull(TrustManagerUtils.getDefaultTrustManager());
    }
}
