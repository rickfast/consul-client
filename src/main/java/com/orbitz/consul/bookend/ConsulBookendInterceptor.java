package com.orbitz.consul.bookend;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ConsulBookendInterceptor implements Interceptor {

    private ConsulBookend consulBookend;

    public ConsulBookendInterceptor(ConsulBookend consulBookend) {
        this.consulBookend = consulBookend;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        consulBookend.pre(request.url().encodedPath(), new ConsulBookendContext());

        Response response = chain.proceed(request);

        consulBookend.post(response.isSuccessful());

        return response;
    }
}
