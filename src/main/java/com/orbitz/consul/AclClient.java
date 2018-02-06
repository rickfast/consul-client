package com.orbitz.consul;

import com.orbitz.consul.config.ClientConfig;
import com.orbitz.consul.model.acl.AclResponse;
import com.orbitz.consul.model.acl.AclToken;
import com.orbitz.consul.model.acl.AclTokenId;
import com.orbitz.consul.monitoring.ClientEventCallback;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

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
    }
}
