package com.orbitz.consul.util;

import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.client.WebTarget;
import java.util.Map;

public class ClientUtil {

    public static WebTarget queryParams(WebTarget webTarget, Map<String, String> params) {
        WebTarget target = webTarget;

        for(Map.Entry<String, String> entry : params.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }

        return target;
    }

    public static String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value));
    }
}
