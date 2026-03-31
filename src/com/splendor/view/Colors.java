package com.splendor.view;

import com.splendor.model.Gem;

/**
 * Centralized ANSI color constants and colorization utilities for terminal output.
 * 
 * This utility class provides a consistent color scheme across the entire Splendor
 * console interface. Each gem type, UI element, and game component has an associated
 * color to improve visual clarity and user experience.
 * 
 * <p>Color assignments:
 * <ul>
 *   <li>{@link #RED}, {@link #GREEN}, {@link #BLUE}, {@link #WHITE}, {@link #BLACK} - Gem colors</li>
 *   <li>{@link #GOLD} - Gold token (wild card) color</li>
 *   <li>{@link #CYAN} - Player names and important information</li>
 *   <li>{@link #PURPLE} - Noble tiles</li>
 *   <li>{@link #GRAY} - Disabled options and secondary information</li>
 *   <li>{@link #DIM} - Unaffordable cards and inaccessible elements</li>
 * </ul>
 * 
 * <p>ANSI support is automatically detected at startup via the {@link #detectAnsiSupport()}
 * method, which checks JVM system properties and environment variables. This ensures
 * compatibility with terminals that don't support ANSI escape codes.
 * 
 * <p>All colorization methods automatically append the {@link #RESET} code after the
 * colored text to prevent color bleeding into subsequent output.
 * 
 * <p>Usage example:
 * <pre>
 *   String redText = Colors.colorize("Red Gem", Colors.RED);
 *   String gemColor = Colors.getGemColor(Gem.BLUE);
 * </pre>
 * 
 * @see Gem For gem type to color mapping
 * @see AnsiUtils For ANSI-aware string manipulation
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

    /**
     * Determines at startup whether ANSI escape codes should be emitted.
     *
     * Resolution order (first match wins):
     *   1. JVM system property "splendor.ansi" (true/false string).
     *   2. Environment variable "SPLENDOR_ANSI" ("true", "1" = enabled).
     *   3. Default: enabled (returns true).
     *
     * @return true if ANSI output is enabled, false if it should be suppressed.
     */
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
