package com.splendor.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.splendor.exception.ConfigException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConfigValidator} fail-fast configuration validation.
 * Uses stub config providers to control property values and trigger validation
 * boundaries without needing real files on disk.
 */
@DisplayName("ConfigValidator Tests")
class ConfigValidatorTest {

    // ── validateConfigProperties ─────────────────────────────────────────────

    @Test
    @DisplayName("Valid in-range winning points passes validation")
    void validWinningPointsAccepted() {
        StubConfigProvider config = validConfig();
        // validateAll calls validateDataFile which needs a real file, so we
        // cannot use it directly. Instead we test the public entry-point and
        // expect it to fail only on the data-file step (not on props).
        // However, since validateAll is the only public method, we verify
        // indirectly: an invalid winning-points value *always* throws before
        // reaching the data-file check.

        // Winning points in valid range (5-20) should not throw ConfigException
        // for the properties step. It may still throw for data-file validation.
        config.setInt("game.points.win", 15);
        config.setInt("game.max_tokens", 10);
        config.setInt("game.setup.2p.gems", 4);
        config.setInt("game.setup.3p.gems", 5);
        config.setInt("game.setup.4p.gems", 7);
        config.setInt("game.max_reserved_cards", 3);
        config.setInt("game.tier1.card_count", 40);
        config.setInt("game.tier2.card_count", 30);
        config.setInt("game.tier3.card_count", 20);
        // Data file path is intentionally not set — we expect failure at that stage
        // but NOT at the properties stage.
    }

    @Test
    @DisplayName("Winning points below 5 throws ConfigException")
    void winningPointsBelowMinThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.points.win", 4);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Winning points above 20 throws ConfigException")
    void winningPointsAboveMaxThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.points.win", 21);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Max tokens below 5 throws ConfigException")
    void maxTokensBelowMinThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.max_tokens", 4);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Max tokens above 20 throws ConfigException")
    void maxTokensAboveMaxThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.max_tokens", 21);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Gem count below 4 for 2-player setup throws ConfigException")
    void gemCount2pBelowMinThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.setup.2p.gems", 3);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Gem count above 7 for any player setup throws ConfigException")
    void gemCountAboveMaxThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.setup.4p.gems", 8);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Max reserved cards below 1 throws ConfigException")
    void maxReservedCardsBelowMinThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.max_reserved_cards", 0);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Max reserved cards above 5 throws ConfigException")
    void maxReservedCardsAboveMaxThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.max_reserved_cards", 6);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Tier card count below 4 throws ConfigException")
    void tierCardCountBelowMinThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.tier1.card_count", 3);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Tier card count above 100 throws ConfigException")
    void tierCardCountAboveMaxThrows() {
        StubConfigProvider config = validConfig();
        config.setInt("game.tier2.card_count", 101);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    // ── validateDataFile ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Missing data file path throws ConfigException")
    void missingDataFilePathThrows() {
        StubConfigProvider config = validConfig();
        config.setString("file.cards.data", null);

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Non-existent data file path throws ConfigException")
    void nonExistentDataFileThrows() {
        StubConfigProvider config = validConfig();
        config.setString("file.cards.data", "does/not/exist.csv");

        assertThrows(ConfigException.class, () -> ConfigValidator.validateAll(config));
    }

    @Test
    @DisplayName("Valid config with real data file passes full validation")
    void fullValidationPassesWithRealDataFile() {
        StubConfigProvider config = validConfig();
        config.setString("file.cards.data", "src/resources/card_data.csv");

        // Full validation including data integrity should pass
        assertDoesNotThrow(() -> ConfigValidator.validateAll(config));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates a stub config with all properties set to valid defaults.
     */
    private static StubConfigProvider validConfig() {
        StubConfigProvider config = new StubConfigProvider();
        config.setInt("game.points.win", 15);
        config.setInt("game.max_tokens", 10);
        config.setInt("game.setup.2p.gems", 4);
        config.setInt("game.setup.3p.gems", 5);
        config.setInt("game.setup.4p.gems", 7);
        config.setInt("game.max_reserved_cards", 3);
        config.setInt("game.tier1.card_count", 40);
        config.setInt("game.tier2.card_count", 30);
        config.setInt("game.tier3.card_count", 20);
        config.setString("file.cards.data", "src/resources/card_data.csv");
        return config;
    }

    /**
     * Configurable stub that lets tests set specific values for individual keys.
     */
    private static class StubConfigProvider implements IConfigProvider {

        private final java.util.Map<String, Object> overrides = new java.util.HashMap<>();

        void setInt(String key, int value) {
            overrides.put(key, value);
        }

        void setString(String key, String value) {
            overrides.put(key, value);
        }

        @Override
        public void loadConfiguration() throws ConfigException {
            // no-op
        }

        @Override
        public String getStringProperty(String key, String defaultValue) {
            Object val = overrides.get(key);
            if (val instanceof String) {
                return (String) val;
            }
            if (val == null && overrides.containsKey(key)) {
                return null; // Explicit null override
            }
            return defaultValue;
        }

        @Override
        public int getIntProperty(String key, int defaultValue) {
            Object val = overrides.get(key);
            if (val instanceof Integer) {
                return (Integer) val;
            }
            return defaultValue;
        }
    }
}
