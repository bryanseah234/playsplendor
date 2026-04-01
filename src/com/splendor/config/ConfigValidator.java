/**
 * Validates game configuration and data files at startup.
 * Implements fail-fast validation to prevent runtime errors.
 * 
 */
package com.splendor.config;

import com.splendor.data.CardLoader;
import com.splendor.exception.ConfigException;
import com.splendor.exception.DataLoadException;
import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Performs fail-fast validation of game configuration and data files.
 * 
 * This validator ensures that:
 * - All required configuration properties are present and valid
 * - Card data files exist and are readable
 * - CSV data conforms to the expected schema
 * - Game can safely start without configuration-related runtime errors
 * 
 * If any validation fails, a ConfigException is thrown immediately,
 * preventing the application from starting in an invalid state.
 * 
 * Network bindings are blocked if validation fails to prevent
 * desynchronized multiplayer lobbies.
 */
public final class ConfigValidator {
    
private static final String FILE_PROPERTY = "file.cards.data";
    private static final String WINNING_POINTS_PROPERTY = "game.points.win";
    private static final String MAX_TOKENS_PROPERTY = "game.max_tokens";
    /**
     * Private constructor to prevent instantiation.
     */
    private ConfigValidator() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Performs comprehensive validation of all game configuration.
     * 
     * This method validates:
     * 1. Configuration file existence and readability
     * 2. Required property presence and value ranges
     * 3. Card data file existence and format
     * 4. Data integrity (cards can be loaded for all tiers)
     * 
     * @param configProvider The configuration provider to validate
     * @throws ConfigException if any validation fails
     */
    public static void validateAll(final IConfigProvider configProvider) throws ConfigException {
        validateConfigProperties(configProvider);
        validateDataFile(configProvider);
        validateDataIntegrity();
    }
    
    /**
     * Validates that all required configuration properties are present and valid.
     * 
     * @param configProvider The configuration provider to validate
     * @throws ConfigException if required properties are missing or invalid
     */
    private static void validateConfigProperties(final IConfigProvider configProvider) 
            throws ConfigException {
        // Validate winning points
        final int winningPoints = configProvider.getIntProperty(WINNING_POINTS_PROPERTY, -1);
        if (winningPoints < 5 || winningPoints > 20) {
            throw new ConfigException(String.format(
                "Invalid winning points value: %d. Must be between 5 and 20.", winningPoints));
        }

        // Validate max tokens
        final int maxTokens = configProvider.getIntProperty(MAX_TOKENS_PROPERTY, -1);
        if (maxTokens < 5 || maxTokens > 20) {
            throw new ConfigException(String.format(
                "Invalid max tokens value: %d. Must be between 5 and 20.", maxTokens));
        }

        // Validate gem setup per player count (4-7 gems per tier)
        final String[] setupKeys = {
            ConfigKeys.SETUP_2P_GEMS,
            ConfigKeys.SETUP_3P_GEMS,
            ConfigKeys.SETUP_4P_GEMS
        };
        for (final String key : setupKeys) {
            final int gems = configProvider.getIntProperty(key, -1);
            if (gems < 4 || gems > 7) {
                throw new ConfigException(String.format(
                    "Invalid gem count for %s: %d. Must be between 4 and 7.", key, gems));
            }
        }

        // Validate max reserved cards (1-5)
        final int maxReserved = configProvider.getIntProperty(ConfigKeys.MAX_RESERVED_CARDS, -1);
        if (maxReserved < 1 || maxReserved > 5) {
            throw new ConfigException(String.format(
                "Invalid max reserved cards: %d. Must be between 1 and 5.", maxReserved));
        }
        
        // Validate tier card counts
        for (int tier = 1; tier <= 3; tier++) {
            final String key = switch (tier) {
                case 1 -> ConfigKeys.TIER1_CARD_COUNT;
                case 2 -> ConfigKeys.TIER2_CARD_COUNT;
                case 3 -> ConfigKeys.TIER3_CARD_COUNT;
                default -> throw new IllegalStateException("Unexpected tier: " + tier); // Unreachable due to loop bounds
            };
            final int count = configProvider.getIntProperty(key, -1);
            if (count < 4 || count > 100) {
                throw new ConfigException(String.format(
                    "Invalid card count for tier %d: %d. Must be between 4 and 100.", tier, count));
            }
        }
    }
    
    /**
     * Validates that the card data file exists and is readable.
     * 
     * @param configProvider The configuration provider containing the file path
     * @throws ConfigException if the file doesn't exist or isn't readable
     */
    private static void validateDataFile(final IConfigProvider configProvider) 
            throws ConfigException {
        final String filePath = configProvider.getStringProperty(FILE_PROPERTY, null);
        if (filePath == null || filePath.isEmpty()) {
            throw new ConfigException("Missing required config property: " + FILE_PROPERTY);
        }
        
        final File dataFile = new File(filePath);
        if (!dataFile.exists()) {
            throw new ConfigException("Card data file not found: " + filePath);
        }
        if (!dataFile.canRead()) {
            throw new ConfigException("Card data file is not readable: " + filePath);
        }
        if (dataFile.length() == 0) {
            throw new ConfigException("Card data file is empty: " + filePath);
        }
    }
    
