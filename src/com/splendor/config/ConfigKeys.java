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
    /** Winning points required to win the game. */
    public static final String WINNING_POINTS = "game.points.win";
    /** Maximum number of tokens a player can hold. */
    public static final String MAX_TOKENS = "game.max_tokens";
    
    // Player scaling configuration
    /** Number of gems per color for a 2-player setup. */
    public static final String SETUP_2P_GEMS = "game.setup.2p.gems";
    /** Number of gems per color for a 3-player setup. */
    public static final String SETUP_3P_GEMS = "game.setup.3p.gems";
    /** Number of gems per color for a 4-player setup. */
    public static final String SETUP_4P_GEMS = "game.setup.4p.gems";
    
    // Game rule configuration
    /** Maximum number of reserved cards a player can hold. */
    public static final String MAX_RESERVED_CARDS = "game.max_reserved_cards";

    // Card Data configuration
    /** Path to the CSV file containing card data. */
    public static final String FILE_CARDS_DATA = "file.cards.data";
    /** Number of tier 1 cards to reveal on the board. */
    public static final String TIER1_CARD_COUNT = "game.tier1.card_count";
    /** Number of tier 2 cards to reveal on the board. */
    public static final String TIER2_CARD_COUNT = "game.tier2.card_count";
    /** Number of tier 3 cards to reveal on the board. */
    public static final String TIER3_CARD_COUNT = "game.tier3.card_count";
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ConfigKeys() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    
}