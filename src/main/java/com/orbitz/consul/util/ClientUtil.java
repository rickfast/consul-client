package com.orbitz.consul.util;

import javax.ws.rs.client.WebTarget;
import java.util.Iterator;
import java.util.Map;

public class ClientUtil {

    public static WebTarget queryParams(WebTarget webTarget, Map<String, String> params) {
        return queryParam(webTarget, params.entrySet().iterator());
    }

    private static WebTarget queryParam(WebTarget webTarget, Iterator<Map.Entry<String, String>> iterator) {
        if(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            WebTarget nextTarget = queryParam(webTarget, iterator);

            return nextTarget.queryParam(entry.getKey(), entry.getValue());
        } else {
            return webTarget;
        }
    }
}
