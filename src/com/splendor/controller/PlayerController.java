/**
 * Manages player-specific operations and state updates.
 * Handles player actions such as noble visits and token management.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.controller;

import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.validator.GameRuleValidator;
import com.splendor.view.IGameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages player-specific operations and state updates.
 * Handles noble assignment, token management, and player state validation.
 */
public class PlayerController {
    
    private final Game game;
    private final IGameView gameView;
    private final GameRuleValidator gameRuleValidator;
    
    /**
     * Creates a new PlayerController with the specified game and view.
     * 
     * @param game Current game state
     * @param gameView Game view for user interaction
     */
    public PlayerController(final Game game, final IGameView gameView) {
        this.game = game;
        this.gameView = gameView;
        this.gameRuleValidator = new GameRuleValidator();
    }
    
    /**
     * Checks if any nobles can visit the player after a card purchase.
     * Automatically assigns qualifying nobles.
     * 
     * @param player Player to check for noble visits
     * @throws SplendorException if noble assignment fails
     */
    public void checkNobleVisits(final Player player) throws SplendorException {
        // Guard clause: Check if player has any nobles available
        if (game.getBoard().getAvailableNobles().isEmpty()) {
            return;
        }
        
        // Get player's gem discounts (from purchased cards)
        final Map<Gem, Integer> playerDiscounts = player.getGemDiscounts();
        final List<Noble> qualifyingNobles = new ArrayList<>();
        for (final Noble noble : game.getBoard().getAvailableNobles()) {
            if (noble.requirementsMet(playerDiscounts)) {
                qualifyingNobles.add(noble);
            }
        }
        
        if (qualifyingNobles.isEmpty()) {
            return;
        }
        
        final Noble selectedNoble = qualifyingNobles.size() == 1
            ? qualifyingNobles.get(0)
            : gameView.promptForNobleChoice(player, qualifyingNobles);
        assignNobleToPlayer(player, selectedNoble);
    }
    
    /**
     * Assigns a noble to the player.
     * 
     * @param player Player receiving the noble
     * @param noble Noble to assign
     * @throws SplendorException if assignment fails
     */
    private void assignNobleToPlayer(final Player player, final Noble noble) throws SplendorException {
        try {
            // Validate noble assignment
            gameRuleValidator.validateNobleAssignment(game, player, noble);
            
            // Remove noble from board
            final boolean nobleRemoved = game.getBoard().removeAvailableNoble(noble);
            if (!nobleRemoved) {
                throw new GameStateException("Failed to remove noble %d from board", noble.getId());
            }
            
            // Add noble to player
            player.addNoble(noble);
            
            // Notify player
            gameView.displayNotification(String.format("%s has been visited by Noble %d and gained %d points!",
                player.getName(), noble.getId(), noble.getPoints()));
            
        } catch (final GameStateException e) {
            throw new SplendorException("Noble assignment failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes token discard for a player who exceeds the token limit.
     * 
     * @param player Player discarding tokens
     * @param discardMove Move containing tokens to discard
     * @throws SplendorException if discard fails
     */
    public void executeTokenDiscard(final Player player, final Move discardMove) throws SplendorException {
        final Map<Gem, Integer> tokensToDiscard = discardMove.getSelectedGems();
        final Board board = game.getBoard();
        
        // Guard clause: Validate discard move
        validateTokenDiscard(player, tokensToDiscard);
        
        try {
            // Remove tokens from player
            for (final Map.Entry<Gem, Integer> entry : tokensToDiscard.entrySet()) {
                final Gem gem = entry.getKey();
                final int quantity = entry.getValue();
                player.removeTokens(gem, quantity);
            }
            
            // Add tokens back to board
            board.addGems(tokensToDiscard);
            
            // Notify player
            gameView.displayNotification(String.format("%s discarded tokens: %s",
                player.getName(), tokensToDiscard));
            
        } catch (final Exception e) {
            throw new SplendorException("Token discard failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates that a token discard is legal.
     * 
     * @param player Player discarding tokens
     * @param tokensToDiscard Map of gems and quantities to discard
     * @throws InvalidPlayerActionException if discard is invalid
     */
    private void validateTokenDiscard(final Player player, final Map<Gem, Integer> tokensToDiscard) 
            throws InvalidPlayerActionException {
        
        // Guard clause: Check if player exceeds token limit
        if (player.getTotalTokenCount() <= game.getMaxTokens()) {
            throw new InvalidPlayerActionException("Player does not exceed token limit");
        }
        
        // Calculate required discard count
        final int requiredDiscard = player.getTotalTokenCount() - game.getMaxTokens();
        final int actualDiscard = tokensToDiscard.values().stream().mapToInt(Integer::intValue).sum();
        
        // Guard clause: Check discard quantity
        if (actualDiscard != requiredDiscard) {
            throw new InvalidPlayerActionException("Must discard exactly %d tokens (attempting to discard %d)",
                requiredDiscard, actualDiscard);
        }
        
        // Validate player has the tokens being discarded
        for (final Map.Entry<Gem, Integer> entry : tokensToDiscard.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();
            final int playerCount = player.getTokenCount(gem);
            
            if (playerCount < quantity) {
                throw new InvalidPlayerActionException("Player does not have %d %s tokens (has %d)",
                    quantity, gem, playerCount);
            }
        }
    }
    
    /**
     * Gets a summary of the player's current state.
     * 
     * @param player Player to summarize
     * @return Map of player state information
     */
    public Map<String, Object> getPlayerStateSummary(final Player player) {
        final Map<String, Object> summary = new HashMap<>();
        
        summary.put("name", player.getName());
        summary.put("points", player.getTotalPoints());
        summary.put("tokens", player.getTokens());
        summary.put("totalTokens", player.getTotalTokenCount());
        summary.put("purchasedCards", player.getPurchasedCards().size());
        summary.put("reservedCards", player.getReservedCards().size());
        summary.put("nobles", player.getNobles().size());
        summary.put("discounts", player.getGemDiscounts());
        
        return summary;
    }
    
    /**
     * Checks if a player can take a specific action.
     * 
     * @param player Player to check
     * @param actionType Type of action
     * @return true if action is possible, false otherwise
     */
    public boolean canPlayerTakeAction(final Player player, final String actionType) {
        switch (actionType.toLowerCase()) {
            case "reserve":
                return player.canReserveCard();
            case "buy":
                return !player.getReservedCards().isEmpty() || hasAvailableCards();
            case "discard":
                return player.getTotalTokenCount() > game.getMaxTokens();
            default:
                return true;
        }
    }
    
    /**
     * Checks if there are any available cards on the board.
     * 
     * @return true if cards are available, false otherwise
     */
    private boolean hasAvailableCards() {
        final Board board = game.getBoard();
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
