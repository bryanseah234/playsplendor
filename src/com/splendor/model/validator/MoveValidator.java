/**
 * Validates player moves according to game rules.
 * Centralized validation logic for all move types to ensure rule compliance.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model.validator;

import com.splendor.model.*;
import com.splendor.exception.InvalidMoveException;
import com.splendor.exception.InsufficientTokensException;

import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

import java.util.HashMap;

/**
 * Validates player moves to ensure they comply with game rules.
 * Provides centralized validation logic for all move types.
 */
public class MoveValidator {

    private static final int MAX_RESERVED_CARDS = 3;
    private static final int MIN_GEMS_FOR_TWO_SAME = 4;
    private static final int MAX_GEMS_PER_TURN = 3;

    /**
     * Validates if a player can perform the specified move.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException        if the move is invalid
     * @throws InsufficientTokensException if player lacks required tokens
     */
    public void validateMove(final Move move, final Player player, final Game game)
            throws InvalidMoveException, InsufficientTokensException {

        // Guard clause: Check if game is finished
        if (game.isGameFinished()) {
            throw new InvalidMoveException("Cannot make moves - game is finished");
        }

        // Validate based on move type
        switch (move.getMoveType()) {
            case TAKE_THREE_DIFFERENT:
                validateTakeThreeDifferent(move, player, game);
                break;
            case TAKE_TWO_SAME:
                validateTakeTwoSame(move, player, game);
                break;
            case RESERVE_CARD:
                validateReserveCard(move, player, game);
                break;
            case BUY_CARD:
                validateBuyCard(move, player, game);
                break;
            case DISCARD_TOKENS:
                validateDiscardTokens(move, player, game);
                break;
            case EXIT_GAME:
                break;
            default:
                throw new InvalidMoveException("Unknown move type: " + move.getMoveType());
        }
    }

    /**
     * Validates taking three different colored gems.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException if move is invalid
     */
    private void validateTakeThreeDifferent(final Move move, final Player player, final Game game)
            throws InvalidMoveException {

        final Map<Gem, Integer> selectedGems = move.getSelectedGems();
        final Board board = game.getBoard();

        // Guard clause: Check gem count
        if (selectedGems.size() != 3) {
            throw new InvalidMoveException("Must select exactly 3 different gems");
        }

        // Guard clause: Check total quantity
        final int totalQuantity = selectedGems.values().stream().mapToInt(Integer::intValue).sum();
        if (totalQuantity != 3) {
            throw new InvalidMoveException("Total gem quantity must be exactly 3");
        }

        // Validate each selected gem
        for (final Map.Entry<Gem, Integer> entry : selectedGems.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();

            // Guard clause: Check quantity per gem
            if (quantity != 1) {
                throw new InvalidMoveException("Each gem type must have quantity 1");
            }

            // Guard clause: Check gem availability
            final int availableCount = board.getGemCount(gem);
            if (availableCount < quantity) {
                throw new InvalidMoveException("Insufficient %s gems available (need %d, have %d)",
                        gem, quantity, availableCount);
            }

            // Guard clause: Check gold token restriction
            if (gem == Gem.GOLD) {
                throw new InvalidMoveException("Cannot take gold tokens with this action");
            }
        }
    }

    /**
     * Validates taking two gems of the same color.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException if move is invalid
     */
    private void validateTakeTwoSame(final Move move, final Player player, final Game game)
            throws InvalidMoveException {

        final Map<Gem, Integer> selectedGems = move.getSelectedGems();
        final Board board = game.getBoard();

        // Guard clause: Check gem count
        if (selectedGems.size() != 1) {
            throw new InvalidMoveException("Must select exactly 1 gem type");
        }

        final Map.Entry<Gem, Integer> entry = selectedGems.entrySet().iterator().next();
        final Gem gem = entry.getKey();
        final int quantity = entry.getValue();

        // Guard clause: Check quantity
        if (quantity != 2) {
            throw new InvalidMoveException("Must take exactly 2 gems of the same type");
        }

        // Guard clause: Check gold token restriction
        if (gem == Gem.GOLD) {
            throw new InvalidMoveException("Cannot take gold tokens with this action");
        }

        // Guard clause: Check minimum availability
        final int availableCount = board.getGemCount(gem);
        if (availableCount < MIN_GEMS_FOR_TWO_SAME) {
            throw new InvalidMoveException("Need at least %d %s gems available (have %d)",
                    MIN_GEMS_FOR_TWO_SAME, gem, availableCount);
        }
    }

