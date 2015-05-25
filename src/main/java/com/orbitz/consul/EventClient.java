package com.orbitz.consul;

import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.EventOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.orbitz.consul.util.ClientUtil.eventConfig;
import static com.orbitz.consul.util.ClientUtil.response;

/**
 * HTTP Client for /v1/event/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#event">The Consul API Docs</a>
 */
public class EventClient {

    private WebTarget webTarget;

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

        target = eventConfig(target, eventOptions);

        return target.request()
                .put(Entity.entity(StringUtils.isEmpty(payload) ? "" : payload, MediaType.WILDCARD_TYPE),
                        new GenericType<Event>() {});
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
    public ConsulResponse<List<Event>> listEvents(String name, QueryOptions queryOptions) {
        WebTarget target = webTarget.path("list");

        if (StringUtils.isNotEmpty(name)) {
            target.queryParam("name", name);
        }

        return response(target, CatalogOptions.BLANK, queryOptions, new GenericType<List<Event>>() {
        });
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
    public ConsulResponse<List<Event>> listEvents(String name) {
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
    public ConsulResponse<List<Event>> listEvents(QueryOptions queryOptions) {
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
    public ConsulResponse<List<Event>> listEvents() {
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
    public void listEvents(String name, QueryOptions queryOptions, ConsulResponseCallback<List<Event>> callback) {
        WebTarget target = webTarget.path("list");

        if (StringUtils.isNotEmpty(name)) {
            target.queryParam("name", name);
        }

        response(target, CatalogOptions.BLANK, queryOptions, new GenericType<List<Event>>() {}, callback);
    }

    /**
     * Asynchronously lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @param queryOptions The query options to use.
     * @param callback The callback to asynchronously process the result.
     */
    public void listEvents(QueryOptions queryOptions, ConsulResponseCallback<List<Event>> callback) {
        listEvents(null, queryOptions, callback);
    }

    /**
     * Asynchronously lists events for the Consul agent.
     *
     * GET /v1/event/list
     *
     * @param callback The callback to asynchronously process the result.
     */
    public void listEvents(ConsulResponseCallback<List<Event>> callback) {
        listEvents(null, QueryOptions.BLANK, callback);
    }
}
