package com.splendor.exception;

/**
 * Exception indicating a failure during card or noble data loading operations.
 *
 * <p>This exception is part of the fail-fast initialization strategy: if game data
 * (cards, nobles) cannot be loaded at startup, the application halts immediately
 * with a clear error message rather than starting in a broken state.
 *
 * <p>Common causes:
 * <ul>
 *   <li>CSV data file cannot be found or read</li>
 *   <li>CSV data is malformed or contains invalid values</li>
 *   <li>Required configuration properties (file paths, card counts) are missing</li>
 * </ul>
 *
 * @see com.splendor.data.CsvCardParser
 * @see com.splendor.data.CardLoader
 */
public class DataLoadException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataLoadException(final String message) {
        super(message);
    }

    public DataLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
