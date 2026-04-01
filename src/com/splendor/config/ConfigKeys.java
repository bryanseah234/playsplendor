/**
 * Configuration property keys used throughout the application.
 * Centralizes all configuration key names to prevent typos and ensure consistency.
 * 
 */
package com.splendor.config;

/**
 * Constants for configuration property keys.
 * These keys are used to retrieve values from the configuration provider.
 */
public final class ConfigKeys {
    
    // Game configuration
    public static final String WINNING_POINTS = "game.points.win";
    public static final String MAX_TOKENS = "game.max_tokens";
    
    // Player scaling configuration
    public static final String SETUP_2P_GEMS = "game.setup.2p.gems";
    public static final String SETUP_3P_GEMS = "game.setup.3p.gems";
    public static final String SETUP_4P_GEMS = "game.setup.4p.gems";
    public static final String SETUP_NOBLES_ADD = "game.setup.nobles.add";
    
    // Game rule configuration
    public static final String MAX_RESERVED_CARDS = "game.max_reserved_cards";

    // Card Data configuration
    public static final String FILE_CARDS_DATA = "file.cards.data";
    public static final String TIER1_CARD_COUNT = "game.tier1.card_count";
    public static final String TIER2_CARD_COUNT = "game.tier2.card_count";
    public static final String TIER3_CARD_COUNT = "game.tier3.card_count";
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ConfigKeys() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    
}