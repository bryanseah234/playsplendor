/**
 * Exception thrown when card or noble data cannot be loaded.
 * Used by data providers to signal configuration or parsing failures.
 * 
 */
package com.splendor.data;

/**
 * Custom exception for data loading failures in the Splendor game.
 * 
 * This exception is thrown when:
 * - Card/noble data files cannot be found or read
 * - CSV data is malformed or contains invalid values
 * - Required configuration properties are missing
 * 
 * The exception includes detailed information about the failure
 * to aid in debugging configuration issues.
 */
public class DataLoadException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new DataLoadException with the specified message.
     * 
     * @param message Detailed description of the data loading failure
     */
    public DataLoadException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new DataLoadException with the specified message and cause.
     * 
     * @param message Detailed description of the data loading failure
     * @param cause The underlying exception that caused this failure
     */
    public DataLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
