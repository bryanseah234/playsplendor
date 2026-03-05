/**
 * Exception thrown for view-related errors.
 * Used for display failures, input handling errors, and view state issues.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.view;

import com.splendor.exception.SplendorException;

/**
 * Exception thrown when view operations fail.
 * This includes display errors, input handling failures,
 * and other view-related issues.
 */
public class ViewException extends SplendorException {
    
    /**
     * Creates a new ViewException with the specified message.
     * 
     * @param message Exception message
     */
    public ViewException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new ViewException with the specified message and cause.
     * 
     * @param message Exception message
     * @param cause Underlying cause
     */
    public ViewException(final String message, final Throwable cause) {
        super(message, cause);
    }
}