/**
 * Enumeration of game states for proper end-game handling.
 * Manages the game flow from ongoing play through final round to finished state.
 */
package com.splendor.model;

/**
 * Represents the current phase of a Splendor game's lifecycle.
 *
 * A Splendor game progresses through distinct phases.
 */
public enum GameState {
    ONGOING("Ongoing"),
    FINAL_ROUND("Final Round"),
    FINISHED("Finished");

    private final String displayName;

    GameState(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
