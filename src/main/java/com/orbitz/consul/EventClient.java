package com.orbitz.consul;

import com.orbitz.consul.async.EventResponseCallback;
import com.orbitz.consul.model.EventResponse;
import com.orbitz.consul.model.ImmutableEventResponse;
import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.option.EventOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.List;

import static com.orbitz.consul.util.ClientUtil.addParams;
import static com.orbitz.consul.util.ClientUtil.handleErrors;

/**
 * HTTP Client for /v1/event/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#event">The Consul API Docs</a>
 */
public class EventClient {

    private static final GenericType<Event> TYPE_EVENT =
            new GenericType<Event>() {};
    private static final GenericType<List<Event>> TYPE_EVENT_LIST =
            new GenericType<List<Event>>() {};

    private final WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    EventClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Fires a Consul event.
     *
     * PUT /v1/event/fire/{name}
     *
     * @param name The name of the event.
     * @param eventOptions The event specific options to use.
     * @param payload Optional string payload.
     * @return The newly created {@link com.orbitz.consul.model.event.Event}.
     */
    public Event fireEvent(String name, EventOptions eventOptions, String payload) {
        WebTarget target = webTarget.path("fire").path(name);

        target = addParams(target, eventOptions);

        return target.request()
                .put(Entity.entity(StringUtils.isEmpty(payload) ? "" : payload, MediaType.WILDCARD_TYPE),
                        TYPE_EVENT);
    }

    /**
     * Fires a Consul event.
     *
     * PUT /v1/event/fire/{name}
     *
     * @param name The name of the event.
     * @return The newly created {@link com.orbitz.consul.model.event.Event}.
     */
    public Event fireEvent(String name) {
        return fireEvent(name, EventOptions.BLANK, null);
    }

    /**
     * Fires a Consul event.
     *
     * PUT /v1/event/fire/{name}
     *
     * @param name The name of the event.
     * @param eventOptions The event specific options to use.
     * @return The newly created {@link com.orbitz.consul.model.event.Event}.
     */
    public Event fireEvent(String name, EventOptions eventOptions) {
        return fireEvent(name, eventOptions, null);
    }

    /**
     * Fires a Consul event.
     *
     * PUT /v1/event/fire/{name}
     *
     * @param name The name of the event.
     * @param payload Optional string payload.
     * @return The newly created {@link com.orbitz.consul.model.event.Event}.
     */
    public Event fireEvent(String name, String payload) {
        return fireEvent(name, EventOptions.BLANK, payload);
    }

    /**
     * Lists events for the Consul agent.
     *
     * GET /v1/event/list?name={name}
     *
     * @param name Event name to filter.
     * @param queryOptions The query options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} object containing
     *  a list of {@link com.orbitz.consul.model.event.Event} objects.
     */
    public EventResponse listEvents(String name, QueryOptions queryOptions) {
        WebTarget target = webTarget.path("list");

        if (StringUtils.isNotEmpty(name)) {
            target.queryParam("name", name);
        }

        return response(target, queryOptions);
    }

    /**
     * Lists events for the Consul agent.
     *
     * GET /v1/event/list?name={name}
     *
     * @param name Event name to filter.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} object containing
     *  a list of {@link com.orbitz.consul.model.event.Event} objects.
     */
    public EventResponse listEvents(String name) {
        return listEvents(name, QueryOptions.BLANK);
    }

    /**
     * Lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @param queryOptions The query options to use.
     * @return A {@link com.orbitz.consul.model.ConsulResponse} object containing
     *  a list of {@link com.orbitz.consul.model.event.Event} objects.
     */
    public EventResponse listEvents(QueryOptions queryOptions) {
        return listEvents(null, queryOptions);
    }

    /**
     * Lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @return A {@link com.orbitz.consul.model.ConsulResponse} object containing
     *  a list of {@link com.orbitz.consul.model.event.Event} objects.
     */
    public EventResponse listEvents() {
        return listEvents(null, QueryOptions.BLANK);
    }

    /**
     * Asynchronously lists events for the Consul agent.
     *
     * GET /v1/event/list?name={name}
     *
     * @param name Event name to filter.
     * @param queryOptions The query options to use.
     * @param callback The callback to asynchronously process the result.
     */
    public void listEvents(String name, QueryOptions queryOptions, EventResponseCallback callback) {
        WebTarget target = webTarget.path("list");

        if (StringUtils.isNotEmpty(name)) {
            target.queryParam("name", name);
        }

        response(target, queryOptions, callback);
    }

    /**
     * Asynchronously lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @param queryOptions The query options to use.
     * @param callback The callback to asynchronously process the result.
     */
    public void listEvents(QueryOptions queryOptions, EventResponseCallback callback) {
        listEvents(null, queryOptions, callback);
    }

    /**
     * Asynchronously lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @param callback The callback to asynchronously process the result.
     */
    public void listEvents(EventResponseCallback callback) {
        listEvents(null, QueryOptions.BLANK, callback);
    }

    private static void response(WebTarget target, QueryOptions queryOptions, final EventResponseCallback callback) {
        target = addParams(target, queryOptions);

        target.request().accept(MediaType.APPLICATION_JSON_TYPE).async().get(new InvocationCallback<Response>() {

            @Override
            public void completed(Response response) {
                try {
                    callback.onComplete(eventResponse(response));
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

    private static EventResponse response(WebTarget target, QueryOptions queryOptions) {
        target = addParams(target, queryOptions);

        return eventResponse(target.request().accept(MediaType.APPLICATION_JSON_TYPE).get());
    }

    private static EventResponse eventResponse(Response response) {
        handleErrors(response);

        String indexHeaderValue = response.getHeaderString("X-Consul-Index");

        BigInteger index = new BigInteger(indexHeaderValue);

        EventResponse eventResponse = ImmutableEventResponse.of(response.readEntity(TYPE_EVENT_LIST), index);
        
        response.close();

        return eventResponse;
    }
}
