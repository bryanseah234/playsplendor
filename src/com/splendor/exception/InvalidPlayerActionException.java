/**
 * Exception thrown when a player attempts an invalid action.
 * Used for actions that are not allowed in the current game state
 * or that violate basic game mechanics.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.exception;

/**
 * Exception thrown when a player attempts an action that is not allowed
 * in the current context. This includes actions during wrong game states,
 * invalid player operations, and other contextual violations.
 */
public class InvalidPlayerActionException extends SplendorException {
    
    /**
     * Creates a new InvalidPlayerActionException with the specified message.
     * 
     * @param message Exception message explaining why the action is invalid
     */
    public InvalidPlayerActionException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new InvalidPlayerActionException with a formatted message.
     * 
     * @param messageFormat Message format string
     * @param args Arguments for the format string
     */
    public InvalidPlayerActionException(final String messageFormat, final Object... args) {
        super(String.format(messageFormat, args));
    }
}