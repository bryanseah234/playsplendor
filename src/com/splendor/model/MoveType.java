/**
 * Enumeration of move types available to players.
 * Defines all possible actions a player can take during their turn.
 */
package com.splendor.model;

/**
 * Enumeration of all possible move types a player can execute during their turn.
 * 
 * Each move type represents a distinct action in the Splendor game, with its own
 * validation rules, game mechanics, and effects on the game state. The move types
 * are used by the controller layer to interpret player input and by the validator
 * layer to check move legality.
 * 
 * <p>The available move types are:
 * <ul>
 *   <li>{@link #TAKE_THREE_DIFFERENT} - Take one token each of three different gem colors</li>
 *   <li>{@link #TAKE_TWO_SAME} - Take two tokens of the same gem color (requires ≥4 available)</li>
 *   <li>{@link #RESERVE_CARD} - Reserve a card (face-up or from deck) and receive a gold token</li>
 *   <li>{@link #BUY_CARD} - Purchase a card using tokens and/or discounts</li>
 *   <li>{@link #DISCARD_TOKENS} - Return excess tokens when exceeding the maximum limit</li>
 *   <li>{@link #EXIT_GAME} - Terminate the game session (console mode only)</li>
 * </ul>
 * 
 * <p>Move validation is performed by {@link com.splendor.model.validator.MoveValidator},
 * which checks preconditions specific to each move type.
 * 
 * @see Move For the complete move data including selected gems and cards
 * @see com.splendor.model.validator.MoveValidator For move legality checking
 */
public enum MoveType {
    
    /**
     * Take three different colored gem tokens.
     * Requires that at least three different gem piles have tokens available.
     */
    TAKE_THREE_DIFFERENT("Take 3 Different Gems"),
    
    /**
     * Take two gem tokens of the same color.
     * Requires that the chosen gem pile has at least 4 tokens available.
     */
    TAKE_TWO_SAME("Take 2 Same Gems"),
    
    /**
     * Reserve a development card and take a gold token.
     * Player can have maximum 3 reserved cards.
     */
    RESERVE_CARD("Reserve Card"),
    
    /**
     * Purchase a development card from the board or reserved cards.
     * Requires sufficient tokens and/or discounts.
     */
    BUY_CARD("Buy Card"),
    
    /**
     * Discard excess tokens when player has more than maximum allowed.
     * Triggered automatically when token limit is exceeded.
     */
    DISCARD_TOKENS("Discard Tokens"),

    /**
     * Exits the game cleanly.
     */
    EXIT_GAME("Exit Game");
    
    private final String displayName;
    
    /**
     * Creates a new MoveType with the specified display name.
     * 
     * @param displayName Human-readable name for the move type
     */
    MoveType(final String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the move type.
     * 
     * @return Human-readable move type name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns a string representation of the move type.
     * 
     * @return Display name of the move type
     */
    @Override
    public String toString() {
        return displayName;
    }
}