/**
 * Base exception class for all Splendor-related exceptions.
 * Provides a common parent for all custom exceptions in the application.
 */
package com.splendor.exception;

/**
 * Root exception class for all Splendor game-specific exceptions.
 * 
 * This exception hierarchy enables consistent error handling throughout the application.
 * All custom exceptions extend this base class, allowing the application to catch
 * Splendor-specific errors separately from system exceptions.
 * 
 * <p>Exception hierarchy:
 * <ul>
 *   <li>{@link SplendorException} (root)</li>
 *   <li>{@link GameStateException} - Invalid game state or transition</li>
 *   <li>{@link InsufficientTokensException} - Not enough tokens for an action</li>
 *   <li>{@link InvalidMoveException} - Illegal move attempted</li>
 *   <li>{@link InvalidPlayerActionException} - Invalid player action</li>
 *   <li>{@link com.splendor.network.NetworkException} - Network communication failure</li>
 *   <li>{@link com.splendor.config.ConfigException} - Configuration loading error</li>
 *   <li>{@link com.splendor.data.DataLoadException} - Data loading failure</li>
 * </ul>
 * 
 * <p>The exception is caught at the application boundary (Main class) and converted
 * to user-friendly error messages with appropriate exit codes.
 * 
 * @see GameStateException
 * @see InsufficientTokensException
 * @see InvalidMoveException
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