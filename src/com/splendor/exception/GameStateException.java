/**
 * Exception thrown when an invalid game state transition is attempted.
 * Used for state management errors such as attempting to start a game
 * that's already in progress or ending a game that hasn't started.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.exception;

/**
 * Exception thrown when an operation is attempted in an invalid game state.
 * This includes state transition violations and operations that are not
 * allowed in the current game state.
 */
public class GameStateException extends SplendorException {
    
    /**
     * Creates a new GameStateException with the specified message.
     * 
     * @param message Exception message explaining the state violation
     */
    public GameStateException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new GameStateException with a formatted message.
     * 
     * @param messageFormat Message format string
     * @param args Arguments for the format string
     */
    public GameStateException(final String messageFormat, final Object... args) {
        super(String.format(messageFormat, args));
    }
}