package com.orbitz.consul.util;

import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ClientUtil {

    /**
     * Applies all key/values from the params map to query string parameters.
     *
     * @param webTarget The JAX-RS target to apply the query parameters.
     * @param params Map of parameters.
     * @return The new target with the parameters applied.
     */
    public static WebTarget queryParams(WebTarget webTarget, Map<String, String> params) {
        WebTarget target = webTarget;

        if(params != null) {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                target = target.queryParam(entry.getKey(), entry.getValue());
            }
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
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @param type The generic type to marshall the resulting data to.
     * @param <T> The result type.
     * @return A {@link com.orbitz.consul.model.ConsulResponse}.
     */
    public static <T> ConsulResponse<T> response(WebTarget target, CatalogOptions catalogOptions,
                                                 QueryOptions queryOptions,
                                                 GenericType<T> type) {
        target = catalogConfig(target, catalogOptions);
        target = queryConfig(target, queryOptions);

        return response(target, type);
    }

    /**
     * Generates a {@link com.orbitz.consul.model.ConsulResponse} for a specific datacenter,
     * set of {@link com.orbitz.consul.option.QueryOptions}, and a result type.
     *
     * @param target The base {@link javax.ws.rs.client.WebTarget}.
     * @param catalogOptions Catalog specific options to use.
     * @param queryOptions The Query Options to use.
     * @param type The generic type to marshall the resulting data to.
     * @param <T> The result type.
     */
    public static <T> void response(WebTarget target, CatalogOptions catalogOptions,
                                                 QueryOptions queryOptions,
                                                 GenericType<T> type,
                                                 ConsulResponseCallback<T> callback) {

        target = catalogConfig(target, catalogOptions);
        target = queryConfig(target, queryOptions);

        response(target, type, callback);
    }

    private static WebTarget catalogConfig(WebTarget target, CatalogOptions catalogOptions) {
        if(catalogOptions != null) {
            if (!StringUtils.isEmpty(catalogOptions.getDatacenter())) {
                target = target.queryParam("dc", catalogOptions.getDatacenter());
            }

            if (!StringUtils.isEmpty(catalogOptions.getTag())) {
                target = target.queryParam("tag", catalogOptions.getTag());
            }
        }
        return target;
    }



    public static <T> ConsulResponse<T> response(WebTarget webTarget, GenericType<T> responseType) {
        Response response = webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        return consulResponse(responseType, response);
    }

    public static <T> void response(WebTarget webTarget, final GenericType<T> responseType,
                                    final ConsulResponseCallback<T> callback) {
        webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<Response>() {

            @Override
            public void completed(Response response) {
                callback.onComplete(consulResponse(responseType, response));
            }

            @Override
            public void failed(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    private static <T> ConsulResponse<T> consulResponse(GenericType<T> responseType, Response response) {

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            ServerErrorException see = null;

            if (response.hasEntity()) {
                // Consul sends back error information in the response body
                String message = response.readEntity(String.class);
                sse = new ServerErrorException(message, response);
            } else {
                sse = new ServerErrorException(response);
            }
            response.close();
            throw new ConsulException(see.getLocalizedMessage(), see);
        }

        int index = Integer.valueOf(response.getHeaderString("X-Consul-Index"));
        long lastContact = Long.valueOf(response.getHeaderString("X-Consul-Lastcontact"));
        boolean knownLeader = Boolean.valueOf(response.getHeaderString("X-Consul-Knownleader"));
        ConsulResponse<T> consulResponse = new ConsulResponse<T>(response.readEntity(responseType), lastContact, knownLeader, index);

        response.close();

        return consulResponse;
    }

    public static String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value));
    }
}
