package com.splendor.model;

import com.splendor.model.validator.MoveValidator;
import java.util.*;

public final class BotStrategy {

    private static final MoveValidator moveValidator = new MoveValidator();

    private BotStrategy() {}

    public static Move chooseBotMove(final Player player, final Game game) {
        final Board board = game.getBoard();
        final List<Integer> affordableVisible = getAffordableVisibleIds(player, board);
        if (!affordableVisible.isEmpty()) {
            return new Move(MoveType.BUY_CARD, affordableVisible.get(0), false);
        }
        final List<Integer> affordableReserved = getAffordableReservedIds(player);
        if (!affordableReserved.isEmpty()) {
            return new Move(MoveType.BUY_CARD, affordableReserved.get(0), true);
        }
        final List<Gem> diffGems = getAvailableDifferentGems(board);
        if (diffGems.size() >= 3) {
            final Map<Gem, Integer> gems = new HashMap<>();
            gems.put(diffGems.get(0), 1);
            gems.put(diffGems.get(1), 1);
            gems.put(diffGems.get(2), 1);
            return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
        }
        final List<Gem> sameGems = getAvailableTwoSameGems(board);
        if (!sameGems.isEmpty()) {
            final Map<Gem, Integer> gems = new HashMap<>();
            gems.put(sameGems.get(0), 2);
            return new Move(MoveType.TAKE_TWO_SAME, gems);
        }
        // Try to reserve from any available deck as fallback
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) {
                return Move.reserveFromDeck(tier);
            }
        }
        // If all decks are empty, take any available single gem
        final List<Gem> availableGems = getAvailableDifferentGems(board);
        if (!availableGems.isEmpty()) {
            final Map<Gem, Integer> gem = new HashMap<>();
            gem.put(availableGems.get(0), 1);
            return new Move(MoveType.TAKE_THREE_DIFFERENT, gem);
        }
        // Ultimate fallback: empty valid move
        return new Move(MoveType.TAKE_THREE_DIFFERENT, new HashMap<>());
    }

    public static Move chooseBotDiscard(final Player player, final int excessCount) {
        final Map<Gem, Integer> discardMap = new HashMap<>();
        int leftToDiscard = excessCount;
        for (final Gem gem : Gem.values()) {
            final int count = player.getTokenCount(gem);
            if (count > 0 && leftToDiscard > 0) {
                final int toDiscard = Math.min(count, leftToDiscard);
                discardMap.put(gem, toDiscard);
                leftToDiscard -= toDiscard;
            }
        }
        return new Move(MoveType.DISCARD_TOKENS, discardMap);
    }

    private static List<Integer> getAffordableVisibleIds(final Player player, final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
            }
        }
        return ids;
    }

    private static List<Integer> getAffordableReservedIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
        }
        return ids;
    }

    private static List<Gem> getAvailableDifferentGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) > 0) gems.add(gem);
        }
        return gems;
    }

    private static List<Gem> getAvailableTwoSameGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) >= 4) gems.add(gem);
        }
        return gems;
    }
}
