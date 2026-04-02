// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * File-based configuration provider implementation.
 * Loads configuration properties from a properties file in the resources directory.
 * 
 */
package com.splendor.config;

import com.splendor.exception.ConfigException;
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
}