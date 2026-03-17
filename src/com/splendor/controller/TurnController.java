/**
 * Handles turn execution and move processing.
 * Manages the execution of individual player moves and their effects.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.controller;

import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.view.IGameView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages turn execution and move processing.
 * Handles the detailed execution of each move type and its game effects.
 */
public class TurnController {

    private final Game game;
    private final IGameView gameView;

    /**
     * Creates a new TurnController with the specified game and view.
     * 
     * @param game     Current game state
     * @param gameView Game view for user interaction
     */
    public TurnController(final Game game, final IGameView gameView) {
        this.game = game;
        this.gameView = gameView;
    }

    /**
     * Executes the specified move for the player.
     * 
     * @param move   Move to execute
     * @param player Player executing the move
     * @throws SplendorException if move execution fails
     */
    public void executeMove(final Move move, final Player player) throws SplendorException {

        switch (move.getMoveType()) {
            case TAKE_THREE_DIFFERENT:
                executeTakeThreeDifferent(move, player);
                break;
            case TAKE_TWO_SAME:
                executeTakeTwoSame(move, player);
                break;
            case RESERVE_CARD:
                executeReserveCard(move, player);
                break;
            case BUY_CARD:
                executeBuyCard(move, player);
                break;
            case DISCARD_TOKENS:
                executeDiscardTokens(move, player);
                break;
            case EXIT_GAME:
                System.exit(0);
                break;
            default:
                throw new InvalidMoveException("Unknown move type: " + move.getMoveType());
        }
    }

    /**
     * Executes taking three different colored gems.
     * 
     * @param move   Move containing gem selection
     * @param player Player taking gems
     * @throws SplendorException if execution fails
     */
    private void executeTakeThreeDifferent(final Move move, final Player player) throws SplendorException {
        final Map<Gem, Integer> selectedGems = move.getSelectedGems();
        final Board board = game.getBoard();

        // Remove gems from bank
        board.removeGems(selectedGems);

        // Add gems to player
        for (final Map.Entry<Gem, Integer> entry : selectedGems.entrySet()) {
            player.addTokens(entry.getKey(), entry.getValue());
        }

        gameView.displayNotification("Took 3 different gems: " + selectedGems);
    }

    /**
     * Executes taking two gems of the same color.
     * 
     * @param move   Move containing gem selection
     * @param player Player taking gems
     * @throws SplendorException if execution fails
     */
    private void executeTakeTwoSame(final Move move, final Player player) throws SplendorException {
        final Map.Entry<Gem, Integer> entry = move.getSelectedGems().entrySet().iterator().next();
        final Gem gem = entry.getKey();
        final int quantity = entry.getValue();
        final Board board = game.getBoard();

        // Create gem map for bank operations
        final Map<Gem, Integer> gemsToTransfer = new HashMap<>();
        gemsToTransfer.put(gem, quantity);

        // Remove gems from bank
        board.removeGems(gemsToTransfer);

        // Add gems to player
        player.addTokens(gem, quantity);

        gameView.displayNotification("Took 2 " + gem + " gems");
    }

    /**
     * Executes reserving a card.
     * 
     * @param move   Move containing card selection
     * @param player Player reserving the card
     * @throws SplendorException if execution fails
     */
    private void executeReserveCard(final Move move, final Player player) throws SplendorException {
        final Board board = game.getBoard();

        final Card cardToReserve = findCardToReserve(move, board);
        if (cardToReserve == null) {
            throw new InvalidMoveException("Selected card not found");
        }

        if (move.hasDeckSelection()) {
            player.addReservedCard(cardToReserve);
        } else {
            board.removeAvailableCard(cardToReserve.getTier(), cardToReserve);
            board.drawCard(cardToReserve.getTier());
            player.addReservedCard(cardToReserve);
        }

        // Take gold token if available
        final int goldAvailable = board.getGemCount(Gem.GOLD);
        if (goldAvailable > 0) {
            board.removeGems(Map.of(Gem.GOLD, 1));
            player.addTokens(Gem.GOLD, 1);
            gameView.displayNotification("Reserved card and took 1 gold token");
        } else {
            gameView.displayNotification("Reserved card (no gold tokens available)");
        }
    }

    /**
     * Executes buying a card.
     * 
     * @param move   Move containing card selection
     * @param player Player buying the card
     * @throws SplendorException if execution fails
     */
    private void executeBuyCard(final Move move, final Player player) throws SplendorException {
        final Board board = game.getBoard();

        // Get the card to buy
        final Card cardToBuy = findCardToBuy(move, player, board);
        if (cardToBuy == null) {
            throw new InvalidMoveException("Selected card not found or not available");
        }

        // Calculate effective cost after discounts
        final Map<Gem, Integer> effectiveCost = calculateEffectiveCost(player, cardToBuy);

        // Process payment
        processCardPayment(player, board, effectiveCost);

        // Transfer card to player
        if (move.isReservedCard()) {
            player.removeReservedCard(cardToBuy);
        } else {
            board.removeAvailableCard(cardToBuy.getTier(), cardToBuy);
            board.drawCard(cardToBuy.getTier());
        }

        player.addPurchasedCard(cardToBuy);

        gameView.displayNotification("Purchased card: " + cardToBuy);
    }

