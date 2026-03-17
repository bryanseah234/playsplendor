/**
 * Enumeration of move types available to players.
 * Defines all possible actions a player can take during their turn.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

/**
 * Represents the different types of moves a player can make.
 * Each move type has specific validation rules and game mechanics.
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