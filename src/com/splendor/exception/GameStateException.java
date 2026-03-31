/**
 * Exception thrown when an invalid game state transition is attempted.
 * Used for state management errors such as attempting to start a game
 * that's already in progress or ending a game that hasn't started.
 */
package com.splendor.exception;

/**
 * Exception indicating an operation was attempted in an invalid game state.
 * 
 * This exception is thrown when game rules are violated due to the current
 * state of the game, such as:
 * <ul>
 *   <li>Attempting to take a turn when it's not the player's turn</li>
 *   <li>Trying to transition from a FINISHED game state</li>
 *   <li>Assigning a noble that is not available</li>
 *   <li>Starting a game with invalid parameters (wrong player count, etc.)</li>
 * </ul>
 * 
 * <p>The exception supports formatted messages for detailed error descriptions.
 * 
 * <p>Validation is performed by {@link com.splendor.model.validator.GameRuleValidator}
 * and {@link com.splendor.model.validator.MoveValidator}.
 * 
 * @see com.splendor.model.validator.GameRuleValidator
 * @see com.splendor.model.validator.MoveValidator
 * @see com.splendor.model.GameState
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