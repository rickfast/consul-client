package com.orbitz.consul;

import com.google.common.base.Optional;
import com.google.common.primitives.UnsignedLongs;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.SessionInfo;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutablePutOptions;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.orbitz.consul.util.ClientUtil.addParams;
import static com.orbitz.consul.util.ClientUtil.response;

/**
 * HTTP Client for /v1/kv/ endpoints.
 */
public class KeyValueClient {

    private static final GenericType<List<Value>> TYPE_VALUE_LIST =
            new GenericType<List<Value>>() {};
    private static final Entity<String> EMPTY_ENTITY = Entity.text("");

    private final WebTarget webTarget;

    /**
     * Constructs an instance of this class.
     *
     * @param webTarget The {@link javax.ws.rs.client.WebTarget} to base requests from.
     */
    KeyValueClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value or {@link Optional#absent()}
     */
    public Optional<Value> getValue(String key) {
        return getValue(key, QueryOptions.BLANK);
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @return An {@link Optional} containing the value or {@link Optional#absent()}
     */
    public Optional<Value> getValue(String key, QueryOptions queryOptions) {
        WebTarget target = addParams(webTarget.path(key), queryOptions);
        try {
            return getSingleValue(target.request().accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<Value>>() {}));
        } catch (NotFoundException ignored) {}

        return Optional.absent();
    }

    /**
     * Asynchronously retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @param callback Callback implemented by callee to handle results.
     */
    public void getValue(String key, QueryOptions queryOptions, final ConsulResponseCallback<Optional<Value>> callback) {
        ConsulResponseCallback<List<Value>> wrapper = new ConsulResponseCallback<List<Value>>() {
            @Override
            public void onComplete(ConsulResponse<List<Value>> consulResponse) {
                callback.onComplete(
                        new ConsulResponse<Optional<Value>>(getSingleValue(consulResponse.getResponse()),
                                consulResponse.getLastContact(),
                                consulResponse.isKnownLeader(), consulResponse.getIndex()));
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        };
        response(webTarget.path(key), CatalogOptions.BLANK, queryOptions,
                TYPE_VALUE_LIST, wrapper);
    }

    private Optional<Value> getSingleValue(List<Value> values){
        return values != null && values.size() != 0 ? Optional.of(values.get(0)) : Optional.<Value>absent();
    }

    /**
     * Retrieves a list of {@link com.orbitz.consul.model.kv.Value} objects for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many {@link com.orbitz.consul.model.kv.Value} objects.
     */
    public List<Value> getValues(String key) {
        WebTarget target = webTarget.path(key).queryParam("recurse", "true");

        return Arrays.asList(target
                .request().accept(MediaType.APPLICATION_JSON_TYPE).get(Value[].class));
    }

    /**
     * Asynchronously retrieves a list of {@link com.orbitz.consul.model.kv.Value} objects for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @param callback Callback implemented by callee to handle results.
     */
    public void getValues(String key, QueryOptions queryOptions, ConsulResponseCallback<List<Value>> callback) {
        response(webTarget.path(key).queryParam("recurse", "true"), CatalogOptions.BLANK,
                queryOptions, TYPE_VALUE_LIST, callback);
    }

    /**
     * Retrieves a string value for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value as a string or
     * {@link Optional#absent()}
     */
    public Optional<String> getValueAsString(String key) {
        for (Value v: getValue(key).asSet()) {
            return v.getValueAsString();
        }
        return Optional.absent();
    }

    /**
     * Retrieves a list of string values for a specific key from the key/value
     * store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many string values.
     */
    public List<String> getValuesAsString(String key) {
        List<String> result = new ArrayList<String>();

        for(Value value : getValues(key)) {
            if (value.getValueAsString().isPresent()) {
                result.add(value.getValueAsString().get());
            }
        }

        return result;
    }

    /**
     * Puts a null value into the key/value store.
     *
     * @param key The key to use as index.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key) {
        return putValue(key, null, 0L, PutOptions.BLANK);
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value) {
        return putValue(key, value, 0L, PutOptions.BLANK);
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param flags The flags for this key.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, long flags) {
        return putValue(key, value, flags, PutOptions.BLANK);
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param putOptions PUT options (e.g. wait, acquire).
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, long flags, PutOptions putOptions) {

        checkArgument(StringUtils.isNotEmpty(key), "Key must be defined");
        WebTarget target = putOptions.apply(webTarget).path(key);

        if (flags != 0) {
            target = target.queryParam("flags", UnsignedLongs.toString(flags));
        }

        return target.request().put(value == null ? EMPTY_ENTITY : Entity.text(value), Boolean.class);
    }

    /**
     * Retrieves a list of matching keys for the given key.
     *
     * GET /v1/kv/{key}?keys
     *
     * @param key The key to retrieve.
     * @return A list of zero to many keys.
     */
    public List<String> getKeys(String key) {
        return Arrays.asList(webTarget.path(key).queryParam("keys", "true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(String[].class));
    }

    /**
     * Deletes a specified key.
     *
     * DELETE /v1/kv/{key}
     *
     * @param key The key to delete.
     */
    public void deleteKey(String key) {
        Response response = webTarget.path(key).request().delete();

        if(response.getStatus() != 200) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * Deletes a specified key and any below it.
     *
     * DELETE /v1/kv/{key}?recurse
     *
     * @param key The key to delete.
     */
    public void deleteKeys(String key) {
        Response response = webTarget.path(key).queryParam("recurse", "true").request().delete();

        if(response.getStatus() != 200) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }

    /**
     * Aquire a lock for a given key.
     *
     * PUT /v1/kv/{key}?acquire={session}
     *
     * @param key The key to acquire the lock.
     * @param session The session to acquire lock.
     * @return true if the lock is acquired successfully, false otherwise.
     */
    public boolean acquireLock(final String key, final String session) {
        return acquireLock(key, "", session);
    }

    /**
     * Aquire a lock for a given key.
     *
     * PUT /v1/kv/{key}?acquire={session}
     *
     * @param key The key to acquire the lock.
     * @param session The session to acquire lock.
     * @param value key value (usually - application specific info about the lock requester)
     * @return true if the lock is acquired successfully, false otherwise.
     */
    public boolean acquireLock(final String key, final String value, final String session) {
        return putValue(key, value, 0, ImmutablePutOptions.builder().acquire(session).build());
    }

    /**
     * Retrieves a session string for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value as a string or
     * {@link Optional#absent()}
     */
    public Optional<String> getSession(String key) {
        Optional<Value> value = getValue(key);
        return value.isPresent() ? value.get().getSession() : Optional.<String>absent();
    }


    /**
     * Releases the lock for a given service and session.
     *
     * GET /v1/kv/{key}?release={sessionId}
     *
     * @param key identifying the service.
     * @param sessionId
     *
     * @return {@link SessionInfo}.
     */
    public boolean releaseLock(final String key, final String sessionId) {
        return putValue(key, "", 0, ImmutablePutOptions.builder().release(sessionId).build());
    }

}
