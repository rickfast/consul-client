package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.acl.*;
import com.orbitz.consul.monitoring.ClientEventCallback;
import com.orbitz.consul.option.TokenQueryOptions;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public class AclClient extends BaseClient {

    private static String CLIENT_NAME = "acl";

    private final Api api;

    AclClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    @Deprecated
    public String createAcl(AclToken aclToken) {
        return http.extract(api.createAcl(aclToken)).id();
    }

    @Deprecated
    public void updateAcl(AclToken aclToken) {
        http.handle(api.updateAcl(aclToken));
    }

    @Deprecated
    public void destroyAcl(String id) {
        http.handle(api.destroyAcl(id));
    }

    @Deprecated
    public List<AclResponse> getAclInfo(String id) {
        return http.extract(api.getAclInfo(id));
    }

    @Deprecated
    public String cloneAcl(String id) {
        return http.extract(api.cloneAcl(id)).id();
    }

    @Deprecated
    public List<AclResponse> listAcls() {
        return http.extract(api.listAcls());
    }

    public PolicyResponse createPolicy(Policy policy) {
        return http.extract(api.createPolicy(policy));
    }

    public PolicyResponse readPolicy(String id) {
        return http.extract(api.readPolicy(id));
    }

    public PolicyResponse readPolicyByName(String name) {
        return http.extract(api.readPolicyByName(name));
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

    public TokenResponse createToken(Token token) {
        return http.extract(api.createToken(token));
    }

    public TokenResponse readToken(String id) {
        return http.extract(api.readToken(id));
    }

    public TokenResponse readSelfToken() {
        return http.extract(api.readToken("self"));
    }

    public TokenResponse updateToken(String id, Token token) {
        return http.extract(api.updateToken(id, token));
    }

    public List<TokenListResponse> listTokens() {
        return listTokens(TokenQueryOptions.BLANK);
    }

    public List<TokenListResponse> listTokens(TokenQueryOptions queryOptions) {
        return http.extract(api.listTokens(queryOptions.toQuery()));
    }

    public void deleteToken(String id) {
        http.extract(api.deleteToken(id));
    }

    interface Api {

        @Deprecated
        @PUT("acl/create")
        Call<AclTokenId> createAcl(@Body AclToken aclToken);

        @Deprecated
        @PUT("acl/update")
        Call<Void> updateAcl(@Body AclToken aclToken);

        @Deprecated
        @PUT("acl/destroy/{id}")
        Call<Void> destroyAcl(@Path("id") String id);

        @Deprecated
        @GET("acl/info/{id}")
        Call<List<AclResponse>> getAclInfo(@Path("id") String id);

        @Deprecated
        @PUT("acl/clone/{id}")
        Call<AclTokenId> cloneAcl(@Path("id") String id);

        @Deprecated
        @GET("acl/list")
        Call<List<AclResponse>> listAcls();

        @PUT("acl/policy")
        Call<PolicyResponse> createPolicy(@Body Policy policy);

        @GET("acl/policy/{id}")
        Call<PolicyResponse> readPolicy(@Path("id") String id);

        @GET("acl/policy/name/{name}")
        Call<PolicyResponse> readPolicyByName(@Path("name") String name);

        @PUT("acl/policy/{id}")
        Call<PolicyResponse> updatePolicy(@Path("id") String id, @Body Policy policy);

        @DELETE("acl/policy/{id}")
        Call<Void> deletePolicy(@Path("id") String id);

        @GET("acl/policies")
        Call<List<PolicyResponse>> listPolicies();

        @PUT("acl/token")
        Call<TokenResponse> createToken(@Body Token token);

        @GET("acl/token/{id}")
        Call<TokenResponse> readToken(@Path("id") String id);

        @PUT("acl/token/{id}")
        Call<TokenResponse> updateToken(@Path("id") String id, @Body Token token);

        @GET("acl/tokens")
        Call<List<TokenListResponse>> listTokens(@QueryMap Map<String, Object> query);

        @DELETE("acl/token/{id}")
        Call<Void> deleteToken(@Path("id") String id);
    }

}
