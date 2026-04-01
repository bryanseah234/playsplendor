/**
 * Exception thrown when a player lacks sufficient tokens for an action.
 * Used for purchase attempts and other token-consuming actions where
 * the player doesn't have enough resources.
 */
package com.splendor.exception;

/**
 * Exception indicating a player does not have enough tokens for an action.
 * 
 * This exception is thrown when a player attempts to perform an action that
 * requires spending tokens, but their current token count is insufficient.
 * Common scenarios include:
 * <ul>
 *   <li>Attempting to purchase a card without enough tokens (even after applying discounts)</li>
 *   <li>Trying to take tokens when the bank has run out</li>
 * </ul>
 * 
 * <p>Note: This exception is different from {@link InvalidMoveException} which
 * covers rule violations. This exception specifically covers resource shortages.
 * 
 * @see com.splendor.model.Player#getTokens()
 * @see com.splendor.model.Card#getCost()
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
        super(messageFormat, args);
    }
}