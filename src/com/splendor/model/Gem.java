// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

package com.splendor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the gem types in Splendor.
 * Five collectible colors (White, Blue, Green, Red, Black) plus Gold (wildcard).
 */
public enum Gem {
    /** White gem, representing Diamond. */
    WHITE("W", "White"),
    /** Blue gem, representing Sapphire. */
    BLUE("B", "Blue"),
    /** Green gem, representing Emerald. */
    GREEN("G", "Green"),
    /** Red gem, representing Ruby. */
    RED("R", "Red"),
    /** Black gem, representing Onyx. */
    BLACK("K", "Black"),
    /** Gold token, acting as a wildcard for any other color. */
    GOLD("Au", "Gold");

        /** Canonical display order used across UI and formatting output. */
        private static final List<Gem> DISPLAY_ORDER = List.of(
            RED, GREEN, BLUE, WHITE, BLACK, GOLD);

        /** Canonical display order excluding Gold for non-gold-only menu actions. */
        private static final List<Gem> DISPLAY_ORDER_NO_GOLD = List.of(
            RED, GREEN, BLUE, WHITE, BLACK);

    private final String shortCode;
    private final String label;

    Gem(String shortCode, String label) {
        this.shortCode = shortCode;
        this.label = label;
    }

    /**
     * Gets the single-character short code.
     *
     * @return The short code string
     */
    public String getShortCode() { return shortCode; }

    /**
     * Gets the full label of the gem.
     *
     * @return The full label string
     */
    public String getLabel() { return label; }

    /**
     * Gets the canonical display order for all gem types, including Gold.
     *
     * @return Immutable gem display order used by renderer and formatters.
     */
    public static List<Gem> displayOrder() {
        return DISPLAY_ORDER;
    }

    /**
     * Gets the canonical display order excluding Gold.
     *
     * @return Immutable non-gold gem display order used by menu take-gem options.
     */
    public static List<Gem> displayOrderNoGold() {
        return DISPLAY_ORDER_NO_GOLD;
    }
    
    @Override
    public String toString() {
        return this.shortCode; 
    }

    /**
     * Parses a single string (like "R", "Red", "RED") into a Gem.
     *
     * @param input The string to parse
     * @return The corresponding Gem enum instance
     */
    public static Gem parseGem(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Gem input cannot be empty.");
        }
        String normalized = input.trim().toUpperCase();
        for (Gem gem : values()) {
            if (gem.name().equals(normalized) || gem.shortCode.equalsIgnoreCase(normalized)) {
                return gem;
            }
        }
        throw new IllegalArgumentException("Invalid gem type: " + input);
    }

    /**
     * Parses a string payload (like "R G B" or "RGB") into a List of Gems.
     *
     * @param input The string of multiple gems to parse
     * @return A list of parsed Gem instances
     */
    public static List<Gem> parseGemSelection(String input) {
        List<Gem> gems = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return gems;
        
        // Handle space-separated like "R G B"
        if (input.contains(" ")) {
            String[] tokens = input.trim().split("\\s+");
            for (String token : tokens) {
                gems.add(parseGem(token));
            }
        } else {
            // Handle concatenated like "RGB"
            for (char c : input.toCharArray()) {
                gems.add(parseGem(String.valueOf(c)));
            }
        }
        return gems;
    }
}