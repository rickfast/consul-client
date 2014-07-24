package com.orbitz.consul;

/**
 * Wraps an exception thrown whilst interacting with the Consul API.
 */
public class ConsulException extends RuntimeException {

    /**
     * Constructs an instance of this class.
     *
     * @param message The exception message.
     */
    public ConsulException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of this class.
     *
     * @param message The exception message.
     * @param throwable The wrapped {@link java.lang.Throwable} object.
     */
    public ConsulException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
