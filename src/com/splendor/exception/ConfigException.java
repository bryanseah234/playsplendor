package com.splendor.exception;

/**
 * Exception thrown when configuration operations fail.
 * This includes missing configuration files, invalid property values,
 * and missing required properties.
 */
public class ConfigException extends Exception {

    /**
     * Constructs a new ConfigException with the specified detail message.
     *
     * @param message The detail message
     */
    public ConfigException(final String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The underlying cause
     */
    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
