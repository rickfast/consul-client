package ru.hh.consul.util;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class TrustManagerUtilsTest {
    @Test
    public void shouldTrustManagerReturnCorrectResult() {
        assertNotNull(TrustManagerUtils.getDefaultTrustManager());
    }
}
