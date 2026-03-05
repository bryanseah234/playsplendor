/**
 * Enumeration of game states for proper end-game handling.
 * Manages the game flow from ongoing play through final round to finished state.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

/**
 * Represents the current state of the game.
 * Used to manage game flow and ensure proper end-game handling.
 */
public enum GameState {
    
    /**
     * Normal gameplay state. Players can take turns normally.
     */
    ONGOING("Ongoing"),
    
    /**
     * Final round state. A player has reached the winning score,
     * but all players must complete their turns.
     */
    FINAL_ROUND("Final Round"),
    
    /**
     * Game finished state. All turns completed, winner determined.
     */
    FINISHED("Finished");
    
    private final String displayName;
    
    /**
     * Creates a new GameState with the specified display name.
     * 
     * @param displayName Human-readable name for the game state
     */
    GameState(final String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the game state.
     * 
     * @return Human-readable game state name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns a string representation of the game state.
     * 
     * @return Display name of the game state
     */
    @Override
    public String toString() {
        return displayName;
    }
}