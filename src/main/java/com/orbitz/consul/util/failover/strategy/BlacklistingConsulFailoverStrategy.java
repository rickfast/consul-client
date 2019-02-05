package com.orbitz.consul.util.failover.strategy;

import java.time.*;
import java.util.*;

import com.google.common.net.HostAndPort;

import okhttp3.*;

/**
 * @author Troy Heanssgen
 */
public class BlacklistingConsulFailoverStrategy implements ConsulFailoverStrategy {

	// The map of blacklisted addresses
	private Map<HostAndPort, Instant> blacklist = Collections.synchronizedMap(new HashMap<>());

	// The map of viable targets
	private Collection<HostAndPort> targets;

	// The blacklist timeout
	private long timeout;

	/**
	 * Constructs a blacklisting strategy with a collection of hosts and ports
	 * @param targets
	 *        A set of viable hosts
	 */
	public BlacklistingConsulFailoverStrategy(Collection<HostAndPort> targets, long timeout) {
		this.targets = targets;
		this.timeout = timeout;
	}

	@Override
	public Optional<Request> computeNextStage(Request previousRequest, Response previousResponse) {

		// Create a host and port
		final HostAndPort initialTarget = fromRequest(previousRequest);

		// If the previous response failed, disallow this request from going through.
		// A 404 does NOT indicate a failure in this case, so it should never blacklist the previous target.
		if ((previousResponse != null) && !previousResponse.isSuccessful() && !(previousResponse.code() == 404))
			this.blacklist.put(initialTarget, Instant.now());

		// If our blacklist contains the target we care about
		if (blacklist.containsKey(initialTarget)) {

			// Find the first entity that doesnt exist in the blacklist
			HostAndPort next = targets.stream().filter(target -> {

				// If we have blacklisted this key
				if (blacklist.containsKey(target)) {

					// Get when we blacklisted this key
					final Instant blacklistWhen = blacklist.get(target);

					// If !(Duration(then, now) - timeout >=0) means that we remove this blacklist
					// entry when the duration between
					// the blacklist marker and now is greater than the timeout duration
					if (!Duration.between(blacklistWhen, Instant.now()).minusMillis(timeout).isNegative()) {
						blacklist.remove(target);
						return true;
					} else
						return false;
				} else
					return true;
			}).findAny().get();

			// Construct the next URL using the old parameters (ensures we don't have to do
			// a copy-on-write
			final HttpUrl nextURL = previousRequest.url().newBuilder().host(next.getHost()).port(next.getPort()).build();

			// Return the result
			return Optional.ofNullable(previousRequest.newBuilder().url(nextURL).build());
		} else {

			// Construct the next URL using the old parameters (ensures we don't have to do
			// a copy-on-write
			final HttpUrl nextURL = previousRequest.url().newBuilder().host(initialTarget.getHost()).port(initialTarget.getPort()).build();

			// Return the result
			return Optional.ofNullable(previousRequest.newBuilder().url(nextURL).build());
		}

	}

	@Override
	public boolean isRequestViable(Request current) {
		return (targets.size() > blacklist.size()) || !blacklist.containsKey(fromRequest(current));
	}

	@Override
	public void markRequestFailed(Request current) {
		this.blacklist.put(fromRequest(current), Instant.now());
	}

	/**
	 * Reconstructs a HostAndPort instance from the request object
	 * @param request
	 * @return
	 */
	private HostAndPort fromRequest(Request request) {
		return HostAndPort.fromParts(request.url().host(), request.url().port());
	}

}
