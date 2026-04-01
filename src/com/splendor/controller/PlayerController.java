/**
 * Manages player-specific operations and state updates.
 * Handles player actions such as noble visits and token management.
 * 
 */
package com.splendor.controller;

import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
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
    private final MoveValidator moveValidator;

    /**
     * Creates a new PlayerController with the specified game and view.
     *
     * @param game          Current game state
     * @param gameView      Game view for user interaction
     * @param moveValidator Validator used for move and reserve checks
     */
    public PlayerController(final Game game, final IGameView gameView, final MoveValidator moveValidator) {
        this.game = game;
        this.gameView = gameView;
        this.gameRuleValidator = new GameRuleValidator();
        this.moveValidator = moveValidator;
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
            // Debug: Show why no nobles visited
            gameView.displayNotification("No nobles interested in visiting " + player.getName() 
                + " - need more discounts from purchased cards (current discounts: " 
                + formatDiscounts(playerDiscounts) + ")");
            return;
        }
        
        final Noble selectedNoble;
        if (qualifyingNobles.size() == 1) {
            selectedNoble = qualifyingNobles.get(0);
        } else {
            selectedNoble = gameView.promptForNobleChoice(player, qualifyingNobles);
        }
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
     * Formats a gem-discount map into a human-readable "GEM=count" string for logging.
     * Entries with a count of zero are omitted. Returns "None" if no discounts are active.
     *
     * @param discounts Map of gem type to discount count from purchased cards.
     * @return Comma-separated discount summary, or "None" if all counts are zero.
     */
    private String formatDiscounts(final Map<Gem, Integer> discounts) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Gem, Integer> entry : discounts.entrySet()) {
            if (entry.getValue() > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.length() == 0 ? "None" : sb.toString();
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
        int actualDiscard = 0;
        for (final int quantity : tokensToDiscard.values()) {
            actualDiscard += quantity;
        }
        
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
}
