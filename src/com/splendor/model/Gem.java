package com.splendor.model;

import java.util.ArrayList;
import java.util.List;

public enum Gem {
    WHITE("W", "White"),
    BLUE("B", "Blue"),
    GREEN("G", "Green"),
    RED("R", "Red"),
    BLACK("K", "Black"),
    GOLD("Au", "Gold");

    private final String shortCode;
    private final String label;

    Gem(String shortCode, String label) {
        this.shortCode = shortCode;
        this.label = label;
    }

    public String getShortCode() { return shortCode; }
    public String getLabel() { return label; }
    
    @Override
    public String toString() {
        return this.shortCode; 
    }

    /**
     * Parses a single string (like "R", "Red", "RED") into a Gem.
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