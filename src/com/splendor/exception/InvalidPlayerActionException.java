/**
 * Exception thrown when a player attempts an invalid action.
 * Used for actions that are not allowed in the current game state
 * or that violate basic game mechanics.
 */
package com.splendor.exception;

/**
 * Exception indicating a player action is not permitted in the current context.
 * 
 * This exception covers actions that are invalid due to contextual factors
 * rather than specific move rules. Examples include:
 * <ul>
 *   <li>Attempting to take a turn when the game has finished</li>
 *   <li>A player trying to perform an action during another player's turn</li>
 *   <li>Performing an action that is disabled for the current game mode</li>
 * </ul>
 * 
 * <p>This exception is distinct from {@link InvalidMoveException} which covers
 * specific move rule violations, and {@link GameStateException} which covers
 * state transition errors.
 * 
 * @see InvalidMoveException For move-specific rule violations
 * @see GameStateException For game state transition errors
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
        super(messageFormat, args);
    }
}