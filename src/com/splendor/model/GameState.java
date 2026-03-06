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
public class GameState {
    public static final GameState ONGOING = new GameState(Phase.ONGOING);
    public static final GameState FINAL_ROUND = new GameState(Phase.FINAL_ROUND);
    public static final GameState FINISHED = new GameState(Phase.FINISHED);

    private Phase phase;

    /**
     * Creates a game state with the default phase.
     */
    public GameState() {
        this(Phase.ONGOING);
    }

    /**
     * Creates a game state with the specified phase.
     *
     * @param phase Game phase
     */
    public GameState(final Phase phase) {
        this.phase = phase == null ? Phase.ONGOING : phase;
    }

    /**
     * Gets the current game phase.
     *
     * @return Game phase
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Sets the current game phase.
     *
     * @param phase Game phase
     */
    public void setPhase(final Phase phase) {
        this.phase = phase == null ? Phase.ONGOING : phase;
    }

    public String getDisplayName() {
        return phase.displayName();
    }

    public boolean isOngoing() {
        return phase == Phase.ONGOING;
    }

    public boolean isFinalRound() {
        return phase == Phase.FINAL_ROUND;
    }

    public boolean isFinished() {
        return phase == Phase.FINISHED;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public enum Phase {
        ONGOING("Ongoing"),
        FINAL_ROUND("Final Round"),
        FINISHED("Finished");

        private final String displayName;

        Phase(final String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
