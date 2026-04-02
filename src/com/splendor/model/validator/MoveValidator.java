// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

package com.splendor.model.validator;

import com.splendor.config.ConfigKeys;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.InsufficientTokensException;
import com.splendor.exception.InvalidMoveException;
import com.splendor.exception.SplendorException;
import com.splendor.model.Board;
import com.splendor.model.Card;
import com.splendor.model.Game;
import com.splendor.model.Gem;
import com.splendor.model.Move;
import com.splendor.model.Player;
import java.util.Map;
import java.util.Objects;

/**
 * Validates whether proposed moves obey Splendor rules.
 */
public class MoveValidator {
    private static final int DEFAULT_MAX_RESERVED_CARDS = 3;

    private final int maxReservedCards;

    /**
     * Creates a validator with configuration-backed rule limits.
     *
     * @param configProvider Configuration provider for runtime rule values.
     */
    public MoveValidator(final IConfigProvider configProvider) {
        this.maxReservedCards = configProvider.getIntProperty(
                ConfigKeys.MAX_RESERVED_CARDS, DEFAULT_MAX_RESERVED_CARDS);
    }

    /**
     * Validates a move and throws when the move is illegal.
     *
     * @param move The move to validate
     * @param player The player attempting the move
     * @param game The current game object
     * @throws SplendorException if the move is invalid or breaks game rules
     */
    public void validateMove(final Move move, final Player player, final Game game) throws SplendorException {
        Objects.requireNonNull(move, "move");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(game, "game");

        switch (move.getMoveType()) {
            case TAKE_THREE_DIFFERENT:
                validateTakeThreeDifferent(move, game.getBoard());
                break;
            case TAKE_TWO_SAME:
                validateTakeTwoSame(move, game.getBoard());
                break;
            case RESERVE_CARD:
                validateReserveCard(move, player, game.getBoard());
                break;
            case BUY_CARD:
                validateBuyCard(move, player, game.getBoard());
                break;
            case DISCARD_TOKENS:
                validateDiscardTokens(move, player, game.getMaxTokens());
                break;
            case EXIT_GAME:
                return;
            default:
                throw new InvalidMoveException("Unknown move type: " + move.getMoveType());
        }
    }

    /**
     * Returns a non-throwing explanation of whether a move is valid.
     *
     * @param move The move to check
     * @param player The player attempting the move
     * @param game The current game object
     * @return ValidationResult indicating success or failure reason
     */
    public ValidationResult getRuleExplanation(final Move move, final Player player, final Game game) {
        try {
            validateMove(move, player, game);
            return ValidationResult.ok();
        } catch (final SplendorException | RuntimeException e) {
            return ValidationResult.fail(e.getMessage());
        }
    }

    /**
     * Checks whether a player can pay for a card using discounts and gold.
     *
     * @param player The player attempting to buy the card
     * @param card The card to check affordability against
     * @return true if the player has enough tokens and discounts, false otherwise
     */
    public boolean canPlayerAffordCard(final Player player, final Card card) {
        final Map<Gem, Integer> discounts = player.getGemDiscounts();
        int requiredGold = 0;

        for (final Gem gem : Gem.values()) {
            if (gem == Gem.GOLD) {
                continue;
            }
            final int cost = card.getCost().getOrDefault(gem, 0);
            final int discount = discounts.getOrDefault(gem, 0);
            final int owed = Math.max(0, cost - discount);
            final int coloredTokens = player.getTokenCount(gem);
            requiredGold += Math.max(0, owed - coloredTokens);
        }

        return player.getTokenCount(Gem.GOLD) >= requiredGold;
    }

    /**
     * Gets the maximum number of reserved cards allowed per player.
     *
     * @return The maximum number of reserved cards
     */
    public int getMaxReservedCards() {
        return maxReservedCards;
    }

    private void validateTakeThreeDifferent(final Move move, final Board board) throws InvalidMoveException {
        final Map<Gem, Integer> selected = move.getSelectedGems();
        if (selected.size() != 3) {
            throw new InvalidMoveException("Must take exactly 3 different gem colors");
        }

        int total = 0;
        for (final Map.Entry<Gem, Integer> entry : selected.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();

            if (gem == Gem.GOLD) {
                throw new InvalidMoveException("Cannot take gold with TAKE_THREE_DIFFERENT");
            }
            if (quantity != 1) {
                throw new InvalidMoveException("Each selected gem must have quantity 1");
            }
            if (board.getGemCount(gem) < 1) {
                throw new InvalidMoveException("Selected gem is not available in bank: " + gem);
            }
            total += quantity;
        }

        if (total != 3) {
            throw new InvalidMoveException("Must take exactly 3 gems total");
        }
    }

