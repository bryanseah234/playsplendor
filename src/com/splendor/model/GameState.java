/**
 * Enumeration of game states for proper end-game handling.
 * Manages the game flow from ongoing play through final round to finished state.
 */
package com.splendor.model;

/**
 * Represents the current phase of a Splendor game's lifecycle.
 * 
 * A Splendor game progresses through three distinct phases:
 * <ol>
 *   <li>{@link Phase#ONGOING} - Normal gameplay; no player has reached the winning point threshold</li>
 *   <li>{@link Phase#FINAL_ROUND} - Triggered when a player reaches the winning threshold; all other
 *       players get one final turn to try to win</li>
 *   <li>{@link Phase#FINISHED} - All players have completed their final turn; the winner is determined</li>
 * </ol>
 * 
 * <p>This class uses the Singleton pattern with predefined instances ({@link #ONGOING},
 * {@link #FINAL_ROUND}, {@link #FINISHED}) to ensure consistent state objects throughout
 * the application.
 * 
 * <p>The state transitions are strictly unidirectional:
 * ONGOING → FINAL_ROUND → FINISHED
 * 
 * <p>State transition validation is handled by {@link com.splendor.model.validator.GameRuleValidator}.
 * 
 * @see Game#isGameFinished() For checking if the game has ended
 * @see com.splendor.model.validator.GameRuleValidator#validateStateTransition(GameState, GameState)
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

    /**
     * Returns the human-readable label for the current phase (e.g. "Final Round").
     *
     * @return Display name of the active Phase.
     */
    public String getDisplayName() {
        return phase.displayName();
    }

    /**
     * Returns true when the game is in normal play (no player has yet hit the winning threshold).
     *
     * @return true if the phase is ONGOING.
     */
    public boolean isOngoing() {
        return phase == Phase.ONGOING;
    }

    /**
     * Returns true when the game is in its concluding round (a player crossed the winning score
     * and all remaining players are taking their last turn).
     *
     * @return true if the phase is FINAL_ROUND.
     */
    public boolean isFinalRound() {
        return phase == Phase.FINAL_ROUND;
    }

    /**
     * Returns true when the game has fully concluded and a winner has been determined.
     *
     * @return true if the phase is FINISHED.
     */
    public boolean isFinished() {
        return phase == Phase.FINISHED;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * The three lifecycle phases a Splendor game passes through:
     * ONGOING → FINAL_ROUND → FINISHED.
     */
    public enum Phase {
        /** Normal game play; no player has yet reached the winning-points threshold. */
        ONGOING("Ongoing"),

        /** A player has crossed the winning threshold; all remaining players get one final turn. */
        FINAL_ROUND("Final Round"),

        /** All players have completed the final round; the winner has been determined. */
        FINISHED("Finished");

        private final String displayName; // human-readable label used in UI output

        Phase(final String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the human-readable name of this phase.
         *
         * @return Display name string.
         */
        public String displayName() {
            return displayName;
        }
    }
}
