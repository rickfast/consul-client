package com.orbitz.consul;

import com.orbitz.consul.model.acl.AclResponse;
import com.orbitz.consul.model.acl.AclToken;
import com.orbitz.consul.model.acl.AclTokenId;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

import static com.orbitz.consul.util.Http.*;

public class AclClient {

    private final Api api;

    AclClient(Retrofit retrofit) {
        this.api = retrofit.create(Api.class);
    }

    public String createAcl(AclToken aclToken) {
        return extract(api.createAcl(aclToken)).id();
    }

    public void updateAcl(AclToken aclToken) {
        handle(api.updateAcl(aclToken));
    }

    public void destroyAcl(String id) {
        handle(api.destroyAcl(id));
    }

    public AclResponse getAclInfo(String id) {
        return extract(api.getAclInfo(id));
    }

    public String cloneAcl(String id) {
        return extract(api.cloneAcl(id)).id();
    }

    public List<AclResponse> listAcls() {
        return extract(api.listAcls());
    }

    private interface Api {

        @PUT("acl/create")
        Call<AclTokenId> createAcl(@Body AclToken aclToken);

        @PUT("acl/update")
        Call<Void> updateAcl(@Body AclToken aclToken);

        @PUT("acl/destroy/{id}")
        Call<Void> destroyAcl(@Path("id") String id);

        @GET("acl/info/{id}")
        Call<AclResponse> getAclInfo(@Path("id") String id);

        @PUT("acl/clone/{id}")
        Call<AclTokenId> cloneAcl(@Path("id") String id);

        @GET("acl/list")
        Call<List<AclResponse>> listAcls();
    }
}
