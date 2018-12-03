package com.orbitz.consul.util.failover;

import java.io.IOException;
import java.util.*;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.util.failover.strategy.*;

import okhttp3.*;

public class ConsulFailoverInterceptor implements Interceptor {

	// The consul failover strategy
	private ConsulFailoverStrategy strategy;

	/**
	 * Default constructor for a set of hosts and ports
	 * @param targets
	 */
	public ConsulFailoverInterceptor(Collection<HostAndPort> targets, long timeout) {
		this(new BlacklistingConsulFailoverStrategy(targets, timeout));
	}

	/**
	 * Allows customization of the interceptor chain
	 * @param strategy
	 */
	public ConsulFailoverInterceptor(ConsulFailoverStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {

		// The original request
		Request originalRequest = chain.request();

		// If it is possible to do a failover on the first request (as in, one or more
		// targets are viable)
		if (strategy.isRequestViable(originalRequest)) {

			// Initially, we have an inflight request and no response
			Request previousRequest = originalRequest;
			Response previousResponse = null;

			Optional<Request> nextRequest;

			// Get the next viable request
			while ((nextRequest = strategy.computeNextStage(previousRequest, previousResponse)).isPresent())
				// Get the response from the last viable request
				try {

					// Cache for the next cycle if needed
					final Request next = nextRequest.get();
					previousRequest = next;

					// Anything other than an exception is valid here.
					// This is because a 400 series error is a valid code (Permission Denied/Key Not Found)
					return chain.proceed(next);
				} catch (Exception ex) {
					strategy.markRequestFailed(nextRequest.get());
				}
			throw new ConsulException("Unable to successfully determine a viable host for communication.");

		} else
			throw new ConsulException("Consul failover strategy has determined that there are no viable hosts remaining.");

	}
}
