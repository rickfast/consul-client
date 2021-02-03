package ru.hh.consul.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePolicyListResponse.class)
@JsonDeserialize(as = ImmutablePolicyListResponse.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PolicyListResponse extends BasePolicyResponse {
}
