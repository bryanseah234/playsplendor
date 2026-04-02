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