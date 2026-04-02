// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Validates game rules and state transitions.
 * Ensures game mechanics are properly enforced and state transitions are valid.
 * 
 */
package com.splendor.model.validator;

import com.splendor.exception.GameStateException;
import com.splendor.model.Game;
import com.splendor.model.Noble;
import com.splendor.model.Player;

/**
 * Validates game-level rules and state transitions.
 * Ensures proper game flow and enforces high-level game mechanics.
 */
public class GameRuleValidator {


    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    
    /**
     * Default constructor for GameRuleValidator.
     */
    public GameRuleValidator() {}
    
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
