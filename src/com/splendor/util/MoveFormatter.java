package com.splendor.util;

import com.splendor.model.*;
import com.splendor.view.Colors;
import java.util.List;
import java.util.Map;

public final class MoveFormatter {

    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);

    private MoveFormatter() {}

    public static String formatMoveEntry(final Player player, final Move move) {
        final StringBuilder sb = new StringBuilder();
        sb.append(player.getName()).append(": ").append(move.getMoveType().getDisplayName());
        if (move.hasGemSelection()) sb.append(" ").append(formatGemCounts(move.getSelectedGems()));
        if (move.hasCardSelection()) {
            sb.append(" Card ").append(move.getCardId());
            if (move.isReservedCard()) sb.append(" (Res)");
        } else if (move.hasDeckSelection()) {
            sb.append(" Deck ").append(move.getDeckTier());
        }
        return sb.toString();
    }

    public static String formatGemCounts(final Map<Gem, Integer> counts) {
        final StringBuilder sb = new StringBuilder();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (count > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem))).append(count);
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    public static String gemLabel(final Gem gem) {
        if (gem == Gem.WHITE) return "W";
        if (gem == Gem.BLUE) return "B";
        if (gem == Gem.GREEN) return "G";
        if (gem == Gem.RED) return "R";
        if (gem == Gem.BLACK) return "K";
        if (gem == Gem.GOLD) return "Au";
        return "";
    }
}
