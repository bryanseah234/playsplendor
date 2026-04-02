// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Configuration provider interface for dependency injection and testability.
 * Defines the contract for loading and accessing configuration properties.
 */
package com.splendor.config;

import com.splendor.exception.ConfigException;

/**
 * Contract for configuration providers in the Splendor game.
 * 
 * This interface abstracts the source of configuration data, enabling:
 * <ul>
 *   <li>File-based configuration via {@link FileConfigProvider}</li>
 *   <li>Mock configuration for unit testing</li>
 *   <li>Future support for environment variables, databases, or remote config</li>
 * </ul>
 * 
 * <p>Configuration properties control game behavior including:
 * <ul>
 *   <li>Winning point threshold (default: 15)</li>
 *   <li>Maximum tokens per player (default: 10)</li>
 *   <li>Gem distribution for different player counts</li>
 *   <li>Card counts per tier</li>
 *   <li>Server port and network settings</li>
 * </ul>
 * 
 * <p>The configuration is loaded once at application startup via {@link #loadConfiguration()}
 * and validated by {@link ConfigValidator} before the game begins.
 * 
 * <p>Usage example:
 * <pre>
 *   IConfigProvider config = new FileConfigProvider();
 *   config.loadConfiguration();
 *   int winningPoints = config.getIntProperty(ConfigKeys.WINNING_POINTS, 15);
 * </pre>
 * 
 * @see FileConfigProvider Default file-based implementation
 * @see ConfigKeys Standard configuration property keys
 * @see ConfigValidator Configuration validation logic
 */
public interface IConfigProvider {
    
    /**
     * Loads the configuration from the underlying source.
     * 
     * @throws ConfigException if configuration loading fails
     */
    void loadConfiguration() throws ConfigException;
    
    /**
     * Gets a string property value.
     * 
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    String getStringProperty(String key, String defaultValue);
    
    /**
     * Gets an integer property value.
     * 
     * @param key Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value or default
     */
    int getIntProperty(String key, int defaultValue);
}