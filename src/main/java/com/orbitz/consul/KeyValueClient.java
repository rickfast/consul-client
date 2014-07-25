package com.orbitz.consul;

import com.orbitz.consul.model.kv.Value;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.orbitz.consul.util.ClientUtil.queryParams;

public class KeyValueClient {
    
    private final WebTarget webTarget;
    
    KeyValueClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/keyValue/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link java.util.Optional} containing the value or {@link java.util.Optional#empty()}
     */
    public Optional<Value> getValue(String key) {
        Value[] values = webTarget.path(key).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Value[].class);

        return values != null && values.length != 0 ? Optional.of(values[0]) : Optional.<Value>empty();
    }

    /**
     * Retrieves a list of {@link com.orbitz.consul.model.kv.Value} objects for a specific key
     * from the key/value store.
     *
     * GET /v1/keyValue/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many {@link com.orbitz.consul.model.kv.Value} objects.
     */
    public List<Value> getValues(String key) {
        return Arrays.asList(webTarget.path(key).queryParam("recurse", "true").request()
                .accept(MediaType.APPLICATION_JSON_TYPE).get(Value[].class));
    }

    /**
     * Retrieves a string value for a specific key from the key/value store.
     *
     * GET /v1/keyValue/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link java.util.Optional} containing the value as a string or
     * {@link java.util.Optional#empty()}
     */
    public Optional<String> getValueAsString(String key) {
        Optional<Value> value = getValue(key);

        return value.isPresent() ? Optional.of(new String(Base64.getDecoder().decode(value.get().getValue())))
                : Optional.empty();
    }

    /**
     * Retrieves a list of string values for a specific key from the key/value
     * store.
     *
     * GET /v1/keyValue/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A list of zero to many string values.
     */
    public List<String> getValuesAsString(String key) {
        return getValues(key).stream().map((value) -> value.getValue()).collect(Collectors.toList());
    }

    public boolean putValue(String key, String value) {
        return putValue(key, value, Collections.EMPTY_MAP);
    }

    /**
     * Puts a value
     * @param key
     * @param value
     * @param params
     * @return
     */
    private boolean putValue(String key, String value, Map<String, String> params) {
        return queryParams(webTarget, params).request().put(Entity.entity(value,
                MediaType.TEXT_PLAIN_TYPE), Boolean.class);
    }

    /**
     * Retrieves a list of matching keys for the given key.
     *
     * GET /v1/keyValue/{key}?keys
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
     * DELETE /v1/keyValue/{key}
     *
     * @param key The key to delete.
     */
    public void deleteKey(String key) {
        delete(key, Collections.EMPTY_MAP);
    }

    /**
     * Deletes a specified key and any below it.
     *
     * DELETE /v1/keyValue/{key}?recurse
     *
     * @param key The key to delete.
     */
    public void deleteKeys(String key) {
        delete(key, Collections.singletonMap("recurse", "true"));
    }

    /**
     * Deletes a specified key.
     *
     * @param key The key to delete.
     * @param params Map of parameters, e.g. recurse=true.
     */
    private void delete(String key, Map<String, String> params) {
        Response response = webTarget.path(key).request().delete();

        if(response.getStatus() != 200) {
            throw new ConsulException(response.readEntity(String.class));
        }
    }
}
