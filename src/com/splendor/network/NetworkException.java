/**
 * Exception thrown for network-related errors.
 * Used for connection failures, protocol violations, and network communication issues.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.network;

import com.splendor.exception.SplendorException;

/**
 * Exception thrown when network operations fail.
 * This includes connection failures, protocol violations,
 * and network communication issues.
 */
public class NetworkException extends SplendorException {
    
    /**
     * Creates a new NetworkException with the specified message.
     * 
     * @param message Exception message
     */
    public NetworkException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new NetworkException with the specified message and cause.
     * 
     * @param message Exception message
     * @param cause Underlying cause
     */
    public NetworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}