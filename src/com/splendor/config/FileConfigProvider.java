/**
 * File-based configuration provider implementation.
 * Loads configuration properties from a properties file in the resources directory.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented safe file loading and exception handling
 */
package com.splendor.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Production implementation of IConfigProvider that loads configuration
 * from a properties file located in the classpath resources.
 */
public class FileConfigProvider implements IConfigProvider {
    
    private static final String CONFIG_FILE_PATH = "src/resources/config.properties";
    private final Properties properties;
    
    /**
     * Creates a new FileConfigProvider with empty properties.
     * Call loadConfiguration() to populate the properties.
     */
    public FileConfigProvider() {
        this.properties = new Properties();
    }
    
    @Override
    public void loadConfiguration() throws ConfigException {
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);
            validateRequiredProperties();
            
        } catch (FileNotFoundException e) {
            throw new ConfigException("Configuration file not found: " + CONFIG_FILE_PATH, e);
        } catch (IOException e) {
            throw new ConfigException("Failed to load configuration file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getStringProperty(final String key, final String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    @Override
    public int getIntProperty(final String key, final int defaultValue) {
        final String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public boolean getBooleanProperty(final String key, final boolean defaultValue) {
        final String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value.trim());
    }
    
    @Override
    public boolean hasProperty(final String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Validates that all required configuration properties are present.
     * 
     * @throws ConfigException if required properties are missing
     */
    private void validateRequiredProperties() throws ConfigException {
        final String[] requiredProperties = {
            ConfigKeys.WINNING_POINTS,
            ConfigKeys.MAX_TOKENS,
            ConfigKeys.SETUP_2P_GEMS,
            ConfigKeys.SETUP_3P_GEMS,
            ConfigKeys.SETUP_4P_GEMS,
            ConfigKeys.SETUP_NOBLES_ADD
        };
        
        for (final String property : requiredProperties) {
            if (!hasProperty(property)) {
                throw new ConfigException("Missing required configuration property: " + property);
            }
        }
    }
}