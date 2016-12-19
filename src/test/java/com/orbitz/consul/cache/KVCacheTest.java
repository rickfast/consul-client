package com.orbitz.consul.cache;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.orbitz.consul.model.kv.ImmutableValue;
import com.orbitz.consul.model.kv.Value;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class KVCacheTest {

    @Test
    @Parameters(method = "getKeyValueTestValues")
    @TestCaseName("wanted {0}, found {1}")
    public void checkKeyExtractor(String rootPath, String input, String expected) {
        //Called in the constructor of the cache, must be use in the test as it may modify rootPath value.
        final String keyPath = KVCache.prepareRootPath(rootPath);

        Function<Value, String> keyExtractor = KVCache.getKeyExtractorFunction(keyPath);
        Assert.assertEquals(expected, keyExtractor.apply(createValue(input)));
    }

    public Object getKeyValueTestValues() {
        return new Object[]{
                new Object[]{"", "a/b", "a/b"},
                new Object[]{"/", "a/b", "a/b"},
                new Object[]{"a", "a/b", "a/b"},
                new Object[]{"a/", "a/b", "b"},
                new Object[]{"a/b", "a/b", ""},
                new Object[]{"a/b", "a/b/", "b/"},
                new Object[]{"a/b", "a/b/c", "b/c"},
                new Object[]{"a/b", "a/bc", "bc"},
                new Object[]{"a/b/", "a/b/", ""},
                new Object[]{"a/b/", "a/b/c", "c"},
                new Object[]{"/a/b", "a/b", ""}

        };
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