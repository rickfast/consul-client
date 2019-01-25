package com.orbitz.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.*;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.policy.BasePolicyResponse;
import com.orbitz.consul.model.policy.ImmutablePolicy;
import com.orbitz.consul.model.policy.Policy;
import com.orbitz.consul.model.policy.PolicyResponse;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PolicyTest {

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
        PolicyClient policyClient = client.policyClient();
        assertTrue(policyClient.listPolicies().stream().anyMatch(p -> Objects.equals(p.name(), "global-management")));
    }

    @Test
    public void testCreateAndReadPolicy() {
        PolicyClient policyClient = client.policyClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse policy = policyClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());
        assertThat(policy.name(), is(policyName));

        policy = policyClient.readPolicy(policy.id());
        assertThat(policy.name(), is(policyName));
    }

    @Test
    public void testUpdatePolicy() {
        PolicyClient policyClient = client.policyClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = policyClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        String newPolicyName = UUID.randomUUID().toString();
         policyClient.updatePolicy(createdPolicy.id(), ImmutablePolicy.builder().name(newPolicyName).build());

        PolicyResponse updatedPolicy = policyClient.readPolicy(createdPolicy.id());
        assertThat(updatedPolicy.name(), is(newPolicyName));
    }

    @Test
    public void testDeletePolicy() {
        PolicyClient policyClient = client.policyClient();

        String policyName = UUID.randomUUID().toString();
        PolicyResponse createdPolicy = policyClient.createPolicy(ImmutablePolicy.builder().name(policyName).build());

        int oldPolicyCount = policyClient.listPolicies().size();
        policyClient.deletePolicy(createdPolicy.id());
        int newPolicyCount = policyClient.listPolicies().size();

        assertThat(oldPolicyCount, is(newPolicyCount + 1));
    }
}
