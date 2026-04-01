package com.splendor.exception;

/**
 * Exception thrown when configuration operations fail.
 * This includes missing configuration files, invalid property values,
 * and missing required properties.
 */
public class ConfigException extends Exception {

    public ConfigException(final String message) {
        super(message);
    }

    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
