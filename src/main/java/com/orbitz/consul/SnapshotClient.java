package com.orbitz.consul;

import com.orbitz.consul.async.Callback;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.QueryOptions;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * HTTP Client for /v1/snapshot/ endpoints.
 *
 * @see <a href="https://www.consul.io/api/snapshot.html">The Consul API Docs</a> for Snapshots
 */
public class SnapshotClient extends BaseClient {

    private static String CLIENT_NAME = "snapshot";

    private final Api api;

    /**
     * Constructs an instance of this class.
     * @param retrofit The {@link Retrofit} to build a client from.
     */
    SnapshotClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    /**
     * Requests a new snapshot and save it in a file.
     * Only a subset of the QueryOptions is supported: datacenter, consistencymode, and token.
     * @param destinationFile file in which the snapshot is to be saved.
     * @param queryOptions query options. Only a subset of the QueryOptions is supported: datacenter, consistencymode, and token.
     * @param callback callback called once the operation is over. It the save operation is successful, the X-Consul-Index is send.
     */
    public void save(File destinationFile, QueryOptions queryOptions, Callback<BigInteger> callback) {
        http.extractConsulResponse(api.generateSnapshot(queryOptions.toQuery()), new ConsulResponseCallback<ResponseBody>() {
            @Override
            public void onComplete(ConsulResponse<ResponseBody> consulResponse) {
                // Note that response.body() and response.body().byteStream() should be closed.
                // see: https://square.github.io/okhttp/3.x/okhttp/okhttp3/ResponseBody.html
                try (ResponseBody responseBody = consulResponse.getResponse()) {
                    try (InputStream inputStream = responseBody.byteStream()) {
                        Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        callback.onResponse(consulResponse.getIndex());
                    }
                } catch (IOException e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    /**
     * Restores a snapshot stored in a file.
     * @param sourceFile source file where the snapshot is stored.
     * @param queryOptions query options. Only a subset of the QueryOptions is supported: datacenter, token.
     * @param callback callback called once the operation is over.
     */
    public void restore(File sourceFile, QueryOptions queryOptions, Callback<Void> callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/binary"), sourceFile);
        http.extractBasicResponse(api.restoreSnapshot(queryOptions.toQuery(), requestBody), callback);
    }

    /**
     * Retrofit API interface.
     */
    interface Api {

        @Streaming
        @GET("snapshot")
        Call<ResponseBody> generateSnapshot(@QueryMap Map<String, Object> query);

        @PUT("snapshot")
        @Headers("Content-Type: application/binary")
        Call<Void> restoreSnapshot(@QueryMap Map<String, Object> query,
                                   @Body RequestBody requestBody);
    }
}
