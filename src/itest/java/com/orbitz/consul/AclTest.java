package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.model.acl.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AclTest {

    protected static Consul client;

    protected static HostAndPort aclClientHostAndPort = HostAndPort.fromParts("localhost", 8501);

    @BeforeClass
    public static void beforeClass() {
        client = Consul.builder()
                .withHostAndPort(aclClientHostAndPort)
                .withAclToken("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
                .withReadTimeoutMillis(Duration.ofSeconds(2).toMillis())
                .build();
    }

    @Test
    public void listPolicies() {
        AclClient aclClient = client.aclClient();
        assertTrue(aclClient.listPolicies().stream().anyMatch(p -> Objects.equals(p.name(), "global-management")));
    }

    @Test
    public void testCreateAndReadPolicy() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse policy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());
        assertThat(policy.name(), is(policyName));

        policy = aclClient.readPolicy(policy.id());
        assertThat(policy.name(), is(policyName));
    }

    @Test
    public void testUpdatePolicy() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String newPolicyName = UUID.randomUUID().toString();
        aclClient.updatePolicy(createdPolicy.id(), ImmutablePolicy.builder().name(newPolicyName).build());

        PolicyResponse updatedPolicy = aclClient.readPolicy(createdPolicy.id());
        assertThat(updatedPolicy.name(), is(newPolicyName));
    }

    @Test
    public void testDeletePolicy() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        int oldPolicyCount = aclClient.listPolicies().size();
        aclClient.deletePolicy(createdPolicy.id());
        int newPolicyCount = aclClient.listPolicies().size();

        assertThat(oldPolicyCount, is(newPolicyCount + 1));
    }

    @Test
    public void testCreateAndReadToken() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String tokenDescription = UUID.randomUUID().toString();
        TokenResponse createdToken = aclClient.createToken(ImmutableToken.builder().description(tokenDescription).local(false).addPolicies(ImmutablePolicyLink.builder().id(createdPolicy.id()).build()).build());

        TokenResponse readToken = aclClient.readToken(createdToken.accessorId());

        assertThat(readToken.description().get(), is(tokenDescription));
        assertThat(readToken.policies().get(0).name().get(), is(policyName));
    }

}
