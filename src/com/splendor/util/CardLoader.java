package com.splendor.util;

import com.splendor.config.*;
import com.splendor.model.*;

import java.io.*;
import java.util.*;

public class CardLoader {

    public CardLoader() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static List<Card> loadCards(int tier) {
        IConfigProvider config = getConfigProvider();

        String filePath = getCardsFilePath(config);

        int requiredCount = switch (tier) {
            case 1 -> config.getIntProperty(ConfigKeys.TIER1_CARD_COUNT, 40); //default back to 40/30/20
            case 2 -> config.getIntProperty(ConfigKeys.TIER2_CARD_COUNT, 30);
            case 3 -> config.getIntProperty(ConfigKeys.TIER3_CARD_COUNT, 20);
            default -> 0;
        };

        List<Card> tierCards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.startsWith("CARD")) {
                    continue;
                }
                Card c = parseCard(line);
                if (c.getTier() == tier) {
                    tierCards.add(c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cards file: " + filePath, e);
        }

        // Validate tier count against config — warn if CSV has too few cards
        validateCardCount(tier, tierCards.size(), requiredCount);

        Collections.shuffle(tierCards);
        return tierCards;
    }

    public static List<Noble> loadNobles() {
        IConfigProvider config = getConfigProvider();
        String filePath = getCardsFilePath(config);

        List<Noble> nobles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.startsWith("NOBLE")) {
                    continue;
                }
                nobles.add(parseNoble(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read nobles from file: " + filePath, e);
        }

        Collections.shuffle(nobles);
        return nobles;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Throws if the CSV has fewer cards than the tier requires, stopping the
     * game from starting in a broken state and pointing the user to the fix.
     */
    private static void validateCardCount(int tier, int actual, int required) {
        if (actual < required) {
            throw new RuntimeException(
                "\n [CONFIG WARNING] Tier " + tier + " card count mismatch: " +
                "card_data.csv contains " + actual + " cards, but config requires " + required + ". " +
                "Please add more cards to card_data.csv or lower " +
                "game.tier" + tier + ".card_count in config.properties."
            );
        }
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    /**
     * CSV format: CARD,id,tier,points,bonus_gem,cost_black,cost_white,cost_red,cost_blue,cost_green
     */
    private static Card parseCard(String line) {
        String[] p = line.split(",");
        int id       = Integer.parseInt(p[1].strip());
        int tier     = Integer.parseInt(p[2].strip());
        int points   = Integer.parseInt(p[3].strip());
        Gem bonusGem = Gem.valueOf(p[4].strip());
        Map<Gem, Integer> cost = new HashMap<>();
        addIfNonZero(cost, Gem.BLACK, p[5]);
        addIfNonZero(cost, Gem.WHITE, p[6]);
        addIfNonZero(cost, Gem.RED,   p[7]);
        addIfNonZero(cost, Gem.BLUE,  p[8]);
        addIfNonZero(cost, Gem.GREEN, p[9]);
        return new Card(id, tier, points, bonusGem, cost);
    }

    /**
     * CSV format: NOBLE,id,points,req_black,req_white,req_red,req_blue,req_green
     */
    private static Noble parseNoble(String line) {
        String[] p = line.split(",");
        int id     = Integer.parseInt(p[1].strip());
        int points = Integer.parseInt(p[2].strip());
        Map<Gem, Integer> cost = new HashMap<>();
        addIfNonZero(cost, Gem.BLACK, p[3]);
        addIfNonZero(cost, Gem.WHITE, p[4]);
        addIfNonZero(cost, Gem.RED,   p[5]);
        addIfNonZero(cost, Gem.BLUE,  p[6]);
        addIfNonZero(cost, Gem.GREEN, p[7]);
        return new Noble(id, points, cost);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addIfNonZero(Map<Gem, Integer> map, Gem gem, String raw) {
        int val = Integer.parseInt(raw.strip());
        if (val > 0) map.put(gem, val);
    }

    private static String getCardsFilePath(IConfigProvider config) {
        String path = config.getStringProperty(ConfigKeys.FILE_CARDS_DATA, null);
        if (path == null) {
            throw new RuntimeException("Missing config key: " + ConfigKeys.FILE_CARDS_DATA);
        }
        return path;
    }

    private static IConfigProvider getConfigProvider() {
        IConfigProvider provider = new FileConfigProvider();
        try {
            provider.loadConfiguration();
        } catch (ConfigException e) {
            throw new RuntimeException("CRITICAL: Failed to initialize game configuration!", e);
        }
        return provider;
    }
}
