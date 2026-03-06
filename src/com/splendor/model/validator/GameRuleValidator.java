/**
 * Validates game rules and state transitions.
 * Ensures game mechanics are properly enforced and state transitions are valid.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model.validator;

import com.splendor.exception.GameStateException;
import com.splendor.model.*;

/**
 * Validates game-level rules and state transitions.
 * Ensures proper game flow and enforces high-level game mechanics.
 */
public class GameRuleValidator {
    
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    
    /**
     * Validates that a game can be started with the specified parameters.
     * 
     * @param playerCount Number of players
     * @param winningPoints Points required to win
     * @param maxTokens Maximum tokens per player
     * @throws GameStateException if parameters are invalid
     */
    public void validateGameStart(final int playerCount, final int winningPoints, final int maxTokens) 
            throws GameStateException {
        
        // Guard clause: Check player count
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new GameStateException("Player count must be between %d and %d (got %d)",
                MIN_PLAYERS, MAX_PLAYERS, playerCount);
        }
        
        // Guard clause: Check winning points
        if (winningPoints <= 0) {
            throw new GameStateException("Winning points must be positive (got %d)", winningPoints);
        }
        
        // Guard clause: Check max tokens
        if (maxTokens <= 0) {
            throw new GameStateException("Maximum tokens must be positive (got %d)", maxTokens);
        }
    }
    
    /**
     * Validates a game state transition.
     * 
     * @param currentState Current game state
     * @param targetState Target game state
     * @throws GameStateException if transition is invalid
     */
    public void validateStateTransition(final GameState currentState, final GameState targetState) 
            throws GameStateException {
        
        // Guard clause: Same state transition
        if (currentState.getPhase() == targetState.getPhase()) {
            return; // Same state is always valid
        }

        switch (currentState.getPhase()) {
            case ONGOING:
                validateFromOngoing(targetState);
                break;
            case FINAL_ROUND:
                validateFromFinalRound(targetState);
                break;
            case FINISHED:
                throw new GameStateException("Cannot transition from FINISHED state");
            default:
                throw new GameStateException("Unknown game state: " + currentState);
        }
    }
    
    /**
     * Validates transitions from ONGOING state.
     * 
     * @param targetState Target state
     * @throws GameStateException if transition is invalid
     */
    private void validateFromOngoing(final GameState targetState) throws GameStateException {
        // From ONGOING, can only go to FINAL_ROUND or FINISHED
        if (targetState.getPhase() != GameState.Phase.FINAL_ROUND && targetState.getPhase() != GameState.Phase.FINISHED) {
            throw new GameStateException("Cannot transition from ONGOING to %s", targetState);
        }
    }
    
    /**
     * Validates transitions from FINAL_ROUND state.
     * 
     * @param targetState Target state
     * @throws GameStateException if transition is invalid
     */
    private void validateFromFinalRound(final GameState targetState) throws GameStateException {
        // From FINAL_ROUND, can only go to FINISHED
        if (targetState.getPhase() != GameState.Phase.FINISHED) {
            throw new GameStateException("Cannot transition from FINAL_ROUND to %s", targetState);
        }
    }
    
    /**
     * Validates that a player can take their turn.
     * 
     * @param game Current game state
     * @param player Player attempting to take turn
     * @throws GameStateException if player cannot take turn
     */
    public void validatePlayerTurn(final Game game, final Player player) throws GameStateException {
        
        // Guard clause: Check if game is finished
        if (game.isGameFinished()) {
            throw new GameStateException("Cannot take turn - game is finished");
        }
        
        // Guard clause: Check if it's the player's turn
        if (game.getCurrentPlayer() != player) {
            throw new GameStateException("Not %s's turn", player.getName());
        }
    }
    
    /**
     * Validates that noble assignment is allowed.
     * 
     * @param game Current game state
     * @param player Player being assigned the noble
     * @param noble Noble to assign
     * @throws GameStateException if assignment is invalid
     */
    public void validateNobleAssignment(final Game game, final Player player, final Noble noble) 
            throws GameStateException {
        
        // Guard clause: Check if noble is available
        if (!game.getBoard().getAvailableNobles().contains(noble)) {
            throw new GameStateException("Noble %d is not available", noble.getId());
        }
        
        // Guard clause: Check if player meets requirements
        if (!noble.requirementsMet(player.getGemDiscounts())) {
            throw new GameStateException("Player %s does not meet noble requirements", player.getName());
        }
    }
}