    /**
     * Validates reserving a card.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException if move is invalid
     */
    private void validateReserveCard(final Move move, final Player player, final Game game)
            throws InvalidMoveException {

        // Guard clause: Check reserved card limit
        if (!player.canReserveCard()) {
            throw new InvalidMoveException("Cannot reserve more than %d cards", MAX_RESERVED_CARDS);
        }

        if (move.hasCardSelection() == move.hasDeckSelection()) {
            throw new InvalidMoveException("Must select exactly one reserve source");
        }

        if (move.hasDeckSelection()) {
            final int tier = move.getDeckTier();
            if (tier < 1 || tier > 3) {
                throw new InvalidMoveException("Deck tier must be between 1 and 3");
            }
            final int deckSize = game.getBoard().getDeckSize(tier);
            if (deckSize <= 0) {
                throw new InvalidMoveException("Selected deck is empty");
            }
            return;
        }

        final Card availableCard = findAvailableCardById(game.getBoard(), move.getCardId());
        if (availableCard == null) {
            throw new InvalidMoveException("Selected card not available");
        }
    }

    /**
     * Validates buying a card.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException        if move is invalid
     * @throws InsufficientTokensException if player lacks required tokens
     */
    private void validateBuyCard(final Move move, final Player player, final Game game)
            throws InvalidMoveException, InsufficientTokensException {

        // Guard clause: Check card selection
        if (!move.hasCardSelection()) {
            throw new InvalidMoveException("Must select a card to buy");
        }
        final Card cardToBuy = move.isReservedCard()
                ? findReservedCardById(player, move.getCardId())
                : findAvailableCardById(game.getBoard(), move.getCardId());
        if (cardToBuy == null) {
            throw new InvalidMoveException("Selected card not available");
        }
        if (!canPlayerAffordCard(player, cardToBuy)) {
            throw new InsufficientTokensException("Insufficient tokens to buy selected card");
        }
    }

    /**
     * Validates discarding tokens.
     * 
     * @param move   Move to validate
     * @param player Player attempting the move
     * @param game   Current game state
     * @throws InvalidMoveException if move is invalid
     */
    private void validateDiscardTokens(final Move move, final Player player, final Game game)
            throws InvalidMoveException {

        final Map<Gem, Integer> selectedGems = move.getSelectedGems();

        // Guard clause: Check if player exceeds token limit
        if (player.getTotalTokenCount() <= game.getMaxTokens()) {
            throw new InvalidMoveException("Player does not exceed token limit");
        }

        // Guard clause: Check discard quantity
        final int discardCount = selectedGems.values().stream().mapToInt(Integer::intValue).sum();
        final int excessTokens = player.getTotalTokenCount() - game.getMaxTokens();

        if (discardCount != excessTokens) {
            throw new InvalidMoveException("Must discard exactly %d tokens (attempting to discard %d)",
                    excessTokens, discardCount);
        }

        // Validate player has the tokens being discarded
        for (final Map.Entry<Gem, Integer> entry : selectedGems.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();
            final int playerCount = player.getTokenCount(gem);

            if (playerCount < quantity) {
                throw new InvalidMoveException("Player does not have %d %s tokens (has %d)",
                        quantity, gem, playerCount);
            }
        }
    }