    private void validateTakeTwoSame(final Move move, final Board board) throws InvalidMoveException {
        final Map<Gem, Integer> selected = move.getSelectedGems();
        if (selected.size() != 1) {
            throw new InvalidMoveException("Must select exactly one gem color for TAKE_TWO_SAME");
        }

        final Map.Entry<Gem, Integer> entry = selected.entrySet().iterator().next();
        final Gem gem = entry.getKey();
        final int quantity = entry.getValue();

        if (gem == Gem.GOLD) {
            throw new InvalidMoveException("Cannot take gold with TAKE_TWO_SAME");
        }
        if (quantity != 2) {
            throw new InvalidMoveException("Must take exactly 2 gems for TAKE_TWO_SAME");
        }
        if (board.getGemCount(gem) < 4) {
            throw new InvalidMoveException("Need at least 4 gems in bank to take two of the same color");
        }
    }

    private void validateReserveCard(final Move move, final Player player, final Board board)
            throws InvalidMoveException {
        if (!player.canReserveCard(maxReservedCards)) {
            throw new InvalidMoveException("Player has reached reserve limit");
        }

        if (move.hasDeckSelection()) {
            final Integer tier = move.getDeckTier();
            if (tier == null || tier < 1 || tier > 3) {
                throw new InvalidMoveException("Deck tier must be between 1 and 3");
            }
            if (board.getDeckSize(tier) <= 0) {
                throw new InvalidMoveException("Selected deck tier is empty");
            }
            return;
        }

        if (!move.hasCardSelection()) {
            throw new InvalidMoveException("Reserve move must select a visible card or a deck tier");
        }

        if (findVisibleCard(board, move.getCardId()) == null) {
            throw new InvalidMoveException("Card not found on board for reserve");
        }
    }

    private void validateBuyCard(final Move move, final Player player, final Board board)
            throws SplendorException {
        if (!move.hasCardSelection()) {
            throw new InvalidMoveException("Buy move must specify a card id");
        }

        final Card target;
        if (move.isReservedCard()) {
            target = findReservedCard(player, move.getCardId());
            if (target == null) {
                throw new InvalidMoveException("Reserved card id not found in player's reserved cards");
            }
        } else {
            target = findVisibleCard(board, move.getCardId());
            if (target == null) {
                throw new InvalidMoveException("Card id not found on board");
            }
        }

        if (!canPlayerAffordCard(player, target)) {
            throw new InsufficientTokensException("Player cannot afford selected card");
        }
    }

    private void validateDiscardTokens(final Move move, final Player player, final int maxTokens)
            throws InvalidMoveException {
        final int total = player.getTotalTokenCount();
        final int excess = total - maxTokens;
        if (excess <= 0) {
            throw new InvalidMoveException("Player does not exceed token limit");
        }

        int discarded = 0;
        for (final Map.Entry<Gem, Integer> entry : move.getSelectedGems().entrySet()) {
            final Gem gem = entry.getKey();
            final int qty = entry.getValue();

            if (qty <= 0) {
                throw new InvalidMoveException("Discard quantities must be positive");
            }
            if (player.getTokenCount(gem) < qty) {
                throw new InvalidMoveException("Cannot discard more tokens than player owns");
            }
            discarded += qty;
        }

        if (discarded != excess) {
            throw new InvalidMoveException("Discard count must equal excess tokens: " + excess);
        }
    }

    private Card findVisibleCard(final Board board, final Integer cardId) {
        if (cardId == null) {
            return null;
        }
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (card.getId() == cardId) {
                    return card;
                }
            }
        }
        return null;
    }

    private Card findReservedCard(final Player player, final Integer cardId) {
        if (cardId == null) {
            return null;
        }
        for (final Card card : player.getReservedCards()) {
            if (card.getId() == cardId) {
                return card;
            }
        }
        return null;
    }
}
