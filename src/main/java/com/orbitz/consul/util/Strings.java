package com.orbitz.consul.util;

public class Strings {

    public static String trimLeadingSlash(String value) {
        return value.replaceAll("^/+", "");
    }

    public static String unquote(String value) {
        return value.replaceAll("^\"|\"$", "");
    }
}
