/**
 * Central loader for card and noble data in the Splendor game.
 * Provides a unified interface for loading game data from various sources.
 * 
 */
package com.splendor.data;

import com.splendor.config.ConfigException;
import com.splendor.config.ConfigKeys;
import com.splendor.config.FileConfigProvider;
import com.splendor.config.IConfigProvider;
import com.splendor.model.Card;
import com.splendor.model.Noble;
import java.util.List;

/**
 * Facade for loading card and noble data into the game.
 * 
 * This class provides a simple static API for loading game data while
 * encapsulating the underlying data provider implementation. It supports:
 * - CSV-based data loading (default)
 * - Custom deck injection via CardDataProvider interface
 * - Fail-fast validation on startup
 * 
 * The loader automatically selects the appropriate data provider based
 * on configuration and handles fallback scenarios gracefully.
 * 
 * @see CardDataProvider Interface for custom data providers
 * @see CsvCardParser Default CSV implementation
 * @see CustomCardDeckProvider Custom deck injection
 */
public final class CardLoader {
    
    private static volatile CardDataProvider instance;
    private static volatile IConfigProvider configProvider;
    private static final Object LOCK = new Object();
    
    /**
     * Private constructor to prevent instantiation.
     * This class is not meant to be instantiated.
     */
    private CardLoader() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Loads all cards for a specific tier.
     * 
     * This method uses the configured CardDataProvider to load cards.
     * If no provider is configured, it creates a default CSV parser.
     * 
     * @param tier The card tier to load (1, 2, or 3)
     * @return List of cards for the specified tier, shuffled
     * @throws RuntimeException if data cannot be loaded
     */
    public static List<Card> loadCards(final int tier) {
        try {
            final CardDataProvider provider = getProvider();
            return provider.loadCards(tier);
        } catch (final DataLoadException e) {
            throw new RuntimeException("Failed to load cards for tier " + tier, e);
        }
    }
    
    /**
     * Loads all available noble tiles.
     * 
     * This method uses the configured CardDataProvider to load nobles.
     * If no provider is configured, it creates a default CSV parser.
     * 
     * @return List of noble tiles, shuffled
     * @throws RuntimeException if data cannot be loaded
     */
    public static List<Noble> loadNobles() {
        try {
            final CardDataProvider provider = getProvider();
            return provider.loadNobles();
        } catch (final DataLoadException e) {
            throw new RuntimeException("Failed to load nobles", e);
        }
    }
    
    /**
     * Sets a custom card data provider.
     * 
     * Use this method to inject custom decks at runtime. The provider
     * will be used for all subsequent card and noble loading operations.
     * 
     * @param provider Custom data provider to use
     * @throws IllegalArgumentException if provider is null
     */
    public static void setCustomProvider(final CardDataProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("CardDataProvider cannot be null");
        }
        synchronized (LOCK) {
            instance = provider;
        }
    }
    
    /**
     * Resets the data provider to the default CSV parser.
     * 
     * Use this method to clear any custom provider and return to
     * standard CSV-based data loading.
     */
    public static void resetToDefault() {
        synchronized (LOCK) {
            instance = null;
        }
    }
    
    /**
     * Validates the card data configuration and loads a sample.
     * 
     * This method performs a fail-fast validation of the data source
     * to ensure the game can start successfully. It should be called
     * during application initialization.
     * 
     * @throws ConfigException if validation fails
     */
    public static void validateConfiguration() throws ConfigException {
        try {
            final CardDataProvider provider = getProvider();
            // Try loading one card from each tier to validate
            for (int tier = 1; tier <= 3; tier++) {
                final List<Card> cards = provider.loadCards(tier);
                if (cards.isEmpty()) {
                    throw new ConfigException("No cards found for tier " + tier);
                }
            }
            // Try loading nobles
            final List<Noble> nobles = provider.loadNobles();
            if (nobles.isEmpty()) {
                throw new ConfigException("No nobles found in data source");
            }
        } catch (final DataLoadException e) {
            throw new ConfigException("Failed to validate card data: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            if (e.getCause() instanceof ConfigException) {
                throw (ConfigException) e.getCause();
            }
            throw new ConfigException("Card data validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the current configuration provider.
     * 
     * @return The configuration provider instance
     */
    public static IConfigProvider getConfigProvider() {
        if (configProvider == null) {
            synchronized (LOCK) {
                if (configProvider == null) {
                    configProvider = new FileConfigProvider();
                    try {
                        configProvider.loadConfiguration();
                    } catch (final ConfigException e) {
                        throw new RuntimeException("Failed to load configuration", e);
                    }
                }
            }
        }
        return configProvider;
    }
    
    /**
     * Gets or creates the card data provider.
     * 
     * If a custom provider has been set, it is returned. Otherwise,
     * a default CSV parser is created and returned.
     * 
     * @return The card data provider to use
     * @throws DataLoadException if provider initialization fails
     */
    private static CardDataProvider getProvider() throws DataLoadException {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    // Validate configuration first (fail-fast)
                    if (configProvider == null) {
                        configProvider = new FileConfigProvider();
                        try {
                            configProvider.loadConfiguration();
                        } catch (final ConfigException e) {
                            throw new DataLoadException(
                                "CRITICAL: Failed to initialize game configuration!", e);
                        }
                    }
                    
                    // Create default CSV parser
                    instance = new CsvCardParser(configProvider);
                }
            }
        }
        return instance;
    }
}
