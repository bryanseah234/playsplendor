/**
 * Manages game flow and state transitions.
 * Coordinates high-level game progression and win condition checking.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.controller;

import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.validator.GameRuleValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages game flow and state transitions.
 * Handles win condition checking, final round management, and game completion.
 */
public class GameFlowController {
    
    private final Game game;
    private final GameRuleValidator gameRuleValidator;
    
    /**
     * Creates a new GameFlowController with the specified game.
     * 
     * @param game Current game state
     */
    public GameFlowController(final Game game) {
        this.game = game;
        this.gameRuleValidator = new GameRuleValidator();
    }
    
    /**
     * Checks if the game should transition to the final round.
     * Triggered when a player reaches the winning score.
     * 
     * @return true if final round should start, false otherwise
     */
    public boolean shouldStartFinalRound() {
        // Guard clause: Already in final round or finished
        if (!game.getCurrentState().isOngoing()) {
            return false;
        }
        
        // Check if any player has reached winning score
        return hasPlayerReachedWinningScore();
    }
    
    /**
     * Starts the final round of the game.
     * All remaining players get one final turn.
     * 
     * @throws GameStateException if final round cannot be started
     */
    public void startFinalRound() throws GameStateException {
        // Guard clause: Validate state transition
        if (!shouldStartFinalRound()) {
            throw new GameStateException("Cannot start final round - no player has reached winning score");
        }
        
        // Mark the current player as the final round starting point
        // All players will get one more turn after this player
        markFinalRoundStart();
    }
    
    /**
     * Checks if the final round is complete.
     * 
     * @return true if final round is complete, false otherwise
     */
    public boolean isFinalRoundComplete() {
        if (!game.getCurrentState().isFinalRound()) {
            return false;
        }
        
        // Check if we've completed the full round
        return hasCompletedFinalRound();
    }
    
    /**
     * Completes the game and determines the winner.
     * 
     * @return Winning player
     * @throws GameStateException if game cannot be completed
     */
    public Player completeGame() throws GameStateException {
        // Guard clause: Check if game can be completed
        if (game.getCurrentState().isFinished()) {
            throw new GameStateException("Game is already finished");
        }
        
        if (!game.getCurrentState().isFinalRound() && !hasPlayerReachedWinningScore()) {
            throw new GameStateException("Cannot complete game - no player has reached winning score");
        }
        
        // Determine winner based on points and tie-breakers
        final Player winner = determineWinner();
        
        // Transition to finished state
        transitionToFinishedState();
        
        return winner;
    }
    
    /**
     * Gets the current game status summary.
     * 
     * @return Map containing game status information
     */
    public Map<String, Object> getGameStatus() {
        final Map<String, Object> status = new HashMap<>();
        
        status.put("currentState", game.getCurrentState());
        status.put("winningPoints", game.getWinningPoints());
        status.put("isFinalRound", game.isFinalRound());
        status.put("hasWinner", game.getWinner() != null);
        
        if (game.getWinner() != null) {
            status.put("winner", game.getWinner().getName());
            status.put("winningScore", game.getWinner().getTotalPoints());
        }
        
        // Add player scores
        final Map<String, Integer> playerScores = new HashMap<>();
        for (final Player player : game.getPlayers()) {
            playerScores.put(player.getName(), player.getTotalPoints());
        }
        status.put("playerScores", playerScores);
        
        return status;
    }
    
    /**
     * Checks if any player has reached the winning score.
     * 
     * @return true if a player has won, false otherwise
     */
    private boolean hasPlayerReachedWinningScore() {
        for (final Player player : game.getPlayers()) {
            if (player.getTotalPoints() >= game.getWinningPoints()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Marks the start of the final round.
     */
    private void markFinalRoundStart() {
        // Implementation would track the starting player for final round
        // This is a simplified version
    }
    
    /**
     * Checks if the final round has been completed.
     * 
     * @return true if final round is complete, false otherwise
     */
    private boolean hasCompletedFinalRound() {
        // Implementation would check if all players have had their final turn
        // This is a simplified version
        return true;
    }
    
    /**
     * Determines the winner based on points and tie-breaker rules.
     * 
     * @return Winning player
     * @throws GameStateException if no winner can be determined
     */
    private Player determineWinner() throws GameStateException {
        final List<Player> candidates = new ArrayList<>();
        int highestScore = 0;
        
        // Find players with highest score
        for (final Player player : game.getPlayers()) {
            final int score = player.getTotalPoints();
            
            if (score > highestScore) {
                highestScore = score;
                candidates.clear();
                candidates.add(player);
            } else if (score == highestScore) {
                candidates.add(player);
            }
        }
        
        // Guard clause: No players with points
        if (candidates.isEmpty()) {
            throw new GameStateException("No players found with scores");
        }
        
        // If single candidate, they win
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        
        // Tie-breaker: fewest purchased cards
        return breakTieWithFewestCards(candidates);
    }
    
    /**
     * Breaks a tie by selecting the player with the fewest purchased cards.
     * 
     * @param tiedPlayers Players tied for highest score
     * @return Winner based on tie-breaker
     * @throws GameStateException if tie cannot be broken
     */
    private Player breakTieWithFewestCards(final List<Player> tiedPlayers) throws GameStateException {
        Player winner = null;
        int fewestCards = Integer.MAX_VALUE;
        
        for (final Player player : tiedPlayers) {
            final int cardCount = player.getPurchasedCards().size();
            
            if (cardCount < fewestCards) {
                fewestCards = cardCount;
                winner = player;
            }
        }
        
        if (winner == null) {
            throw new GameStateException("Could not break tie - no winner determined");
        }
        
        return winner;
    }
    
    /**
     * Transitions the game to the finished state.
     * 
     * @throws GameStateException if transition fails
     */
    private void transitionToFinishedState() throws GameStateException {
        try {
            gameRuleValidator.validateStateTransition(game.getCurrentState(), GameState.FINISHED);
            // In a real implementation, this would update the game's internal state
        } catch (final GameStateException e) {
            throw new GameStateException("Failed to transition to finished state: " + e.getMessage(), e);
        }
    }
}
