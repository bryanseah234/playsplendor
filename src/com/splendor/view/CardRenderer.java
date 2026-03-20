package com.splendor.view;

import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.util.AnsiUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles ASCII rendering of individual development cards.
 */
public final class CardRenderer {

    private static final int CARD_CONTENT_WIDTH = 14;
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);

    private CardRenderer() {}

    public static String formatCardAscii(final Card card) {
        return formatCardAscii(card, true);
    }

    public static String formatCardAscii(final Card card, final boolean affordable) {
        final String borderColor = affordable ? Colors.WHITE : Colors.DIM;
        final String textColor = affordable ? "" : Colors.DIM;
        final String bonusLabel = card.getBonusGem() == null
                ? "-"
                : (affordable
                        ? Colors.colorize(gemLabel(card.getBonusGem()), Colors.getGemColor(card.getBonusGem()))
                        : Colors.colorize(gemLabel(card.getBonusGem()), Colors.DIM));
        final String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : "-";
        final String line1 = Colors.colorize("┌" + "─".repeat(CARD_CONTENT_WIDTH) + "┐", borderColor);
        final String line2 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(applyDim("ID: " + card.getId(), textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line3 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(applyDim("Pts: " + points, textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line4 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(applyDim("Bonus: ", textColor) + bonusLabel, CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line5 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(applyDim("Cost:", textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final List<String> costLines = formatCardCostLines(card, 2, !affordable);
        final String line6 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(costLines.get(0), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line7 = Colors.colorize("│", borderColor)
                + AnsiUtils.padRightAnsi(costLines.get(1), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line8 = Colors.colorize("└" + "─".repeat(CARD_CONTENT_WIDTH) + "┘", borderColor);

        return String.join("\n", line1, line2, line3, line4, line5, line6, line7, line8);
    }

    private static String applyDim(final String text, final String dimColor) {
        if (dimColor.isEmpty()) {
            return text;
        }
        return Colors.colorize(text, dimColor);
    }

    private static List<String> formatCardCostLines(final Card card, final int maxLines, final boolean dimmed) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = card.getCost().getOrDefault(gem, 0);
            if (count > 0) {
                final String color = dimmed ? Colors.DIM : Colors.getGemColor(gem);
                tokens.add(Colors.colorize(gemLabel(gem) + count, color));
            }
        }
        if (tokens.isEmpty()) {
            return dimmed ? List.of(Colors.colorize("None", Colors.DIM), "") : List.of("None", "");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (AnsiUtils.stripAnsi(next).length() > CARD_CONTENT_WIDTH && !current.isEmpty()) {
                lines.add(current);
                current = token;
            } else {
                current = next;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        if (lines.size() < maxLines) {
            while (lines.size() < maxLines) {
                lines.add("");
            }
            return lines;
        }
        if (lines.size() > maxLines) {
            final String merged = String.join(" ", lines.subList(1, lines.size()));
            return List.of(lines.get(0), AnsiUtils.truncateAnsi(merged, CARD_CONTENT_WIDTH));
        }
        return lines;
    }

    static String gemLabel(final Gem gem) {
        if (gem == Gem.WHITE) return "W";
        if (gem == Gem.BLUE) return "B";
        if (gem == Gem.GREEN) return "G";
        if (gem == Gem.RED) return "R";
        if (gem == Gem.BLACK) return "K";
        if (gem == Gem.GOLD) return "Au";
        return "";
    }
}
