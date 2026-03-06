package com.splendor.view;

import com.splendor.model.Gem;

/**
 * ANSI Color Constants for terminal output.
 */
public class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m"; // Red Gem
    public static final String GREEN = "\u001B[32m"; // Green Gem
    public static final String BLUE = "\u001B[34m"; // Blue Gem
    public static final String WHITE = "\u001B[37m"; // White Gem
    public static final String BLACK = "\u001B[90m"; // Black Gem (Dark Gray)
    public static final String GOLD = "\u001B[33m"; // Gold Gem
    public static final String CYAN = "\u001B[36m"; // Player Names / Info
    public static final String PURPLE = "\u001B[35m"; // Nobles
    public static final String GRAY = "\u001B[90m"; // Disabled / Info
    public static final String DIM = "\u001B[38;5;240m"; // Unaffordable / Inaccessible
    private static final boolean ANSI_ENABLED = detectAnsiSupport();

    /**
     * Helper to wrap text in ANSI color codes.
     * 
     * @param text      The text to colorize
     * @param colorCode The ANSI color code constant
     * @return String wrapped in color code and reset code
     */
    public static String colorize(String text, String colorCode) {
        if (!ANSI_ENABLED) {
            return text;
        }
        return colorCode + text + RESET;
    }

    /**
     * Maps a Gem type to its corresponding ANSI color.
     * 
     * @param gem The gem to get color for
     * @return ANSI color code string
     */
    public static String getGemColor(Gem gem) {
        if (gem == Gem.RED) {
            return RED;
        }
        if (gem == Gem.GREEN) {
            return GREEN;
        }
        if (gem == Gem.BLUE) {
            return BLUE;
        }
        if (gem == Gem.WHITE) {
            return WHITE;
        }
        if (gem == Gem.BLACK) {
            return BLACK;
        }
        if (gem == Gem.GOLD) {
            return GOLD;
        }
        return RESET;
    }

    private static boolean detectAnsiSupport() {
        final String override = System.getProperty("splendor.ansi");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        final String envOverride = System.getenv("SPLENDOR_ANSI");
        if (envOverride != null) {
            return envOverride.equalsIgnoreCase("true") || envOverride.equals("1");
        }
        return true;
    }
}
