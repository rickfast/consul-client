package ru.hh.consul;

import ru.hh.consul.config.ClientConfig;
import com.orbitz.consul.model.acl.*;
import ru.hh.consul.monitoring.ClientEventCallback;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.util.List;
import ru.hh.consul.model.acl.AclResponse;
import ru.hh.consul.model.acl.AclToken;
import ru.hh.consul.model.acl.AclTokenId;
import ru.hh.consul.model.acl.Policy;
import ru.hh.consul.model.acl.PolicyResponse;
import ru.hh.consul.model.acl.Token;
import ru.hh.consul.model.acl.TokenListResponse;
import ru.hh.consul.model.acl.TokenResponse;

public class AclClient extends BaseClient {

    private static String CLIENT_NAME = "acl";

    private final Api api;

    AclClient(Retrofit retrofit, ClientConfig config, ClientEventCallback eventCallback) {
        super(CLIENT_NAME, config, eventCallback);
        this.api = retrofit.create(Api.class);
    }

    public String createAcl(AclToken aclToken) {
        return http.extract(api.createAcl(aclToken)).id();
    }

    public void updateAcl(AclToken aclToken) {
        http.handle(api.updateAcl(aclToken));
    }

    public void destroyAcl(String id) {
        http.handle(api.destroyAcl(id));
    }

    public List<AclResponse> getAclInfo(String id) {
        return http.extract(api.getAclInfo(id));
    }

    public String cloneAcl(String id) {
        return http.extract(api.cloneAcl(id)).id();
    }

    public List<AclResponse> listAcls() {
        return http.extract(api.listAcls());
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
        return http.extract(api.listTokens());
    }

    public void deleteToken(String id) {
        http.extract(api.deleteToken(id));
    }

    interface Api {

        @PUT("acl/create")
        Call<AclTokenId> createAcl(@Body AclToken aclToken);

        @PUT("acl/update")
        Call<Void> updateAcl(@Body AclToken aclToken);

        @PUT("acl/destroy/{id}")
        Call<Void> destroyAcl(@Path("id") String id);

        @GET("acl/info/{id}")
        Call<List<AclResponse>> getAclInfo(@Path("id") String id);

        @PUT("acl/clone/{id}")
        Call<AclTokenId> cloneAcl(@Path("id") String id);

        @GET("acl/list")
        Call<List<AclResponse>> listAcls();

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

        @PUT("acl/token")
        Call<TokenResponse> createToken(@Body Token token);

        @GET("acl/token/{id}")
        Call<TokenResponse> readToken(@Path("id") String id);

        @PUT("acl/token/{id}")
        Call<TokenResponse> updateToken(@Path("id") String id, @Body Token token);

        @GET("acl/tokens")
        Call<List<TokenListResponse>> listTokens();

        @DELETE("acl/token/{id}")
        Call<Void> deleteToken(@Path("id") String id);
    }

}
