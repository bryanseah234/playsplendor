/**
 * Exception thrown when configuration-related errors occur.
 * Used for configuration loading failures, missing properties, and invalid values.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.config;

/**
 * Exception thrown when configuration operations fail.
 * This includes missing configuration files, invalid property values,
 * and missing required properties.
 */
public class ConfigException extends Exception {
    
    /**
     * Creates a new ConfigException with the specified message.
     * 
     * @param message Exception message
     */
    public ConfigException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new ConfigException with the specified message and cause.
     * 
     * @param message Exception message
     * @param cause Underlying cause
     */
    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }
}