/**
 * Configuration property keys used throughout the application.
 * Centralizes all configuration key names to prevent typos and ensure consistency.
 * 
 * @author Splendor Development Team
 * @version 1.0
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
    
    // Server configuration
    public static final String SERVER_PORT = "server.port";
    
    // Network configuration
    public static final String MAX_CLIENTS = "network.max_clients";
    public static final String CONNECTION_TIMEOUT = "network.connection_timeout";
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ConfigKeys() {
        throw new AssertionError("Cannot instantiate utility class");
    }
}