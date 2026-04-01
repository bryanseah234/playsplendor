package com.splendor.view;

import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.util.AnsiUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a single development Card as an 8-line ASCII art box for display in the terminal.
 *
 * Each card is drawn as a bordered rectangle showing:
 *   - the card's numeric ID
 *   - its prestige-point value (or "-" for zero-point cards)
 *   - its gem bonus colour
 *   - its purchase cost, word-wrapped to fit the fixed content width
 *
 * Cards that the current player cannot afford are rendered in a dim colour so
 * the player can quickly see at a glance which cards are within reach.
 */
public final class CardRenderer {

    /** Number of visible characters available inside the card border for content. */
    private static final int CARD_CONTENT_WIDTH = 14;

    /** Canonical gem display order used when listing cost tokens inside the card. */
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);

    /** Prevent instantiation of this utility class. */
    private CardRenderer() {}

    /**
     * Renders a card as a fully coloured ASCII box, assuming the current player
     * can afford it (i.e. normal brightness).
     *
     * @param card The card to render.
     * @return An 8-line string with embedded ANSI colour codes.
     */
    public static String formatCardAscii(final Card card) {
        return formatCardAscii(card, true);
    }

    /**
     * Renders a card as an ASCII box, dimming the text and border when the card
     * is not affordable by the current player.
     *
     * The box always occupies exactly 8 lines so that multiple cards placed
     * side-by-side align correctly regardless of their content.
     *
     * @param card       The card to render.
     * @param affordable true for normal colouring; false for dim/greyed-out rendering.
     * @return An 8-line string with embedded ANSI colour codes.
     */
    public static String formatCardAscii(final Card card, final boolean affordable) {
        final String borderColor = affordable ? Colors.WHITE : Colors.DIM; // border colour reflects affordability
        final String textColor = affordable ? "" : Colors.DIM;              // empty string = no extra dim on text

        // Gem bonus label: coloured in its own gem colour when affordable, dim otherwise.
        final String bonusLabel = card.getBonusGem() == null
                ? "-"
                : (affordable
                        ? Colors.colorize(gemLabel(card.getBonusGem()), Colors.getGemColor(card.getBonusGem()))
                        : Colors.colorize(gemLabel(card.getBonusGem()), Colors.DIM));

        final String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : "-";

        // Build each of the 8 fixed lines of the card box.
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

        // Cost spans two lines; the helper word-wraps the token list to fit the width.
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

    /**
     * Conditionally wraps text in the dim colour code.
     * When dimColor is an empty string (affordable path) the text is returned unchanged.
     *
     * @param text     The text to optionally dim.
     * @param dimColor An ANSI colour code, or "" to leave text unstyled.
     * @return The text, optionally wrapped in the dim colour.
     */
    private static String applyDim(final String text, final String dimColor) {
        if (dimColor.isEmpty()) {
            return text;
        }
        return Colors.colorize(text, dimColor);
    }

    /**
     * Formats the card's cost tokens into a fixed number of display lines,
     * word-wrapping based on visible character width so the text fits inside the card box.
     *
     * Each cost token is a coloured letter+number pair (e.g. "R3"). Tokens are packed
     * onto the first line until they would exceed the card width, then overflow wraps
     * to the second line. If there are still more tokens after two lines, the remainder
     * is truncated with an ANSI-safe truncation to prevent layout breaks.
     *
     * @param card     The card whose cost map is being formatted.
     * @param maxLines The maximum number of display lines to produce (always 2 here).
     * @param dimmed   true to render tokens in dim colour instead of their gem colour.
     * @return A list of exactly maxLines strings ready for insertion into the card box.
     */
    private static List<String> formatCardCostLines(final Card card, final int maxLines, final boolean dimmed) {
        final List<String> tokens = new ArrayList<>();

        // Build one coloured token string per gem type that has a non-zero cost.
        for (final Gem gem : GEM_ORDER) {
            final int count = card.getCost().getOrDefault(gem, 0);
            if (count > 0) {
                final String color = dimmed ? Colors.DIM : Colors.getGemColor(gem);
                tokens.add(Colors.colorize(gemLabel(gem) + count, color));
            }
        }

        if (tokens.isEmpty()) {
            // Free cards show "None" on the first line.
            return dimmed ? List.of(Colors.colorize("None", Colors.DIM), "") : List.of("None", "");
        }

        // Word-wrap tokens onto lines, measuring visible width via stripAnsi.
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (AnsiUtils.stripAnsi(next).length() > CARD_CONTENT_WIDTH && !current.isEmpty()) {
                lines.add(current); // current line is full; start a new one
                current = token;
            } else {
                current = next;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current); // flush the last partial line
        }

        // Pad with blank lines if fewer than maxLines were generated.
        if (lines.size() < maxLines) {
            while (lines.size() < maxLines) {
                lines.add("");
            }
            return lines;
        }

        // Merge overflow lines beyond maxLines into the last slot, truncating to fit.
        if (lines.size() > maxLines) {
            final String merged = String.join(" ", lines.subList(1, lines.size()));
            return List.of(lines.get(0), AnsiUtils.truncateAnsi(merged, CARD_CONTENT_WIDTH));
        }

        return lines;
    }

    /**
     * Returns the single- or two-character abbreviation used to represent a gem
     * inside the card's cost display.
     *
     * @param gem The gem type to abbreviate.
     * @return "W", "B", "G", "R", "K", "Au", or "" for an unknown gem.
     */
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
