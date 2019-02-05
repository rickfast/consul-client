package com.orbitz.consul;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.async.EventResponseCallback;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.EventResponse;
import com.orbitz.consul.model.ImmutableEventResponse;
import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.EventOptions;
import com.orbitz.consul.option.QueryOptions;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HTTP Client for /v1/event/ endpoints.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#event">The Consul API Docs</a>
 */
public class EventClient extends BaseClient {

    private static String CLIENT_NAME = "event";

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    EventClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
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
        return http.extract(api.fireEvent(name,
                RequestBody.create(MediaType.parse("text/plain"), payload),
                eventOptions.toQuery()));
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
        return fireEvent(name, EventOptions.BLANK);
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
        return http.extract(api.fireEvent(name, eventOptions.toQuery()));
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
        Map<String, String> query = StringUtils.isNotEmpty(name) ? ImmutableMap.of("name", name) : Collections.emptyMap();

        ConsulResponse<List<Event>> response = http.extractConsulResponse(api.listEvents(query));
        return ImmutableEventResponse.of(response.getResponse(), response.getIndex());
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
        Map<String, String> query = StringUtils.isNotEmpty(name) ? ImmutableMap.of("name", name) : Collections.emptyMap();

        http.extractConsulResponse(api.listEvents(query), createConsulResponseCallbackWrapper(callback));
    }

    private ConsulResponseCallback<List<Event>> createConsulResponseCallbackWrapper(EventResponseCallback callback) {
        return new ConsulResponseCallback<List<Event>>() {
            @Override
            public void onComplete(ConsulResponse<List<Event>> response) {
                callback.onComplete(ImmutableEventResponse.of(response.getResponse(), response.getIndex()));
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        };
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
    /**
     * Retrofit API interface.
     */
    interface Api {

        @PUT("event/fire/{name}")
        Call<Event> fireEvent(@Path("name") String name,
                              @Body RequestBody payload,
                              @QueryMap Map<String, Object> query);

        @PUT("event/fire/{name}")
        Call<Event> fireEvent(@Path("name") String name,
                              @QueryMap Map<String, Object> query);

        @GET("event/list")
        Call<List<Event>> listEvents(@QueryMap Map<String, String> query);
    }
}
