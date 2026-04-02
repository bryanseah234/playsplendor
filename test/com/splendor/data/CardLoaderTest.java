package com.splendor.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CardLoader} — verifies that card and noble data
 * can be loaded from the CSV resource files and conforms to Splendor rules.
 *
 * <p>These tests rely on the real {@code src/resources/card_data.csv} file
 * being present on the classpath (copied to {@code classes/} during compilation).
 */
@DisplayName("CardLoader Tests")
class CardLoaderTest {

    @Test
    @DisplayName("Tier 1 cards can be loaded and are non-empty")
    void loadTier1Cards() {
        List<Card> cards = CardLoader.loadCards(1);

        assertNotNull(cards, "Tier 1 card list should not be null");
        assertFalse(cards.isEmpty(), "Tier 1 should contain cards");
    }

    @Test
    @DisplayName("Tier 2 cards can be loaded and are non-empty")
    void loadTier2Cards() {
        List<Card> cards = CardLoader.loadCards(2);

        assertNotNull(cards, "Tier 2 card list should not be null");
        assertFalse(cards.isEmpty(), "Tier 2 should contain cards");
    }

    @Test
    @DisplayName("Tier 3 cards can be loaded and are non-empty")
    void loadTier3Cards() {
        List<Card> cards = CardLoader.loadCards(3);

        assertNotNull(cards, "Tier 3 card list should not be null");
        assertFalse(cards.isEmpty(), "Tier 3 should contain cards");
    }

    @Test
    @DisplayName("All loaded cards have correct tier values")
    void cardsHaveCorrectTier() {
        for (int tier = 1; tier <= 3; tier++) {
            List<Card> cards = CardLoader.loadCards(tier);
            for (Card card : cards) {
                assertEquals(tier, card.getTier(),
                        "Card " + card.getId() + " should be tier " + tier);
            }
        }
    }

    @Test
    @DisplayName("All loaded cards have non-negative points")
    void cardsHaveNonNegativePoints() {
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : CardLoader.loadCards(tier)) {
                assertTrue(card.getPoints() >= 0,
                        "Card " + card.getId() + " should have non-negative points");
            }
        }
    }

    @Test
    @DisplayName("All loaded cards have a non-null bonus gem")
    void cardsHaveBonusGem() {
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : CardLoader.loadCards(tier)) {
                assertNotNull(card.getBonusGem(),
                        "Card " + card.getId() + " should have a bonus gem");
            }
        }
    }

    @Test
    @DisplayName("Card bonus gems are never GOLD")
    void cardBonusGemsAreNotGold() {
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : CardLoader.loadCards(tier)) {
                if (card.getBonusGem() != null) {
                    assertTrue(card.getBonusGem() != Gem.GOLD,
                            "Card " + card.getId() + " bonus gem should not be GOLD");
                }
            }
        }
    }

    @Test
    @DisplayName("Card costs contain only non-GOLD gems with positive counts")
    void cardCostsAreValid() {
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : CardLoader.loadCards(tier)) {
                if (card.getCost() != null) {
                    for (var entry : card.getCost().entrySet()) {
                        assertTrue(entry.getKey() != Gem.GOLD,
                                "Card " + card.getId() + " cost should not include GOLD");
                        assertTrue(entry.getValue() >= 0,
                                "Card " + card.getId() + " should have non-negative cost");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("All card IDs are unique across all tiers")
    void cardIdsAreUnique() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : CardLoader.loadCards(tier)) {
                assertTrue(ids.add(card.getId()),
                        "Duplicate card ID found: " + card.getId());
            }
        }
    }

    // ── Noble tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Nobles can be loaded and are non-empty")
    void loadNobles() {
        List<Noble> nobles = CardLoader.loadNobles();

        assertNotNull(nobles, "Noble list should not be null");
        assertFalse(nobles.isEmpty(), "Should have at least one noble");
    }

    @Test
    @DisplayName("All nobles are worth 3 prestige points")
    void noblesAreWorthThreePoints() {
        for (Noble noble : CardLoader.loadNobles()) {
            assertEquals(3, noble.getPoints(),
                    "Noble " + noble.getId() + " should be worth 3 points");
        }
    }

    @Test
    @DisplayName("All nobles have non-empty requirements")
    void noblesHaveRequirements() {
        for (Noble noble : CardLoader.loadNobles()) {
            assertNotNull(noble.getRequirements(),
                    "Noble " + noble.getId() + " should have requirements");
            assertFalse(noble.getRequirements().isEmpty(),
                    "Noble " + noble.getId() + " requirements should not be empty");
        }
    }

    @Test
    @DisplayName("Noble requirements do not include GOLD gem")
    void nobleRequirementsDoNotIncludeGold() {
        for (Noble noble : CardLoader.loadNobles()) {
            for (Gem gem : noble.getRequirements().keySet()) {
                assertTrue(gem != Gem.GOLD,
                        "Noble " + noble.getId() + " requirement should not include GOLD");
            }
        }
    }
}