    /**
     * Checks if a player can afford a card considering their tokens and discounts.
     * 
     * @param player Player to check
     * @param card   Card to purchase
     * @return true if player can afford the card, false otherwise
     */
    public boolean canPlayerAffordCard(final Player player, final Card card) {
        final Map<Gem, Integer> discounts = player.getGemDiscounts();
        final Map<Gem, Integer> tokens = player.getTokens();

        // Track cumulative gold usage across all gem types
        int goldRemaining = tokens.getOrDefault(Gem.GOLD, 0);

        // Calculate effective cost after discounts
        for (final Map.Entry<Gem, Integer> costEntry : card.getCost().entrySet()) {
            final Gem gem = costEntry.getKey();
            final int required = costEntry.getValue();
            final int discount = discounts.getOrDefault(gem, 0);
            final int availableTokens = tokens.getOrDefault(gem, 0);

            final int effectiveCost = Math.max(0, required - discount);
            final int remainingAfterTokens = Math.max(0, effectiveCost - availableTokens);

            // If still need more after using regular tokens, use gold
            if (remainingAfterTokens > 0) {
                if (goldRemaining < remainingAfterTokens) {
                    return false;
                }
                goldRemaining -= remainingAfterTokens;
            }
        }

        return true;
    }

    private Card findAvailableCardById(final Board board, final int cardId) {
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (card.getId() == cardId) {
                    return card;
                }
            }
        }
        return null;
    }

    private Card findReservedCardById(final Player player, final int cardId) {
        for (final Card card : player.getReservedCards()) {
            if (card.getId() == cardId) {
                return card;
            }
        }
        return null;
    }

    // Inside MoveValidator.java
    public ValidationResult validateTakeTwo(Gem gem, Board board) {
        int countInBank = board.getGemCount(gem);

        if (gem == Gem.GOLD) {
            return ValidationResult.fail("Cannot take Gold tokens directly. You must reserve a card to get Gold.");
        }

        if (countInBank < 4) {
            return ValidationResult.fail("Rule Violation: You can only take 2 " + gem +
                    " gems if there are 4+ in the bank. Currently only " + countInBank + " available.");
        }

        return ValidationResult.ok();
    }

    // KEEP YOUR OLD CODE, JUST ADD THIS AT THE BOTTOM
    public ValidationResult getRuleExplanation(Move move, Player player, Game game) {
        Board board = game.getBoard();

        // 1. Rule for taking 2 of the same gem
        // Using move.getMoveType() which returns a MoveType enum
        // Change TAKE_TWO to TAKE_TWO_SAME
        if (move.getMoveType() == MoveType.TAKE_TWO_SAME) {
            if (move.getSelectedGems() != null && !move.getSelectedGems().isEmpty()) {
                Gem gem = move.getSelectedGems().keySet().iterator().next();
                int countInBank = board.getGemCount(gem);
                if (countInBank < 4) {
                    return ValidationResult.fail("The bank only has " + countInBank +
                            " " + gem + " tokens. You need at least 4 to take two.");
                }
            }
        }

        // 2. Rule for buying a card
        if (move.getMoveType() == MoveType.BUY_CARD) {
            Card cardToBuy = null;
            // Search tiers 1-3 to find the card
            for (int tier = 1; tier <= 3; tier++) {
                for (Card c : board.getAvailableCards(tier)) {
                    if (c.getId() == move.getCardId()) {
                        cardToBuy = c;
                        break;
                    }
                }
            }

            if (cardToBuy != null) {
                // Use 'this' instead of 'moveValidator' because we are inside the class
                if (!this.canPlayerAffordCard(player, cardToBuy)) {
                    return ValidationResult.fail("You cannot afford Card #" + move.getCardId() +
                            ". Check your tokens and bonuses.");
                }
            } else {
                return ValidationResult.fail("Card #" + move.getCardId() + " is not on the board.");
            }
        }

        return ValidationResult.ok();
    }
}
