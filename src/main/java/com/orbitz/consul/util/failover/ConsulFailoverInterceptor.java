package com.orbitz.consul.util.failover;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.util.failover.strategy.BlacklistingConsulFailoverStrategy;
import com.orbitz.consul.util.failover.strategy.ConsulFailoverStrategy;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ConsulFailoverInterceptor implements Interceptor {

	// The consul failover strategy
	private ConsulFailoverStrategy strategy;

	/**
	 * Default constructor for a set of hosts and ports
	 * 
	 * @param targets
	 */
	public ConsulFailoverInterceptor(Collection<HostAndPort> targets, long timeout) {
		this(new BlacklistingConsulFailoverStrategy(targets, timeout));
	}

	/**
	 * Allows customization of the interceptor chain
	 * 
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
			while ((nextRequest = strategy.computeNextStage(previousRequest, previousResponse)).isPresent()) {
				System.out.println("Requesting with : " + nextRequest.get().url().uri().toString());
				// Get the response from the last viable request
				try {
					final Response lastResponse = chain.proceed(nextRequest.get());

					// If we were successful
					if (lastResponse.isSuccessful())
						return lastResponse;
					else {
						previousResponse = lastResponse;
						previousRequest = nextRequest.get();
					}
				} catch (Exception ex) {
					strategy.markRequestFailed(nextRequest.get());
				}
			}
			throw new ConsulException("Unable to successfully determine a viable host for communication.");

		} else {
			throw new ConsulException(
					"Consul failover strategy has determined that there are no viable hosts remaining.");
		}

	}
}
