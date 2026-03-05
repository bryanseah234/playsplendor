/**
 * Configuration provider interface for dependency injection and testability.
 * Defines the contract for loading and accessing configuration properties.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.config;

/**
 * Interface for configuration management, allowing for different implementations
 * such as file-based configuration or mock configuration for testing.
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
    
    /**
     * Gets a boolean property value.
     * 
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    boolean getBooleanProperty(String key, boolean defaultValue);
    
    /**
     * Checks if a property exists.
     * 
     * @param key Property key
     * @return true if property exists
     */
    boolean hasProperty(String key);
}