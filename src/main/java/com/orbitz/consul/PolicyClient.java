package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.policy.Policy;
import com.orbitz.consul.model.policy.PolicyResponse;
import com.orbitz.consul.monitoring.ClientEventCallback;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.List;

public class PolicyClient extends BaseClient {

    private static String CLIENT_NAME = "policy";

    private final Api api;

    PolicyClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    public PolicyResponse createPolicy(Policy policy) {
        return http.extract(api.createPolicy(policy));
    }

    public PolicyResponse readPolicy(String id) {
        return http.extract(api.readPolicy(id));
    }

    public PolicyResponse updatePolicy(String id, Policy policy) {
        return http.extract(api.updatePolicy(id, policy));
    }

    public void deletePolicy(String id) {
        http.extract(api.deletePolicy(id));
    }

    public List<PolicyResponse> listPolicies() {
        return http.extract(api.listPolicies());
    }

    interface Api {

        @PUT("acl/policy")
        Call<PolicyResponse> createPolicy(@Body Policy policy);

        @GET("acl/policy/{id}")
        Call<PolicyResponse> readPolicy(@Path("id") String id);

        @PUT("acl/policy/{id}")
        Call<PolicyResponse> updatePolicy(@Path("id") String id, @Body Policy policy);

        @DELETE("acl/policy/{id}")
        Call<Void> deletePolicy(@Path("id") String id);

        @GET("acl/policies")
        Call<List<PolicyResponse>> listPolicies();
    }
}
