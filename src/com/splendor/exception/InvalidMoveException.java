/**
 * Exception thrown when a player attempts an invalid move.
 * Used for move validation failures such as illegal token combinations
 * or attempting actions that violate game rules.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.exception;

/**
 * Exception thrown when a player attempts a move that violates game rules.
 * This includes invalid token combinations, illegal card purchases,
 * and other rule violations.
 */
public class InvalidMoveException extends SplendorException {
    
    /**
     * Creates a new InvalidMoveException with the specified message.
     * 
     * @param message Exception message explaining why the move is invalid
     */
    public InvalidMoveException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new InvalidMoveException with a formatted message.
     * 
     * @param messageFormat Message format string
     * @param args Arguments for the format string
     */
    public InvalidMoveException(final String messageFormat, final Object... args) {
        super(String.format(messageFormat, args));
    }
}