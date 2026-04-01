/**
 * Exception thrown when a player attempts an invalid move.
 * Used for move validation failures such as illegal token combinations
 * or attempting actions that violate game rules.
 */
package com.splendor.exception;

/**
 * Exception indicating a move violates the rules of Splendor.
 * 
 * This exception is thrown by {@link com.splendor.model.validator.MoveValidator}
 * when a player attempts an illegal move. Common violations include:
 * <ul>
 *   <li>Trying to take more than 3 gems</li>
 *   <li>Attempting to take 2 gems of a color with fewer than 4 available</li>
 *   <li>Reserving a card when already at maximum reserved cards (3)</li>
 *   <li>Purchasing a card that the player cannot afford</li>
 * </ul>
 * 
 * <p>The validation is performed before the move is executed, allowing the
 * controller to reject invalid moves and prompt the player to choose again.
 * 
 * @see com.splendor.model.validator.MoveValidator
 * @see com.splendor.model.Move
 */
public class InvalidMoveException extends SplendorException {
    
    @Override
	public String toString() {
		return "InvalidMoveException []";
	}

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
        super(messageFormat, args);
    }
}