    /**
     * Validates that card data can be loaded successfully for all tiers.
     * 
     * This performs an actual load of card data to ensure the format is correct
     * and the data is complete. It uses the CardLoader which will throw
     * RuntimeException if there are any parsing or format issues.
     *
     * @throws ConfigException if data loading fails or data is incomplete
     */
    private static void validateDataIntegrity() throws ConfigException {
        try {
            // Validate cards can be loaded for each tier
            for (int tier = 1; tier <= 3; tier++) {
                final List<Card> cards = CardLoader.loadCards(tier);
                if (cards == null || cards.isEmpty()) {
                    throw new ConfigException(
                        String.format("No cards loaded for tier %d. Check data file format.", tier));
                }

                // Validate each card has required properties
                for (final Card card : cards) {
                    validateCard(card, tier);
                }
            }

            // Validate nobles can be loaded
            final List<Noble> nobles = CardLoader.loadNobles();
            if (nobles == null || nobles.isEmpty()) {
                throw new ConfigException("No nobles loaded. Check data file format.");
            }

            // Validate each noble has required properties
            for (final Noble noble : nobles) {
                validateNoble(noble);
            }
        } catch (final RuntimeException e) {
            if (e.getCause() instanceof ConfigException) {
                throw (ConfigException) e.getCause();
            }
            if (e.getCause() instanceof DataLoadException) {
                throw new ConfigException("Failed to load card data: " + e.getCause().getMessage(), e);
            }
            throw new ConfigException("Unexpected error during data validation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates a single card's properties.
     * 
     * @param card The card to validate
     * @param expectedTier The expected tier for this card
     * @throws ConfigException if the card is invalid
     */
    private static void validateCard(final Card card, final int expectedTier) 
            throws ConfigException {
        if (card == null) {
            throw new ConfigException("Null card found in data");
        }
        
        if (card.getTier() != expectedTier) {
            throw new ConfigException(String.format(
                "Card %d has tier %d but expected tier %d", 
                card.getId(), card.getTier(), expectedTier));
        }
        
        if (card.getPoints() < 0 || card.getPoints() > 5) {
            throw new ConfigException(String.format(
                "Card %d has invalid points: %d. Must be 0-5.", 
                card.getId(), card.getPoints()));
        }
        
        if (card.getBonusGem() != null) {
            validateGem(card.getBonusGem(), String.format("Card %d bonus gem", card.getId()));
        }
        
        if (card.getCost() != null) {
            for (final Map.Entry<Gem, Integer> entry : card.getCost().entrySet()) {
                validateGem(entry.getKey(), String.format("Card %d cost gem", card.getId()));
                if (entry.getValue() < 0 || entry.getValue() > 10) {
                    throw new ConfigException(String.format(
                        "Card %d has invalid cost for %s: %d. Must be 0-10.",
                        card.getId(), entry.getKey(), entry.getValue()));
                }
            }
        }
    }
    
    /**
     * Validates a single noble's properties.
     * 
     * @param noble The noble to validate
     * @throws ConfigException if the noble is invalid
     */
    private static void validateNoble(final Noble noble) throws ConfigException {
        if (noble == null) {
            throw new ConfigException("Null noble found in data");
        }
        
        if (noble.getPoints() != 3) {
            throw new ConfigException(String.format(
                "Noble %d has invalid points: %d. Must be 3.", 
                noble.getId(), noble.getPoints()));
        }
        
        if (noble.getRequirements() == null || noble.getRequirements().isEmpty()) {
            throw new ConfigException(String.format(
                "Noble %d has no requirements.", noble.getId()));
        }
        
        for (final Map.Entry<Gem, Integer> entry : noble.getRequirements().entrySet()) {
            validateGem(entry.getKey(), String.format("Noble %d requirement gem", noble.getId()));
            if (entry.getValue() < 0 || entry.getValue() > 10) {
                throw new ConfigException(String.format(
                    "Noble %d has invalid requirement for %s: %d. Must be 0-10.",
                    noble.getId(), entry.getKey(), entry.getValue()));
            }
        }
    }
    
    /**
     * Validates a gem enum value.
     * 
     * @param gem The gem to validate
     * @param context Description of where this gem was found (for error messages)
     * @throws ConfigException if the gem is null or is GOLD (not allowed in costs)
     */
    private static void validateGem(final Gem gem, final String context) 
            throws ConfigException {
        if (gem == null) {
            throw new ConfigException(context + " is null");
        }
        if (gem == Gem.GOLD) {
            throw new ConfigException(context + " cannot be GOLD");
        }
    }

}
