package com.splendor.util;

import com.splendor.model.*;
import com.splendor.view.Colors;
import java.util.List;
import java.util.Map;

/**
 * Utility class that converts a completed Move into a compact, human-readable
 * string suitable for display in the scrolling move-history panel.
 *
 * All output is ANSI-coloured so that gem names appear in their corresponding
 * terminal colours, matching the rest of the board rendering.
 */
public final class MoveFormatter {

    /** Canonical ordering in which gems appear in formatted output. */
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);

    /** Prevent instantiation of this utility class. */
    private MoveFormatter() {}

    /**
     * Formats a completed move as a single log entry line.
     *
     * The resulting string follows the pattern:
     *   "PlayerName: MoveTypeName [gem counts] [Card ID [(Res)]] [Deck N]"
     * where the optional parts are included only when the move involves them.
     *
     * @param player The player who performed the move.
     * @param move   The move that was executed.
     * @return A coloured, human-readable history entry.
     */
    public static String formatMoveEntry(final Player player, final Move move) {
        final String playerName = player.getName();
        final MoveType moveType = move.getMoveType();
        final String moveTypeName = moveType.getDisplayName();

        final StringBuilder sb = new StringBuilder();
        sb.append(playerName).append(": ").append(moveTypeName);

        // Append gem counts when the move involves token selection (take/discard).
        if (move.hasGemSelection()) {
            final Map<Gem, Integer> selectedGems = move.getSelectedGems();
            final String gemCountText = formatGemCounts(selectedGems);
            sb.append(" ").append(gemCountText);
        }

        // Append card information when a specific card was chosen.
        if (move.hasCardSelection()) {
            final int cardId = move.getCardId();
            sb.append(" #").append(cardId);
            if (move.isReservedCard()) {
                sb.append(" (Res)");
            }
        } else if (move.hasDeckSelection()) {
            final int deckTier = move.getDeckTier();
            sb.append(" Tier ").append(deckTier);
        }
        return sb.toString();
    }

    /**
     * Formats a gem-count map as a space-separated, ANSI-coloured string.
     * Gems are always listed in the canonical GEM_ORDER regardless of map iteration order.
     * Zero-count gems are omitted. Returns "-" when the map is empty or all counts are zero.
     *
     * @param counts A map from Gem to quantity (e.g. {RED=1, BLUE=2}).
     * @return Coloured string such as "W1 B2" or "-".
     */
    public static String formatGemCounts(final Map<Gem, Integer> counts) {
        final StringBuilder sb = new StringBuilder();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (count > 0) {
                if (sb.length() > 0) sb.append(" ");
                // Coloured single-letter label followed by the quantity digit.
                final String gemAbbreviation = gem.getShortCode();
                final String gemColor = Colors.getGemColor(gem);
                final String colorizedLabel = Colors.colorize(gemAbbreviation, gemColor);
                sb.append(colorizedLabel).append(count);
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

}
