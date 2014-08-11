package com.orbitz.consul.util;

import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.common.util.StringUtils;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ClientUtil {

    public static WebTarget queryParams(WebTarget webTarget, Map<String, String> params) {
        WebTarget target = webTarget;

        for(Map.Entry<String, String> entry : params.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }

        return target;
    }

    public static WebTarget queryConfig(WebTarget webTarget, QueryOptions queryOptions) {
        if(queryOptions.isBlocking()) {
            webTarget = webTarget.queryParam("wait", queryOptions.getWait())
                    .queryParam("index", String.valueOf(queryOptions.getIndex()));
        }

        if(queryOptions.getConsistencyMode() == ConsistencyMode.CONSISTENT) {
            webTarget = webTarget.queryParam("consistent");
        }

        if(queryOptions.getConsistencyMode() == ConsistencyMode.STALE) {
            webTarget = webTarget.queryParam("stale");
        }

        return webTarget;
    }

    /**
     * Generates a {@link com.orbitz.consul.model.ConsulResponse} for a specific datacenter,
     * set of {@link com.orbitz.consul.option.QueryOptions}, and a result type.
     *
     * @param target The base {@link javax.ws.rs.client.WebTarget}.
     * @param datacenter The datacenter to query.
     * @param queryOptions The Query Options to use.
     * @param type The generic type to marshall the resulting data to.
     * @param <T> The result type.
     * @return A {@link com.orbitz.consul.model.ConsulResponse}.
     */
    public static <T> ConsulResponse<T> response(WebTarget target, String datacenter, QueryOptions queryOptions,
                                           GenericType<T> type) {
        if(!StringUtils.isEmpty(datacenter)) {
            target = target.queryParam("dc", datacenter);
        }

        target = queryConfig(target, queryOptions);

        return response(target, type);
    }

    public static <T> ConsulResponse<T> response(WebTarget webTarget, GenericType<T> responseType) {
        Response response = webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        int index = Integer.valueOf(response.getHeaderString("X-Consul-Index"));
        long lastContact = Long.valueOf(response.getHeaderString("X-Consul-Lastcontact"));
        boolean knownLeader = Boolean.valueOf(response.getHeaderString("X-Consul-Knownleader"));

        return new ConsulResponse<T>(response.readEntity(responseType), lastContact, knownLeader, index);
    }

    public static String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value));
    }
}
