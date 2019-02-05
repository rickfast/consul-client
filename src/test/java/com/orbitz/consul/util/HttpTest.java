package com.orbitz.consul.util;

import com.orbitz.consul.ConsulException;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.monitoring.ClientEventHandler;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HttpTest {

    private ClientEventHandler clientEventHandler;
    private Http http;

    @Before
    public void setUp() {
        clientEventHandler = mock(ClientEventHandler.class);
        http = new Http(clientEventHandler);
    }

    private <T> Function<Call<T>, T> createExtractWrapper() {
        return (call) -> http.extract(call);
    }

    private Function<Call<Void>, Void> createHandleWrapper() {
        return call -> {
            http.handle(call);
            return null;
        };
    }

    private <T> Function<Call<T>, ConsulResponse<T>> createExtractConsulResponseWrapper() {
        return (call) -> http.extractConsulResponse(call);
    }

    @Test
    public void extractingBodyShouldSucceedWhenRequestSucceed() throws IOException {
        String expectedBody = "success";
        Response<String> response = Response.success(expectedBody);
        Call<String> call = mock(Call.class);
        doReturn(response).when(call).execute();

        String body = http.extract(call);

        assertEquals(expectedBody, body);
    }

    @Test
    public void handlingRequestShouldNotThrowWhenRequestSucceed() throws IOException {
        String expectedBody = "success";
        Response<String> response = Response.success(expectedBody);
        Call<Void> call = mock(Call.class);
        doReturn(response).when(call).execute();

        http.handle(call);
    }

    @Test
    public void extractingConsulResponseShouldSucceedWhenRequestSucceed() throws IOException {
        String expectedBody = "success";
        Response<String> response = Response.success(expectedBody);
        Call<String> call = mock(Call.class);
        doReturn(response).when(call).execute();

        ConsulResponse<String> consulResponse = http.extractConsulResponse(call);

        assertEquals(expectedBody, consulResponse.getResponse());
    }

    @Test(expected = ConsulException.class)
    public void extractingBodyShouldThrowWhenRequestFailed() throws IOException {
        checkForFailedRequest(createExtractWrapper());
    }

    @Test(expected = ConsulException.class)
    public void handlingRequestShouldThrowWhenRequestFailed() throws IOException {
        checkForFailedRequest(createHandleWrapper());
    }

    @Test(expected = ConsulException.class)
    public void extractingConsulResponseShouldThrowWhenRequestFailed() throws IOException {
        checkForFailedRequest(createExtractConsulResponseWrapper());
    }

    private <U, V> void checkForFailedRequest(Function<Call<U>, V> httpCall) throws IOException {
        Call<U> call = mock(Call.class);
        doThrow(new IOException("failure")).when(call).execute();

        httpCall.apply(call);
    }

    @Test(expected = ConsulException.class)
    public void extractingBodyShouldThrowWhenRequestIsInvalid() throws IOException {
        checkForInvalidRequest(createExtractWrapper());
    }

    @Test(expected = ConsulException.class)
    public void handlingRequestShouldThrowWhenRequestIsInvalid() throws IOException {
        checkForInvalidRequest(createHandleWrapper());
    }

    @Test(expected = ConsulException.class)
    public void extractingConsulResponseShouldThrowWhenRequestIsInvalid() throws IOException {
        checkForInvalidRequest(createExtractConsulResponseWrapper());
    }

    private <U, V> void checkForInvalidRequest(Function<Call<U>, V> httpCall) throws IOException {
        Response<String> response = Response.error(400, ResponseBody.create(MediaType.parse(""), "failure"));
        Call<U> call = mock(Call.class);
        doReturn(response).when(call).execute();

        httpCall.apply(call);
    }

    @Test
    public void extractingBodyShouldSendSuccessEventWhenRequestSucceed() throws IOException {
        checkSuccessEventIsSentWhenRequestSucceed(createExtractWrapper());
    }

    @Test
    public void handlingRequestShouldSendSuccessEventWhenRequestSucceed() throws IOException {
        checkSuccessEventIsSentWhenRequestSucceed(createHandleWrapper());
    }

    @Test
    public void extractingConsulResponseShouldSendSuccessEventWhenRequestSucceed() throws IOException {
        checkSuccessEventIsSentWhenRequestSucceed(createExtractConsulResponseWrapper());
    }

    private <U, V> void checkSuccessEventIsSentWhenRequestSucceed(Function<Call<U>, V> httpCall) throws IOException {
        String expectedBody = "success";
        Response<String> response = Response.success(expectedBody);
        Call<U> call = mock(Call.class);
        doReturn(response).when(call).execute();

        httpCall.apply(call);

        verify(clientEventHandler, only()).httpRequestSuccess(any(Request.class));
    }

    @Test
    public void extractingBodyShouldSendFailureEventWhenRequestFailed() throws IOException {
        checkFailureEventIsSentWhenRequestFailed(createExtractWrapper());
    }

    @Test
    public void handlingRequestShouldSendFailureEventWhenRequestFailed() throws IOException {
        checkFailureEventIsSentWhenRequestFailed(createHandleWrapper());
    }

    @Test
    public void extractingConsulResponseShouldSendFailureEventWhenRequestFailed() throws IOException {
        checkFailureEventIsSentWhenRequestFailed(createExtractConsulResponseWrapper());
    }

    private <U, V> void checkFailureEventIsSentWhenRequestFailed(Function<Call<U>, V> httpCall) throws IOException {
        Call<U> call = mock(Call.class);
        doThrow(new IOException("failure")).when(call).execute();

        try {
            httpCall.apply(call);
        } catch (ConsulException e) {
            //ignore
        }

        verify(clientEventHandler, only()).httpRequestFailure(any(Request.class), any(Throwable.class));
    }

    @Test
    public void extractingBodyShouldSendInvalidEventWhenRequestIsInvalid() throws IOException {
        checkInvalidEventIsSentWhenRequestIsInvalid(createExtractWrapper());
    }

    @Test
    public void handlingRequestShouldSendInvalidEventWhenRequestIsInvalid() throws IOException {
        checkInvalidEventIsSentWhenRequestIsInvalid(createHandleWrapper());
    }

    @Test
    public void extractingConsulResponseShouldSendInvalidEventWhenRequestIsInvalid() throws IOException {
        checkInvalidEventIsSentWhenRequestIsInvalid(createExtractConsulResponseWrapper());
    }

    private <U, V> void checkInvalidEventIsSentWhenRequestIsInvalid(Function<Call<U>, V> httpCall) throws IOException {
        Response<String> response = Response.error(400, ResponseBody.create(MediaType.parse(""), "failure"));
        Call<U> call = mock(Call.class);
        doReturn(response).when(call).execute();

        try {
            httpCall.apply(call);
        } catch (ConsulException e) {
            //ignore
        }

        verify(clientEventHandler, only()).httpRequestInvalid(any(Request.class), any(Throwable.class));
    }

    @Test
    public void extractingConsulResponseAsyncShouldSucceedWhenRequestSucceed() throws IOException, InterruptedException {
        AtomicReference<ConsulResponse<String>> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        final ConsulResponseCallback<String> callback = new ConsulResponseCallback<String>() {
            @Override
            public void onComplete(ConsulResponse<String> consulResponse) {
                result.set(consulResponse);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) { }
        };
        Call<String> call = mock(Call.class);
        Request request = new Request.Builder().url("http://localhost:8500/this/endpoint").build();
        when(call.request()).thenReturn(request);
        Callback<String> callCallback = http.createCallback(call, callback);
        String expectedBody = "success";

        Response<String> response = Response.success(expectedBody);
        callCallback.onResponse(call, response);
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(expectedBody, result.get().getResponse());
        verify(clientEventHandler, only()).httpRequestSuccess(any(Request.class));
    }

    @Test
    public void extractingConsulResponseAsyncShouldFailWhenRequestIsInvalid() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final ConsulResponseCallback<String> callback = new ConsulResponseCallback<String>() {
            @Override
            public void onComplete(ConsulResponse<String> consulResponse) {}

            @Override
            public void onFailure(Throwable throwable) {
                latch.countDown();
            }
        };
        Call<String> call = mock(Call.class);
        Request request = new Request.Builder().url("http://localhost:8500/this/endpoint").build();
        when(call.request()).thenReturn(request);
        Callback<String> callCallback = http.createCallback(call, callback);

        Response<String> response = Response.error(400, ResponseBody.create(MediaType.parse(""), "failure"));
        callCallback.onResponse(call, response);
        latch.await(1, TimeUnit.SECONDS);

        verify(clientEventHandler, only()).httpRequestInvalid(any(Request.class), any(Throwable.class));
    }

    @Test
    public void extractingConsulResponseAsyncShouldFailWhenRequestFailed() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final ConsulResponseCallback<String> callback = new ConsulResponseCallback<String>() {
            @Override
            public void onComplete(ConsulResponse<String> consulResponse) {}

            @Override
            public void onFailure(Throwable throwable) {
                latch.countDown();
            }
        };
        Call<String> call = mock(Call.class);
        Callback<String> callCallback = http.createCallback(call, callback);

        callCallback.onFailure(call, new RuntimeException("the request failed"));

        latch.await(1, TimeUnit.SECONDS);
        verify(clientEventHandler, only()).httpRequestFailure(any(Request.class), any(Throwable.class));
    }

    @Test
    public void consulResponseShouldHaveResponseAndDefaultValuesIfNoHeader() {
        String responseMessage = "success";
        ConsulResponse<String> expectedConsulResponse = new ConsulResponse<>(responseMessage, 0, false, BigInteger.ZERO);

        Response<String> response = Response.success(responseMessage);
        ConsulResponse<String> consulResponse = Http.consulResponse(response);

        assertEquals(expectedConsulResponse, consulResponse);
    }

    @Test
    public void consulResponseShouldHaveIndexIfPresentInHeader() {
        Response<String> response = Response.success("", Headers.of("X-Consul-Index", "10"));
        ConsulResponse<String> consulResponse = Http.consulResponse(response);

        assertEquals(BigInteger.TEN, consulResponse.getIndex());
    }

    @Test
    public void consulResponseShouldHaveLastContactIfPresentInHeader() {
        Response<String> response = Response.success("", Headers.of("X-Consul-Lastcontact", "2"));
        ConsulResponse<String> consulResponse = Http.consulResponse(response);

        assertEquals(2L, consulResponse.getLastContact());
    }

    @Test
    public void consulResponseShouldHaveKnownLeaderIfPresentInHeader() {
        Response<String> response = Response.success("", Headers.of("X-Consul-Knownleader", "true"));
        ConsulResponse<String> consulResponse = Http.consulResponse(response);

        assertEquals(true, consulResponse.isKnownLeader());
    }
}
