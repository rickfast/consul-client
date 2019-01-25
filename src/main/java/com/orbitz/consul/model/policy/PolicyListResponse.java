package com.orbitz.consul.model.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutablePolicyResponse.class)
@JsonDeserialize(as = ImmutablePolicyResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PolicyListResponse extends BasePolicyResponse {


}
