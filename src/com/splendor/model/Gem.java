/**
 * Enumeration of gem types in the Splendor game.
 * Defines the five standard gem colors plus gold (wild card).
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

/**
 * Represents the different types of gems available in the game.
 * Includes five standard colors (Red, Green, Blue, White, Black) and
 * Gold which serves as a wild card token.
 */
public enum Gem {
    RED("Red"),
    GREEN("Green"), 
    BLUE("Blue"),
    WHITE("White"),
    BLACK("Black"),
    GOLD("Gold");
    
    private final String displayName;
    
    /**
     * Creates a new Gem with the specified display name.
     * 
     * @param displayName Human-readable name for the gem
     */
    Gem(final String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the gem.
     * 
     * @return Human-readable gem name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns a string representation of the gem.
     * 
     * @return Display name of the gem
     */
    @Override
    public String toString() {
        return displayName;
    }
}