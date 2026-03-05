/**
 * Application-wide constants and configuration values.
 * Centralizes commonly used constants to prevent magic numbers and strings.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.util;

/**
 * Application constants used throughout the codebase.
 * Provides centralized access to commonly used values.
 */
public final class Constants {
    
    // Server configuration
    public static final String SERVER_MODE_FLAG = "--server";
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final int MAX_CLIENT_CONNECTIONS = 10;
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    
    // Game configuration
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int DEFAULT_WINNING_POINTS = 15;
    public static final int DEFAULT_MAX_TOKENS = 10;
    public static final int MAX_RESERVED_CARDS = 3;
    
    // Card tiers
    public static final int MIN_CARD_TIER = 1;
    public static final int MAX_CARD_TIER = 3;
    public static final int CARDS_PER_TIER_DISPLAYED = 4;
    
    // Gem configuration
    public static final int GOLD_TOKENS_PER_PLAYER_COUNT = 5;
    public static final int MIN_GEMS_FOR_TWO_SAME_ACTION = 4;
    public static final int MAX_GEMS_PER_TURN = 3;
    
    // Input validation
    public static final int MIN_PLAYER_NAME_LENGTH = 1;
    public static final int MAX_PLAYER_NAME_LENGTH = 20;
    public static final int MAX_INPUT_RETRIES = 3;
    
    // Network protocol
    public static final String PROTOCOL_DELIMITER = ":";
    public static final String PROTOCOL_SUCCESS = "SUCCESS";
    public static final String PROTOCOL_ERROR = "ERROR";
    public static final String PROTOCOL_MOVE = "MOVE";
    
    // Logging
    public static final String LOG_FORMAT = "[%s] %s: %s";
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_ERROR = "ERROR";
    public static final String LOG_LEVEL_DEBUG = "DEBUG";
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Constants() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}