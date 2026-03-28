package com.splendor.util;

import com.splendor.model.Gem;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that converts raw user input strings into Gem enum values.
 *
 * Players type gem identifiers in the terminal using either abbreviated codes
 * (single letters) or full color names. This class centralises all such
 * parsing so that the view layer never needs to know the exact format rules,
 * and so that the same parsing logic can be reused for both console and network
 * input paths.
 *
 * Supported abbreviations:
 *   W / WHITE  -> WHITE
 *   B / BLUE   -> BLUE
 *   G / GREEN  -> GREEN
 *   R / RED    -> RED
 *   K / BLACK  -> BLACK  (K avoids ambiguity with BLUE's B)
 *   AU / GOLD  -> GOLD
 */
public final class GemParser {

    /** Prevent instantiation of this utility class. */
    private GemParser() {
    }

    /**
     * Parses a single gem token from a trimmed, potentially mixed-case string.
     * Accepts both short codes (W, B, G, R, K, AU) and full names (WHITE, BLUE, etc.).
     *
     * @param token The string to parse; leading/trailing whitespace is ignored.
     * @return The matching Gem enum value.
     * @throws IllegalArgumentException if the token does not match any known gem.
     */
    public static Gem parseGem(final String token) {
        final String normalized = token.trim().toUpperCase();
        switch (normalized) {
            case "W":
            case "WHITE":
                return Gem.WHITE;
            case "B":
            case "BLUE":
                return Gem.BLUE;
            case "G":
            case "GREEN":
                return Gem.GREEN;
            case "R":
            case "RED":
                return Gem.RED;
            case "K":
            case "BLACK":
                return Gem.BLACK;
            case "AU":
            case "GOLD":
                return Gem.GOLD;
            default:
                throw new IllegalArgumentException("Unknown gem: " + token);
        }
    }

    /**
     * Parses a single gem from a one-character code.
     * Used when iterating over a compact string of gem codes (e.g. "RGB").
     * Gold is not supported here because its code ("AU") is two characters.
     *
     * @param code Single uppercase character: R, G, B, W, or K.
     * @return The matching Gem, or null if the character is unrecognised.
     */
    public static Gem parseGemCode(final char code) {
        switch (Character.toUpperCase(code)) {
            case 'R':
                return Gem.RED;
            case 'G':
                return Gem.GREEN;
            case 'B':
                return Gem.BLUE;
            case 'W':
                return Gem.WHITE;
            case 'K':
                return Gem.BLACK;
            default:
                return null; // caller must handle unrecognised codes
        }
    }

    /**
     * Parses a free-form gem selection string into a list of Gem values.
     *
     * The input may be space-separated tokens (e.g. "R G B"), a compact sequence of
     * single-character codes (e.g. "RGB"), or full color names (e.g. "Red Green Blue").
     * Mixed formats like "R GREEN B" are handled by normalising to uppercase and then
     * splitting on non-alphabetic runs.
     *
     * @param input Raw user input; null is treated as empty.
     * @return Ordered list of Gem values parsed from the input.
     * @throws IllegalArgumentException if the input is empty or contains an unknown token.
     */
    public static List<Gem> parseGemSelection(final String input) {
        final String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please enter at least one color");
        }
        final String normalized = trimmed.toUpperCase();

        // Collapse any run of non-letter characters into a single space delimiter.
        final String spaced = normalized.replaceAll("[^A-Z]+", " ").trim();

        if (!spaced.isEmpty() && spaced.contains(" ")) {
            // Space-separated tokens: parse each word individually.
            final String[] parts = spaced.split("\\s+");
            final List<Gem> gems = new ArrayList<>();
            for (final String part : parts) {
                gems.add(parseGem(part));
            }
            return gems;
        }

        // No spaces: treat as a compact sequence of single-char codes (e.g. "RGK").
        final String compact = spaced.isEmpty() ? normalized.replaceAll("[^A-Z]+", "") : spaced;
        return parseGemSequence(compact);
    }

    /**
     * Parses a compact gem sequence string where each gem is represented by a
     * single uppercase letter, except gold which uses the two-character code "AU".
     *
     * The method scans left-to-right, consuming "AU" when it appears and single
     * characters otherwise.
     *
     * @param input Compact uppercase string (e.g. "RGBAU" means Red, Green, Blue, Gold).
     * @return Ordered list of Gem values.
     * @throws IllegalArgumentException if any character does not map to a known gem.
     */
    public static List<Gem> parseGemSequence(final String input) {
        final List<Gem> gems = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            // Check for the two-character gold code before single-char codes.
            if (i + 1 < input.length() && input.startsWith("AU", i)) {
                gems.add(Gem.GOLD);
                i += 2;
                continue;
            }
            gems.add(parseGem(String.valueOf(input.charAt(i))));
            i += 1;
        }
        return gems;
    }
}
