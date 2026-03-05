/**
 * Exception thrown when a player lacks sufficient tokens for an action.
 * Used for purchase attempts and other token-consuming actions where
 * the player doesn't have enough resources.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.exception;

/**
 * Exception thrown when a player attempts an action requiring more tokens
 * than they currently possess. This includes card purchases and other
 * token-consuming operations.
 */
public class InsufficientTokensException extends SplendorException {
    
    /**
     * Creates a new InsufficientTokensException with the specified message.
     * 
     * @param message Exception message explaining the token shortage
     */
    public InsufficientTokensException(final String message) {
        super(message);
    }
    
    /**
     * Creates a new InsufficientTokensException with a formatted message.
     * 
     * @param messageFormat Message format string
     * @param args Arguments for the format string
     */
    public InsufficientTokensException(final String messageFormat, final Object... args) {
        super(String.format(messageFormat, args));
    }
}