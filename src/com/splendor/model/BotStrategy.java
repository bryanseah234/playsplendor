package com.splendor.model;

import com.splendor.model.validator.MoveValidator;
import java.util.*;

public final class BotStrategy {

    private static final MoveValidator moveValidator = new MoveValidator();

    private BotStrategy() {}

    public static Move chooseBotMove(final Player player, final Game game) {
        final Board board = game.getBoard();

        // Compute weighted bonus importance across ALL nobles (closer nobles = higher weight)
        final Map<Gem, Double> bonusImportance = getWeightedBonusImportance(player, board);

        // 1. Buy the best affordable visible card whose bonus contributes to any target noble
        if (!bonusImportance.isEmpty()) {
            final Integer nobleCardId = getBestAffordableCardForNoble(player, board, bonusImportance);
            if (nobleCardId != null) {
                return new Move(MoveType.BUY_CARD, nobleCardId, false);
            }
            // 2. Buy a reserved card that contributes to any target noble
            final Integer nobleReservedId = getBestAffordableReservedForNoble(player, bonusImportance);
            if (nobleReservedId != null) {
                return new Move(MoveType.BUY_CARD, nobleReservedId, true);
            }
        }

        // 3. Buy any other affordable visible card (prefer higher points)
        final Integer bestVisibleId = getBestAffordableVisibleId(player, board);
        if (bestVisibleId != null) {
            return new Move(MoveType.BUY_CARD, bestVisibleId, false);
        }
        // 4. Buy any other affordable reserved card
        final List<Integer> affordableReserved = getAffordableReservedIds(player);
        if (!affordableReserved.isEmpty()) {
            return new Move(MoveType.BUY_CARD, affordableReserved.get(0), true);
        }

        // 5. Take gems specifically toward buying noble-contributing cards
        if (!bonusImportance.isEmpty()) {
            final Move gemMove = takeGemsTowardNoble(player, board, bonusImportance);
            if (gemMove != null) return gemMove;
        }

        // 6. Fall back to generic gem taking
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

    /**
     * Smart discard: keep gems that are most needed for the noble path; discard least needed first.
     * Gold (wild) is always discarded last.
     */
    public static Move chooseBotDiscard(final Player player, final int excessCount, final Game game) {
        final Map<Gem, Double> importance = getWeightedBonusImportance(player, game.getBoard());

        // Build gem deficit scores so we know how valuable each token in hand is
        final Map<Gem, Double> gemValue = new HashMap<>();
        for (final Gem gem : Gem.values()) {
            if (gem == Gem.GOLD) continue;
            gemValue.put(gem, importance.getOrDefault(gem, 0.0));
        }

        // Sort non-gold gems by value ascending (discard least valuable first)
        final List<Gem> gemsByValue = new ArrayList<>(gemValue.keySet());
        gemsByValue.removeIf(g -> player.getTokenCount(g) == 0);
        gemsByValue.sort(Comparator.comparingDouble(g -> gemValue.getOrDefault(g, 0.0)));
        // Gold is most valuable (wild) — discard last
        if (player.getTokenCount(Gem.GOLD) > 0) {
            gemsByValue.add(Gem.GOLD);
        }

        final Map<Gem, Integer> discardMap = new HashMap<>();
        int leftToDiscard = excessCount;
        for (final Gem gem : gemsByValue) {
            if (leftToDiscard <= 0) break;
            final int count = player.getTokenCount(gem);
            final int toDiscard = Math.min(count, leftToDiscard);
            if (toDiscard > 0) {
                discardMap.put(gem, toDiscard);
                leftToDiscard -= toDiscard;
            }
        }
        return new Move(MoveType.DISCARD_TOKENS, discardMap);
    }

    // --- Multi-noble importance scoring ---

    /**
     * Computes a weighted importance score per bonus gem color across all available nobles.
     * Nobles the player is closer to achieving get higher weight (weight = 1 / cards_still_needed).
     * Colors needed by multiple close nobles accumulate higher scores.
     */
    private static Map<Gem, Double> getWeightedBonusImportance(final Player player, final Board board) {
        final List<Noble> nobles = board.getAvailableNobles();
        if (nobles.isEmpty()) return Collections.emptyMap();

        final Map<Gem, Integer> discounts = player.getGemDiscounts();
        final Map<Gem, Double> importance = new HashMap<>();

        for (final Noble noble : nobles) {
            int missing = 0;
            for (final Map.Entry<Gem, Integer> req : noble.getRequirements().entrySet()) {
                missing += Math.max(0, req.getValue() - discounts.getOrDefault(req.getKey(), 0));
            }
            if (missing == 0) continue; // already qualify, skip
            final double weight = 1.0 / missing;
            for (final Map.Entry<Gem, Integer> req : noble.getRequirements().entrySet()) {
                final int still = Math.max(0, req.getValue() - discounts.getOrDefault(req.getKey(), 0));
                if (still > 0) {
                    importance.merge(req.getKey(), weight * still, Double::sum);
                }
            }
        }
        return importance;
    }

    // --- Card selection helpers ---

    /**
     * Finds the best affordable visible card whose bonus color has noble importance.
     * Scored by: bonus importance * 10 + card points.
     */
    private static Integer getBestAffordableCardForNoble(final Player player, final Board board,
                                                          final Map<Gem, Double> bonusImportance) {
        Card best = null;
        double bestScore = -1;
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                final double imp = bonusImportance.getOrDefault(card.getBonusGem(), 0.0);
                if (imp <= 0) continue;
                if (!moveValidator.canPlayerAffordCard(player, card)) continue;
                final double score = imp * 10 + card.getPoints();
                if (score > bestScore) {
                    bestScore = score;
                    best = card;
                }
            }
        }
        return best != null ? best.getId() : null;
    }

    /**
     * Finds the best affordable reserved card whose bonus color has noble importance.
     */
    private static Integer getBestAffordableReservedForNoble(final Player player,
                                                              final Map<Gem, Double> bonusImportance) {
        Card best = null;
        double bestScore = -1;
        for (final Card card : player.getReservedCards()) {
            final double imp = bonusImportance.getOrDefault(card.getBonusGem(), 0.0);
            if (imp <= 0) continue;
            if (!moveValidator.canPlayerAffordCard(player, card)) continue;
            final double score = imp * 10 + card.getPoints();
            if (score > bestScore) {
                bestScore = score;
                best = card;
            }
        }
        return best != null ? best.getId() : null;
    }

    /**
     * Finds the best affordable visible card by points (fallback when no noble card is affordable).
     */
    private static Integer getBestAffordableVisibleId(final Player player, final Board board) {
        Card best = null;
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (!moveValidator.canPlayerAffordCard(player, card)) continue;
                if (best == null || card.getPoints() > best.getPoints()) {
                    best = card;
                }
            }
        }
        return best != null ? best.getId() : null;
    }

    // --- Gem taking helper ---

    /**
     * Takes gems toward buying cards that give bonus colors needed for nobles.
     * Each card's gem deficit is weighted by the importance of its bonus color,
     * so gems needed for higher-priority cards are preferred.
     */
    private static Move takeGemsTowardNoble(final Player player, final Board board,
                                             final Map<Gem, Double> bonusImportance) {
        final Map<Gem, Integer> discounts = player.getGemDiscounts();
        final Map<Gem, Double> gemDeficit = new HashMap<>();

        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                final double imp = bonusImportance.getOrDefault(card.getBonusGem(), 0.0);
                if (imp <= 0) continue;
                for (final Map.Entry<Gem, Integer> entry : card.getCost().entrySet()) {
                    final Gem gem = entry.getKey();
                    if (gem == Gem.GOLD) continue;
                    final int discount = discounts.getOrDefault(gem, 0);
                    final int effectiveCost = Math.max(0, entry.getValue() - discount);
                    final int have = player.getTokenCount(gem);
                    final int deficit = Math.max(0, effectiveCost - have);
                    if (deficit > 0) {
                        gemDeficit.merge(gem, deficit * imp, Double::sum);
                    }
                }
            }
        }

        // Keep only gems available on the board
        gemDeficit.entrySet().removeIf(e -> board.getGemCount(e.getKey()) <= 0);

        if (gemDeficit.isEmpty()) return null;

        // Sort by weighted deficit descending
        final List<Gem> sortedNeeded = new ArrayList<>(gemDeficit.keySet());
        sortedNeeded.sort((a, b) -> Double.compare(gemDeficit.get(b), gemDeficit.get(a)));

        // Take 2 of the same if the top gem has large deficit and enough supply
        final Gem mostNeeded = sortedNeeded.get(0);
        if (gemDeficit.get(mostNeeded) >= 2 && board.getGemCount(mostNeeded) >= 4) {
            final Map<Gem, Integer> gems = new HashMap<>();
            gems.put(mostNeeded, 2);
            return new Move(MoveType.TAKE_TWO_SAME, gems);
        }

        // Otherwise take up to 3 different most-needed gems
        final Map<Gem, Integer> gems = new HashMap<>();
        for (int i = 0; i < Math.min(3, sortedNeeded.size()); i++) {
            gems.put(sortedNeeded.get(i), 1);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
    }

    // --- Original helpers ---

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
