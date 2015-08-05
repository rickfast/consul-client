package com.orbitz.consul.util;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ParamAdder;
import com.orbitz.consul.option.QueryOptions;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.List;
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
     * Given a {@link com.orbitz.consul.option.ParamAdder} object, adds the
     * appropriate query string parameters to the request being built.
     *
     * @param webTarget The base {@link javax.ws.rs.client.WebTarget}.
     * @param  paramAdder will add specific params to the target.
     * @return A {@link javax.ws.rs.client.WebTarget} with all appropriate query
     *  string parameters.
     */
    public static WebTarget addParams(WebTarget webTarget, ParamAdder paramAdder) {
        return paramAdder == null ? webTarget : paramAdder.apply(webTarget);
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
        target = addParams(target, catalogOptions);
        target = addParams(target, queryOptions);

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

        target = addParams(target, catalogOptions);
        target = addParams(target, queryOptions);

        response(target, type, callback);
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

        ConsulResponse<T> consulResponse = new ConsulResponse<T>(readResponse(response, responseType), lastContact, knownLeader, index);

        response.close();

        return consulResponse;
    }

    /**
     * Converts a {@link Response} object to the generic type provided, or an empty
     * representation if appropriate
     *
     * @param response response
     * @param responseType response type
     * @param <T>
     * @return the re
     */
    private static <T> T readResponse(Response response, GenericType<T> responseType) {
        if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            // would be nice I knew a better way to do this
            if (responseType.getRawType() == List.class) {
                return (T) ImmutableList.of();
            } else if (responseType.getRawType() == Optional.class) {
                return (T) Optional.absent();
            } else if(responseType.getRawType() == Map.class) {
                return (T) ImmutableMap.of();
            } else {
                // Not sure if this case will be reached, but if it is it'll be nice to know
                throw new IllegalStateException("Cannot determine empty representation for " + responseType.getRawType());
            }
        }
        return response.readEntity(responseType);
    }

    /**
     * Since Consul returns plain text when an error occurs, check for
     * unsuccessful HTTP status code, and throw an exception with the text
     * from Consul as the message.
     *
     * @param response The HTTP response.
     */
    public static void handleErrors(Response response) {

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                || response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            // not an error
            return;
        }

        try {
            final String message = response.hasEntity() ? response.readEntity(String.class) : null;
            if (response.getStatusInfo().getFamily() ==  Response.Status.Family.SERVER_ERROR) {
                throw new ServerErrorException(message, response);
            } else {
                throw new WebApplicationException(message, response);
            }
        } catch (Exception e) {
            throw new ConsulException(e.getLocalizedMessage(), e);
        } finally {
            response.close();
        }
    }
}
