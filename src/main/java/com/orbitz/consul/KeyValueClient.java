package com.orbitz.consul;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.nio.charset.Charset;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedLongs;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Operation;
import com.orbitz.consul.model.kv.TxResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.SessionInfo;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.ConsistencyMode;
import com.orbitz.consul.option.DeleteOptions;
import com.orbitz.consul.option.ImmutablePutOptions;
import com.orbitz.consul.option.ImmutableTransactionOptions;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import com.orbitz.consul.option.TransactionOptions;
import com.orbitz.consul.util.Jackson;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.orbitz.consul.util.Strings.trimLeadingSlash;

/**
 * HTTP Client for /v1/kv/ endpoints.
 */
public class KeyValueClient extends BaseClient {

    private static String CLIENT_NAME = "keyvalue";
    public static final int NOT_FOUND_404 = 404;

    private final Api api;

    /**
     * Constructs an instance of this class.
     *
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    KeyValueClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value or {@link Optional#empty()()}
     */
    public Optional<Value> getValue(String key) {
        return getValue(key, QueryOptions.BLANK);
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.ConsulResponse} with the
     * {@link com.orbitz.consul.model.kv.Value} for a spefici key from the
     * key/value store
     * @param key The key to retrieve
     * @return An {@link Optional} containing the {@link ConsulResponse} or {@link Optional#empty()()}
     */
    public Optional<ConsulResponse<Value>> getConsulResponseWithValue(String key) {
        return getConsulResponseWithValue(key, QueryOptions.BLANK);
    }

    /**
     * Retrieves a {@link com.orbitz.consul.model.kv.Value} for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @return An {@link Optional} containing the value or {@link Optional#empty()()}
     */
    public Optional<Value> getValue(String key, QueryOptions queryOptions) {
        try {
            return getSingleValue(http.extract(api.getValue(trimLeadingSlash(key), queryOptions.toQuery()), NOT_FOUND_404));
        } catch (ConsulException ignored) {
            if(ignored.getCode() != NOT_FOUND_404) {
                throw ignored;
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a {@link ConsulResponse<Value>} for a specific key from the kv store.
     * Contains the consul response headers along with the configuration value.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @return An {@link Optional} containing the ConsulResponse or {@link Optional#empty()}
     */
    public Optional<ConsulResponse<Value>> getConsulResponseWithValue(String key, QueryOptions queryOptions) {
        try {
            ConsulResponse<List<Value>> consulResponse =
                    http.extractConsulResponse(api.getValue(trimLeadingSlash(key), queryOptions.toQuery()), NOT_FOUND_404);
            Optional<Value> consulValue = getSingleValue(consulResponse.getResponse());
            if (consulValue.isPresent()) {
                ConsulResponse<Value> result =
                        new ConsulResponse<>(consulValue.get(), consulResponse.getLastContact(),
                                                    consulResponse.isKnownLeader(), consulResponse.getIndex());
                return Optional.of(result);
            }
        } catch (ConsulException ignored) {
            if (ignored.getCode() != NOT_FOUND_404) {
                throw ignored;
            }
        }

        return Optional.empty();
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
                        new ConsulResponse<>(getSingleValue(consulResponse.getResponse()),
                                consulResponse.getLastContact(),
                                consulResponse.isKnownLeader(), consulResponse.getIndex()));
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        };

        http.extractConsulResponse(api.getValue(trimLeadingSlash(key), queryOptions.toQuery()), wrapper, NOT_FOUND_404);
    }

    private Optional<Value> getSingleValue(List<Value> values) {
        return values != null && values.size() != 0 ? Optional.of(values.get(0)) : Optional.empty();
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
        return getValues(key, QueryOptions.BLANK);
    }

    /**
     * Retrieves a {@link ConsulResponse} with a list of {@link Value} objects along with
     * consul response headers for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @return A {@link ConsulResponse} with a list of zero to many {@link Value} objects and
     * consul response headers.
     */
    public ConsulResponse<List<Value>> getConsulResponseWithValues(String key) {
        return getConsulResponseWithValues(key, QueryOptions.BLANK);
    }

    /**
     * Retrieves a list of {@link com.orbitz.consul.model.kv.Value} objects for a specific key
     * from the key/value store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options.
     * @return A list of zero to many {@link com.orbitz.consul.model.kv.Value} objects.
     */
    public List<Value> getValues(String key, QueryOptions queryOptions) {
        Map<String, Object> query = queryOptions.toQuery();

        query.put("recurse", "true");

        List<Value> result = http.extract(api.getValue(trimLeadingSlash(key), query), NOT_FOUND_404);

        return result == null ? Collections.emptyList() : result;
    }

    /**
     * Retrieves a {@link ConsulResponse} with a list of {@link Value} objects along with
     * consul response headers for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @param queryOptions The query options to use.
     * @return A {@link ConsulResponse} with a list of zero to many {@link Value} objects and
     * consul response headers.
     */
    public ConsulResponse<List<Value>> getConsulResponseWithValues(String key, QueryOptions queryOptions) {
        Map<String, Object> query = queryOptions.toQuery();

        query.put("recurse", "true");

        return http.extractConsulResponse(api.getValue(trimLeadingSlash(key), query), NOT_FOUND_404);
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
        Map<String, Object> query = queryOptions.toQuery();

        query.put("recurse", "true");

        http.extractConsulResponse(api.getValue(trimLeadingSlash(key), query), callback, NOT_FOUND_404);
    }

    /**
     * Retrieves a string value for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @return An {@link Optional} containing the value as a string or
     * {@link Optional#empty()}
     */
    public Optional<String> getValueAsString(String key) {
        return getValueAsString(key, Charset.defaultCharset());
    }

    /**
     * Retrieves a string value for a specific key from the key/value store.
     *
     * GET /v1/kv/{key}
     *
     * @param key The key to retrieve.
     * @param charset The charset of the value
     * @return An {@link Optional} containing the value as a string or
     * {@link Optional#empty()}
     */
    public Optional<String> getValueAsString(String key, Charset charset) {
        return getValue(key).flatMap(v -> v.getValueAsString(charset));
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
        return getValuesAsString(key, Charset.defaultCharset());
    }

    /**
     * Retrieves a list of string values for a specific key from the key/value
     * store.
     *
     * GET /v1/kv/{key}?recurse
     *
     * @param key The key to retrieve.
     * @param charset The charset of the value
     * @return A list of zero to many string values.
     */
    public List<String> getValuesAsString(String key, Charset charset) {
        List<String> result = new ArrayList<>();

        for(Value value : getValues(key)) {
            value.getValueAsString(charset).ifPresent(result::add);
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
        return putValue(key, null, 0L, PutOptions.BLANK, Charset.defaultCharset());
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
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, Charset charset) {
        return putValue(key, value, 0L, PutOptions.BLANK, charset);
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
     * @param flags The flags for this key.
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, long flags, Charset charset) {
        return putValue(key, value, flags, PutOptions.BLANK, charset);
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
        return putValue(key, value, flags, putOptions, Charset.defaultCharset());
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param putOptions PUT options (e.g. wait, acquire).
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, String value, long flags, PutOptions putOptions, Charset charset) {

        checkArgument(StringUtils.isNotEmpty(key), "Key must be defined");
        Map<String, Object> query = putOptions.toQuery();

        if (flags != 0) {
            query.put("flags", UnsignedLongs.toString(flags));
        }

        if (value == null) {
            return http.extract(api.putValue(trimLeadingSlash(key),
                    query));
        } else {
            return http.extract(api.putValue(trimLeadingSlash(key),
                    RequestBody.create(MediaType.parse("text/plain; charset=" + charset.name()), value), query));
        }
    }

    /**
     * Puts a value into the key/value store.
     *
     * @param key The key to use as index.
     * @param value The value to index.
     * @param putOptions PUT options (e.g. wait, acquire).
     * @return <code>true</code> if the value was successfully indexed.
     */
    public boolean putValue(String key, byte[] value, long flags, PutOptions putOptions) {

        checkArgument(StringUtils.isNotEmpty(key), "Key must be defined");
        Map<String, Object> query = putOptions.toQuery();

        if (flags != 0) {
            query.put("flags", UnsignedLongs.toString(flags));
        }

        if (value == null) {
            return http.extract(api.putValue(trimLeadingSlash(key),
                    query));
        } else {
            return http.extract(api.putValue(trimLeadingSlash(key),
                    RequestBody.create(MediaType.parse("application/octet-stream"), value), query));
        }
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
        return http.extract(api.getKeys(trimLeadingSlash(key), ImmutableMap.of("keys", "true")));
    }

    /**
     * Deletes a specified key.
     *
     * DELETE /v1/kv/{key}
     *
     * @param key The key to delete.
     */
    public void deleteKey(String key) {
        deleteKey(key, DeleteOptions.BLANK);
    }

    /**
     * Deletes a specified key and any below it.
     *
     * DELETE /v1/kv/{key}?recurse
     *
     * @param key The key to delete.
     */
    public void deleteKeys(String key) {
        deleteKey(key, DeleteOptions.RECURSE);
    }

    /**
     * Deletes a specified key.
     *
     * DELETE /v1/kv/{key}
     *
     * @param key The key to delete.
     * @param deleteOptions DELETE options (e.g. recurse, cas)
     */
    public void deleteKey(String key, DeleteOptions deleteOptions) {
        checkArgument(StringUtils.isNotEmpty(key), "Key must be defined");
        Map<String, Object> query = deleteOptions.toQuery();

        http.handle(api.deleteValues(trimLeadingSlash(key), query));
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
     * {@link Optional#empty()}
     */
    public Optional<String> getSession(String key) {
        return getValue(key).flatMap(Value::getSession);
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

    /**
     * Performs a Consul transaction.
     *
     * PUT /v1/tx
     *
     * @param operations A list of KV operations.
     * @return A {@link ConsulResponse} containing results and potential errors.
     */
    public ConsulResponse<TxResponse> performTransaction(Operation... operations) {
        ImmutableTransactionOptions immutableTransactionOptions =
            ImmutableTransactionOptions.builder().consistencyMode(ConsistencyMode.DEFAULT).build();
        return performTransaction(immutableTransactionOptions, operations);
    }

    /**
     * Performs a Consul transaction.
     *
     * PUT /v1/tx
     *
     * @deprecated Replaced by {@link #performTransaction(TransactionOptions, Operation...)}
     *
     * @param consistency The consistency to use for the transaction.
     * @param operations A list of KV operations.
     * @return A {@link ConsulResponse} containing results and potential errors.
     */
    @Deprecated
    public ConsulResponse<TxResponse> performTransaction(ConsistencyMode consistency, Operation... operations) {

        Map<String, Object> query = consistency == ConsistencyMode.DEFAULT
                ? ImmutableMap.of()
                : ImmutableMap.of(consistency.toParam().get(), "true");

        try {
            return http.extractConsulResponse(api.performTransaction(RequestBody.create(MediaType.parse("application/json"),
                    Jackson.MAPPER.writeValueAsString(kv(operations))), query));
        } catch (JsonProcessingException e) {
            throw new ConsulException(e);
        }
    }

    /**
     * Performs a Consul transaction.
     *
     * PUT /v1/tx
     *
     * @param transactionOptions transaction options (e.g. dc, consistency).
     * @param operations A list of KV operations.
     * @return A {@link ConsulResponse} containing results and potential errors.
     */
    public ConsulResponse<TxResponse> performTransaction(TransactionOptions transactionOptions, Operation... operations) {
        Map<String, Object> query = transactionOptions.toQuery();

        try {
            return http.extractConsulResponse(api.performTransaction(RequestBody.create(MediaType.parse("application/json"),
                Jackson.MAPPER.writeValueAsString(kv(operations))), query));
        } catch (JsonProcessingException e) {
            throw new ConsulException(e);
        }
    }

    /**
     * Wraps {@link Operation} in a <code>"KV": { }</code> block.
     * @param operations An array of ops.
     * @return An array of wrapped ops.
     */
    static Kv[] kv(Operation... operations) {
        Kv[] kvs = new Kv[operations.length];

        for (int i = 0; i < operations.length; i ++) {
            kvs[i] = new Kv(operations[i]);
        }

        return kvs;
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @GET("kv/{key}")
        Call<List<Value>> getValue(@Path("key") String key,
                                   @QueryMap Map<String, Object> query);

        @GET("kv/{key}")
        Call<List<String>> getKeys(@Path("key") String key,
                                   @QueryMap Map<String, Object> query);

        @PUT("kv/{key}")
        Call<Boolean> putValue(@Path("key") String key,
                               @QueryMap Map<String, Object> query);
        @PUT("kv/{key}")
        Call<Boolean> putValue(@Path("key") String key,
                               @Body RequestBody data,
                               @QueryMap Map<String, Object> query);

        @DELETE("kv/{key}")
        Call<Void> deleteValues(@Path("key") String key,
                                @QueryMap Map<String, Object> query);

        @PUT("txn")
        @Headers("Content-Type: application/json")
        Call<TxResponse> performTransaction(@Body RequestBody body,
                                            @QueryMap Map<String, Object> query);
    }

    /**
     * Wrapper for Transaction KV entry.
     */
    static class Kv {
        private Operation kv;

        private Kv(Operation operation) {
            kv = operation;
        }

        public Operation getKv() {
            return kv;
        }
    }
}
