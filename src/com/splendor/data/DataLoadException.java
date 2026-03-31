/**
 * Exception thrown when card or noble data cannot be loaded.
 * Used by data providers to signal configuration or parsing failures.
 */
package com.splendor.data;

/**
 * Exception indicating a failure during card or noble data loading operations.
 * 
 * This exception is part of the fail-fast initialization strategy: if the game
 * data (cards, nobles) cannot be loaded at startup, the application halts immediately
 * with a clear error message rather than starting in a broken state.
 * 
 * <p>Common causes:
 * <ul>
 *   <li>Card/noble data files (CSV) cannot be found or read</li>
 *   <li>CSV data is malformed or contains invalid values</li>
 *   <li>Required configuration properties (file paths, card counts) are missing</li>
 *   <li>Data validation fails (e.g., insufficient cards for a tier)</li>
 * </ul>
 * 
 * <p>This exception is used by:
 * <ul>
 *   <li>{@link CardDataProvider} implementations when loading fails</li>
 *   <li>{@link CardLoader} when delegating to providers</li>
 *   <li>{@link com.splendor.config.ConfigValidator} during startup validation</li>
 * </ul>
 * 
 * @see CsvCardParser
 * @see CustomCardDeckProvider
 * @see CardLoader
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
