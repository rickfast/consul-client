package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.model.acl.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
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
    public void testCreateAndReadPolicyByName() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse policy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());
        assertThat(policy.name(), is(policyName));

        policy = aclClient.readPolicyByName(policy.name());
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
    public void testCreateAndCloneTokenWithNewDescription() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String tokenDescription = UUID.randomUUID().toString();
        TokenResponse createdToken = aclClient.createToken(
                ImmutableToken.builder()
                        .description(tokenDescription)
                        .local(false)
                        .addPolicies(
                                ImmutablePolicyLink.builder()
                                        .id(createdPolicy.id())
                                        .build()
                        ).build());

        String updatedTokenDescription = UUID.randomUUID().toString();
        Token updateToken =
                ImmutableToken.builder()
                        .id(createdToken.accessorId())
                        .description(updatedTokenDescription)
                        .build();

        TokenResponse readToken = aclClient.cloneToken(createdToken.accessorId(), updateToken);

        assertThat(readToken.accessorId(), not(createdToken.accessorId()));
        assertThat(readToken.description(), is(updatedTokenDescription));
    }

    @Test
    public void testCreateAndReadTokenWithCustomIds() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String tokenId = UUID.randomUUID().toString();
        String tokenSecretId = UUID.randomUUID().toString();
        Token token = ImmutableToken.builder()
                .id(tokenId)
                .secretId(tokenSecretId)
                .local(false)
                .addPolicies(
                        ImmutablePolicyLink.builder()
                                .id(createdPolicy.id())
                                .build()
                ).build();
        TokenResponse createdToken = aclClient.createToken(token);

        TokenResponse readToken = aclClient.readToken(createdToken.accessorId());

        assertThat(readToken.accessorId(), is(tokenId));
        assertThat(readToken.secretId(), is(tokenSecretId));
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

    @Test
    public void testListRoles() {
        AclClient aclClient = client.aclClient();

        String roleName1 = UUID.randomUUID().toString();
        String roleName2 = UUID.randomUUID().toString();
        aclClient.createRole(ImmutableRole.builder().name(roleName1).build());
        aclClient.createRole(ImmutableRole.builder().name(roleName2).build());

        assertTrue(aclClient.listRoles().stream().anyMatch(p -> Objects.equals(p.name(), roleName1)));
        assertTrue(aclClient.listRoles().stream().anyMatch(p -> Objects.equals(p.name(), roleName2)));
    }

    @Test
    public void testCreateAndReadRole() {
        AclClient aclClient = client.aclClient();

        String roleName = UUID.randomUUID().toString();
        RoleResponse role = aclClient.createRole(ImmutableRole.builder().name(roleName).build());

        RoleResponse roleResponse = aclClient.readRole(role.id());
        assertEquals(role.id(), roleResponse.id());
    }

    @Test
    public void testCreateAndReadRoleByName() {
        AclClient aclClient = client.aclClient();

        String roleName = UUID.randomUUID().toString();
        RoleResponse role = aclClient.createRole(ImmutableRole.builder().name(roleName).build());

        RoleResponse roleResponse = aclClient.readRoleByName(role.name());
        assertEquals(role.name(), roleResponse.name());
    }

    @Test
    public void testCreateAndReadRoleWithPolicy() {
        AclClient aclClient = client.aclClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = aclClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String roleName = UUID.randomUUID().toString();
        RoleResponse role = aclClient.createRole(
                ImmutableRole.builder()
                        .name(roleName)
                        .addPolicies(
                                ImmutableRolePolicyLink.builder()
                                .id(createdPolicy.id())
                                .build()
                        )
                        .build());

        RoleResponse roleResponse = aclClient.readRole(role.id());
        assertEquals(role.id(), roleResponse.id());
        assertEquals(1, roleResponse.policies().size());
        assertTrue(roleResponse.policies().get(0).id().isPresent());
        assertEquals(createdPolicy.id(), roleResponse.policies().get(0).id().get());
    }

    @Test
    public void testUpdateRole() {
        AclClient aclClient = client.aclClient();

        String roleName = UUID.randomUUID().toString();
        String roleDescription = UUID.randomUUID().toString();
        RoleResponse role = aclClient.createRole(
                ImmutableRole.builder()
                        .name(roleName)
                        .description(roleDescription)
                        .build());

        RoleResponse roleResponse = aclClient.readRole(role.id());
        assertEquals(roleDescription, roleResponse.description());

        String roleNewDescription = UUID.randomUUID().toString();
        RoleResponse updatedRoleResponse = aclClient.updateRole(roleResponse.id(),
                ImmutableRole.builder()
                        .name(roleName)
                        .description(roleNewDescription)
                        .build());

        assertEquals(roleNewDescription, updatedRoleResponse.description());
    }

    @Test
    public void testDeleteRole() {
        AclClient aclClient = client.aclClient();

        String roleName = UUID.randomUUID().toString();
        RoleResponse role = aclClient.createRole(
                ImmutableRole.builder()
                        .name(roleName)
                        .build());

        RoleResponse roleResponse = aclClient.readRole(role.id());
        assertEquals(roleName, roleResponse.name());

        aclClient.deleteRole(roleResponse.id());

        assertThrows(ConsulException.class, () -> aclClient.readRole(roleResponse.id()));
    }

}
