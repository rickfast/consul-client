package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.orbitz.consul.model.kv.ImmutableValue;
import com.orbitz.consul.model.kv.Value;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KVCacheTest {

    private Function<Value, String> keyExtractor;

    @Before
    public void setUp() {
        keyExtractor = KVCache.getKeyExtractorFunction("a/b");
    }

    @Test
    public void testSameRootPathAndKeyNoSlash() {
        String actualKey = keyExtractor.apply(createValue("a/b"));
        Assert.assertEquals("", actualKey);
    }

    @Test
    public void testSameRootPathAndKeyWithSlash() {
        String actualKey = keyExtractor.apply(createValue("a/b/"));
        Assert.assertEquals("", actualKey);
    }

    @Test
    public void testRootPathHasSubkeysNoSlash() {
        String actualKey = keyExtractor.apply(createValue("a/b/c"));
        Assert.assertEquals("c", actualKey);
    }

    @Test
    public void testRootPathHasSubkeysWithSlash() {
        String actualKey = keyExtractor.apply(createValue("a/b/c/d/"));
        Assert.assertEquals("c/d/", actualKey);
    }

    private Value createValue(final String key) {
        return ImmutableValue.builder()
                .createIndex(1234567890)
                .modifyIndex(1234567890)
                .lockIndex(1234567890)
                .flags(1234567890)
                .key(key)
                .value(Optional.<String>absent())
                .build();
    }
}