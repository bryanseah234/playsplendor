/**
 * Base exception class for all Splendor-related exceptions.
 * Provides a common parent for all custom exceptions in the application.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.exception;

/**
 * Base class for all Splendor-specific exceptions.
 * All custom exceptions should extend this class for consistent error handling.
 */
public class SplendorException extends Exception {
    
    /**
     * Creates a new SplendorException with the specified message.
     * 
     * @param message Exception message
     */
    public SplendorException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new SplendorException with the specified message and cause.
     * 
     * @param message Exception message
     * @param cause Underlying cause
     */
    public SplendorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}