    /**
     * Executes discarding excess tokens.
     * 
     * @param move   Move containing tokens to discard
     * @param player Player discarding tokens
     * @throws SplendorException if execution fails
     */
    private void executeDiscardTokens(final Move move, final Player player) throws SplendorException {
        final Map<Gem, Integer> tokensToDiscard = move.getSelectedGems();
        final Board board = game.getBoard();

        // Remove tokens from player
        for (final Map.Entry<Gem, Integer> entry : tokensToDiscard.entrySet()) {
            player.removeTokens(entry.getKey(), entry.getValue());
        }

        // Add tokens back to bank
        board.addGems(tokensToDiscard);

        gameView.displayNotification("Discarded tokens: " + tokensToDiscard);
    }

    /**
     * Finds the card to reserve based on the move.
     * 
     * @param move  Move containing card selection
     * @param board Game board
     * @return Card to reserve or null if not found
     */
    private Card findCardToReserve(final Move move, final Board board) {
        if (move.hasDeckSelection()) {
            return board.drawBlindCard(move.getDeckTier());
        }
        return findAvailableCardById(board, move.getCardId());
    }

    private Card findAvailableCardById(final Board board, final int cardId) {
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card availableCard : board.getAvailableCards(tier)) {
                if (availableCard.getId() == cardId) {
                    return availableCard;
                }
            }
        }
        return null;
    }

    /**
     * Finds the card to buy based on the move.
     * 
     * @param move   Move containing card selection
     * @param player Player buying the card
     * @param board  Game board
     * @return Card to buy or null if not found
     */
    private Card findCardToBuy(final Move move, final Player player, final Board board) {
        if (move.isReservedCard()) {
            // Find in player's reserved cards
            for (final Card reservedCard : player.getReservedCards()) {
                if (reservedCard.getId() == move.getCardId()) {
                    return reservedCard;
                }
            }
        } else {
            // Find in available cards
            for (int tier = 1; tier <= 3; tier++) {
                for (final Card availableCard : board.getAvailableCards(tier)) {
                    if (availableCard.getId() == move.getCardId()) {
                        return availableCard;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Calculates the effective cost of a card after applying discounts.
     * 
     * @param player Player buying the card
     * @param card   Card to buy
     * @return Map of gems and quantities to pay
     */
    private Map<Gem, Integer> calculateEffectiveCost(final Player player, final Card card) {
        final Map<Gem, Integer> discounts = player.getGemDiscounts();
        final Map<Gem, Integer> effectiveCost = new HashMap<>();

        for (final Map.Entry<Gem, Integer> costEntry : card.getCost().entrySet()) {
            final Gem gem = costEntry.getKey();
            final int baseCost = costEntry.getValue();
            final int discount = discounts.getOrDefault(gem, 0);

            effectiveCost.put(gem, Math.max(0, baseCost - discount));
        }

        return effectiveCost;
    }

    /**
     * Processes payment for a card purchase.
     * 
     * @param player        Player making the payment
     * @param board         Game board
     * @param effectiveCost Effective cost after discounts
     * @throws InsufficientTokensException if player lacks required tokens
     */
    private void processCardPayment(final Player player, final Board board, final Map<Gem, Integer> effectiveCost)
            throws InsufficientTokensException {

        final Map<Gem, Integer> tokensToRemove = new HashMap<>();
        int totalGoldUsed = 0;

        for (final Map.Entry<Gem, Integer> costEntry : effectiveCost.entrySet()) {
            final Gem gem = costEntry.getKey();
            int remainingCost = costEntry.getValue();

            final int playerTokens = player.getTokenCount(gem);
            final int tokensToUse = Math.min(playerTokens, remainingCost);

            if (tokensToUse > 0) {
                tokensToRemove.put(gem, tokensToUse);
                remainingCost -= tokensToUse;
            }

            if (remainingCost > 0) {
                final int playerGold = player.getTokenCount(Gem.GOLD);
                final int goldAvailable = playerGold - totalGoldUsed;
                final int goldToUse = Math.min(goldAvailable, remainingCost);

                if (goldToUse > 0) {
                    totalGoldUsed += goldToUse;
                    remainingCost -= goldToUse;
                }
            }

            if (remainingCost > 0) {
                throw new InsufficientTokensException("Insufficient tokens to buy card. Need %d more %s gems",
                        remainingCost, gem);
            }
        }

        if (totalGoldUsed > 0) {
            tokensToRemove.put(Gem.GOLD, totalGoldUsed);
        }

        final Map<Gem, Integer> tokensToAdd = new HashMap<>();
        for (final Map.Entry<Gem, Integer> entry : tokensToRemove.entrySet()) {
            player.removeTokens(entry.getKey(), entry.getValue());
            tokensToAdd.put(entry.getKey(), entry.getValue());
        }

        board.addGems(tokensToAdd);
    }
}
