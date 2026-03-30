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
        // 1. Get our new config provider
        IConfigProvider config = getConfigProvider();

        // 2. Ask the provider for the file path (no more Properties!)
        String cardsFilePath = config.getStringProperty(ConfigKeys.FILE_CARDS_DATA, null);
        if (cardsFilePath == null) {
            throw new RuntimeException("Missing config key: " + ConfigKeys.FILE_CARDS_DATA);
        }

        // 3. Ask the provider for the integer directly (no more Integer.parseInt!)
        int targetCount = switch (tier) {
            case 1 -> config.getIntProperty(ConfigKeys.TIER1_CARD_COUNT, 40);
            case 2 -> config.getIntProperty(ConfigKeys.TIER2_CARD_COUNT, 30);
            case 3 -> config.getIntProperty(ConfigKeys.TIER3_CARD_COUNT, 20);
            default -> 0;
        };

        List<Card> tierCards = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(cardsFilePath))) {
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
            throw new RuntimeException("Failed to read cards file: " + cardsFilePath, e);
        }

        if (tierCards.size() < targetCount && !tierCards.isEmpty()) {
            List<Card> base = new ArrayList<>(tierCards);
            int id = nextIdAfter(tierCards);
            for (int i = 0; tierCards.size() < targetCount; i++) {
                Card b = base.get(i % base.size());
                tierCards.add(new Card(id++, tier, b.getPoints(), b.getBonusGem(), b.getCost()));
            }
        }

        Collections.shuffle(tierCards);
        return tierCards;
    }

    public static List<Noble> loadNobles() {
        IConfigProvider config = getConfigProvider();
        
        // Grab the exact same file path used for cards
        String dataFilePath = config.getStringProperty(ConfigKeys.FILE_CARDS_DATA, null);
        if (dataFilePath == null) {
            throw new RuntimeException("Missing config key: " + ConfigKeys.FILE_CARDS_DATA);
        }

        List<Noble> nobles = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#") || !line.startsWith("NOBLE")) {
                    continue;
                }
                
                nobles.add(parseNoble(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read nobles from file: " + dataFilePath, e);
        }
        
        Collections.shuffle(nobles);
        return nobles;
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private static Card parseCard(String value) {
        String[] parts = value.split("\\|");
        int id       = Integer.parseInt(parts[1].strip());
        int tier     = Integer.parseInt(parts[2].strip());
        int points   = Integer.parseInt(parts[3].strip());
        Gem bonusGem = Gem.valueOf(parts[4].strip());
        Map<Gem, Integer> cost = parseCost(parts[5].strip());
        return new Card(id, tier, points, bonusGem, cost);
    }

    private static Noble parseNoble(String value) {
        String[] parts = value.split("\\|");
        int id     = Integer.parseInt(parts[1].strip());
        int points = Integer.parseInt(parts[2].strip());
        Map<Gem, Integer> cost = parseCost(parts[3].strip());
        return new Noble(id, points, cost);
    }

    private static Map<Gem, Integer> parseCost(String costStr) {
        Map<Gem, Integer> cost = new HashMap<>();
        for (String entry : costStr.split(",")) {
            entry = entry.strip();
            if (entry.isEmpty()) continue;
            String[] kv = entry.split(":");
            cost.put(Gem.valueOf(kv[0].strip()), Integer.parseInt(kv[1].strip()));
        }
        return cost;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Instantiates and loads the new Configuration Provider.
     */
    private static IConfigProvider getConfigProvider() {
        IConfigProvider provider = new FileConfigProvider();
        try {
            // This triggers the file reading AND the validation checks!
            provider.loadConfiguration();
        } catch (ConfigException e) {
            // We catch your custom exception and wrap it so the program stops safely
            throw new RuntimeException("CRITICAL: Failed to initialize game configuration!", e);
        }
        return provider;
    }

    private static int nextIdAfter(List<Card> cards) {
        return cards.stream().mapToInt(Card::getId).max().orElse(0) + 1;
    }
}