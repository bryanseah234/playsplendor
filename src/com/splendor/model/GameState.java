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
    /** The game is currently in normal play phase. */
    ONGOING("Ongoing"),
    /** A player has reached the point threshold, and the last round is concluding. */
    FINAL_ROUND("Final Round"),
    /** The game has concluded and a winner has been decided. */
    FINISHED("Finished");

    private final String displayName;

    GameState(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name of the state.
     *
     * @return The display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
