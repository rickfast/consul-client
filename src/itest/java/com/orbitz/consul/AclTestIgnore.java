package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.model.acl.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.testcontainers.containers.GenericContainer;

public class AclTestIgnore {

    public static GenericContainer<?> consulContainerAcl;
    static {
        consulContainerAcl = new GenericContainer<>("consul")
                .withCommand("agent", "-dev", "-client", "0.0.0.0", "--enable-script-checks=true")
                .withExposedPorts(8500)
                .withEnv("CONSUL_LOCAL_CONFIG",
                        "{\n" +
                                "  \"acl\": {\n" +
                                "    \"enabled\": true,\n" +
                                "    \"default_policy\": \"deny\",\n" +
                                "    \"tokens\": {\n" +
                                "      \"master\": \"aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "}"
                );
        consulContainerAcl.start();
    }

    protected static Consul client;

    protected static HostAndPort aclClientHostAndPort = HostAndPort.fromParts("localhost", consulContainerAcl.getFirstMappedPort());

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

        assertThat(newPolicyCount, is(oldPolicyCount - 1));
    }

    @Test
    public void testCreateAndReadToken() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String tokenDescription = UUID.randomUUID().toString();
        TokenResponse createdToken = aclClient.createToken(ImmutableToken.builder().description(tokenDescription).local(false).addPolicies(ImmutablePolicyLink.builder().id(createdPolicy.id()).build()).build());

        TokenResponse readToken = aclClient.readToken(createdToken.accessorId());

        assertThat(readToken.description(), is(tokenDescription));
        assertThat(readToken.policies().get(0).name().get(), is(policyName));
    }

    @Test
    public void testReadSelfToken() {
        AclClient aclClient = client.aclClient();

        TokenResponse selfToken = aclClient.readSelfToken();
        assertThat(selfToken.description(), is("Master Token"));
    }

    @Test
    public void testUpdateToken() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        TokenResponse createdToken = aclClient.createToken(ImmutableToken.builder().description("none").local(false).addPolicies(ImmutablePolicyLink.builder().id(createdPolicy.id()).build()).build());
        String newDescription = UUID.randomUUID().toString();
        aclClient.updateToken(createdToken.accessorId(), ImmutableToken.builder().local(false).description(newDescription).build());

        TokenResponse readToken = aclClient.readToken(createdToken.accessorId());
        assertThat(readToken.description(), is(newDescription));
    }

    @Test
    public void testListTokens() {
        AclClient aclClient = client.aclClient();

        assertTrue(aclClient.listTokens().stream().anyMatch(p -> Objects.equals(p.description(), "Anonymous Token")));
        assertTrue(aclClient.listTokens().stream().anyMatch(p -> Objects.equals(p.description(), "Master Token")));
    }

    @Test
    public void testDeleteToken() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());
        TokenResponse createdToken = aclClient.createToken(ImmutableToken.builder().description(UUID.randomUUID().toString()).local(false).addPolicies(ImmutablePolicyLink.builder().id(createdPolicy.id()).build()).build());

        int oldTokenCount = aclClient.listTokens().size();
        aclClient.deleteToken(createdToken.accessorId());

        int newTokenCount = aclClient.listTokens().size();
        assertThat(newTokenCount, is(oldTokenCount - 1));
    }

}
