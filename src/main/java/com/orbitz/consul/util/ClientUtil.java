package com.orbitz.consul.util;

import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.EventOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Map;

/**
 * A collection of stateless utility methods for use in constructing
 * requests and responses to the Consul HTTP API.
 */
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

    /**
     * Given a {@link com.orbitz.consul.option.QueryOptions} object, adds the
     * appropriate query string parameters to the request being built.
     *
     * @param webTarget The base {@link javax.ws.rs.client.WebTarget}.
     * @param queryOptions Query specific options to use.
     * @return A {@link javax.ws.rs.client.WebTarget} with all appropriate query
     *  string parameters.
     */
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

        if(queryOptions.hasToken()){
            webTarget = webTarget.queryParam("token",queryOptions.getToken());
        }

        return webTarget;
    }

    /**
     * Given a {@link com.orbitz.consul.option.EventOptions} object, adds the
     * appropriate query string parameters to the request being built.
     *
     * @param webTarget The base {@link javax.ws.rs.client.WebTarget}.
     * @param eventOptions Event specific options to use.
     * @return A {@link javax.ws.rs.client.WebTarget} with all appropriate query
     *  string parameters.
     */
    public static WebTarget eventConfig(WebTarget webTarget, EventOptions eventOptions) {
        if(StringUtils.isNotEmpty(eventOptions.getDatacenter())) {
            webTarget = webTarget.queryParam("dc", eventOptions.getDatacenter());
        }

        if(StringUtils.isNotEmpty(eventOptions.getNodeFilter())) {
            webTarget = webTarget.queryParam("node", eventOptions.getNodeFilter());
        }

        if(StringUtils.isNotEmpty(eventOptions.getServiceFilter())) {
            webTarget = webTarget.queryParam("service", eventOptions.getServiceFilter());
        }

        if(StringUtils.isNotEmpty(eventOptions.getTagFilter())) {
            webTarget = webTarget.queryParam("tag", eventOptions.getTagFilter());
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

    /**
     * Given a {@link com.orbitz.consul.option.CatalogOptions} object, adds the
     * appropriate query string parameters to the request being built.
     *
     * @param target The base {@link javax.ws.rs.client.WebTarget}.
     * @param catalogOptions Catalog specific options to use.
     * @return A {@link javax.ws.rs.client.WebTarget} with all appropriate query
     *  string parameters.
     */
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

    /**
     * Given a {@link javax.ws.rs.client.WebTarget} object and a type to marshall
     * the result JSON into, complete the HTTP GET request.
     *
     * @param webTarget The JAX-RS target.
     * @param responseType The class to marshall the JSON into.
     * @param <T> The class to marshall the JSON into.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} containing the result.
     */
    public static <T> ConsulResponse<T> response(WebTarget webTarget, GenericType<T> responseType) {
        Response response = webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        return consulResponse(responseType, response);
    }

    /**
     * Given a {@link javax.ws.rs.client.WebTarget} object and a type to marshall
     * the result JSON into, complete the HTTP GET request.
     *
     * @param webTarget The JAX-RS target.
     * @param responseType The class to marshall the JSON into.
     * @param callback The callback object to handle the result on a different thread.
     * @param <T> The class to marshall the JSON into.
     */
    public static <T> void response(WebTarget webTarget, final GenericType<T> responseType,
                                    final ConsulResponseCallback<T> callback) {
        webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<Response>() {

            @Override
            public void completed(Response response) {
                try {
                    callback.onComplete(consulResponse(responseType, response));
                } catch (Exception ex) {
                    callback.onFailure(ex);
                }
            }

            @Override
            public void failed(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    /**
     * Extracts Consul specific headers and adds them to a {@link com.orbitz.consul.model.ConsulResponse}
     * object, which also contains the returned JSON entity.
     *
     * @param responseType The class to marshall the JSON to.
     * @param response The HTTP response.
     * @param <T> The class to marshall the JSON to.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} object.
     */
    private static <T> ConsulResponse<T> consulResponse(GenericType<T> responseType, Response response) {
        handleErrors(response);

        String indexHeaderValue = response.getHeaderString("X-Consul-Index");
        String lastContactHeaderValue = response.getHeaderString("X-Consul-Lastcontact");
        String knownLeaderHeaderValue = response.getHeaderString("X-Consul-Knownleader");

        BigInteger index = new BigInteger(indexHeaderValue);
        long lastContact = lastContactHeaderValue == null ? -1 : Long.valueOf(lastContactHeaderValue);
        boolean knownLeader = knownLeaderHeaderValue == null ? false : Boolean.valueOf(knownLeaderHeaderValue);

        ConsulResponse<T> consulResponse = new ConsulResponse<T>(response.readEntity(responseType), lastContact, knownLeader, index);

        response.close();

        return consulResponse;
    }

    /**
     * Decodes a Base 64 encoded string.
     *
     * @param value The encoded string.
     * @return The decoded string.
     */
    public static String decodeBase64(String value) {
        return new String(Base64.decodeBase64(value));
    }

    /**
     * Since Consul returns plain text when an error occurs, check for
     * unsuccessful HTTP status code, and throw an exception with the text
     * from Consul as the message.
     *
     * @param response The HTTP response.
     */
    public static void handleErrors(Response response) {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            ServerErrorException see = null;

            if (response.hasEntity()) {
                // Consul sends back error information in the response body
                String message = response.readEntity(String.class);
                see = new ServerErrorException(message, response);
            } else {
                see = new ServerErrorException(response);
            }
            response.close();
            throw new ConsulException(see.getLocalizedMessage(), see);
        }
    }
}
