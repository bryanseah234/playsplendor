// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Central loader for card and noble data in the Splendor game.
 * Provides a unified interface for loading game data from various sources.
 *
 */
package com.splendor.data;

import com.splendor.config.FileConfigProvider;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.ConfigException;
import com.splendor.exception.DataLoadException;
import com.splendor.model.Card;
import com.splendor.model.Noble;
import java.util.List;

/**
 * Facade for loading card and noble data into the game. Provides a simple
 * static API for CSV-based card and noble loading with fail-fast validation.
 *
 * @see CsvCardParser
 */
public final class CardLoader {


    private static volatile CsvCardParser instance;
    private static volatile IConfigProvider configProvider;
    private static final Object LOCK = new Object();

    /**
     * Private constructor to prevent instantiation. This class is not meant to
     * be instantiated.
     */
    private CardLoader() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Loads all cards for a specific tier.
     *
     * This method uses the configured CsvCardParser to load cards. If no
     * provider is configured, it creates a default CSV parser.
     *
     * @param tier The card tier to load (1, 2, or 3)
     * @return List of cards for the specified tier, shuffled
     * @throws RuntimeException if data cannot be loaded
     */
    public static List<Card> loadCards(final int tier) {
        try {
            final CsvCardParser provider = getProvider();
            return provider.loadCards(tier);
        } catch (final DataLoadException e) {
            throw new RuntimeException("Failed to load cards for tier " + tier, e);
        }
    }

    /**
     * Loads all available noble tiles.
     *
     * This method uses the configured CsvCardParser to load nobles. If no
     * provider is configured, it creates a default CSV parser.
     *
     * @return List of noble tiles, shuffled
     * @throws RuntimeException if data cannot be loaded
     */
    public static List<Noble> loadNobles() {
        try {
            final CsvCardParser provider = getProvider();
            return provider.loadNobles();
        } catch (final DataLoadException e) {
            throw new RuntimeException("Failed to load nobles", e);
        }
    }

    /**
     * Gets or creates the card data provider.
     *
     * If a custom provider has been set, it is returned. Otherwise, a default
     * CSV parser is created and returned.
     *
     * @return The card data provider to use
     * @throws DataLoadException if provider initialization fails
     */
    private static CsvCardParser getProvider() throws DataLoadException {
        CsvCardParser localInstance = instance;
        if (localInstance == null) {
            synchronized (LOCK) {
                localInstance = instance;
                if (localInstance == null) {
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
                    instance = localInstance = new CsvCardParser(configProvider);
                }
            }
        }
        return localInstance;
    }
}
