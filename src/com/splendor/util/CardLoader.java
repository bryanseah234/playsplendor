package com.splendor.util;

import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to load initial game data (cards and nobles).
 */
public class CardLoader {

    /**
     * Generates a deck of cards for the specified tier.
     *
     * @param tier The tier of cards to generate (1, 2, or 3)
     * @return A shuffled list of cards
     */
    public static List<Card> loadCards(int tier) {
        List<Card> cards = new ArrayList<>();
        int idStart = (tier - 1) * 40 + 1; // ID offset for tiers

        switch (tier) {
            case 1:
                // Tier 1: Low cost, low points (mostly 0, some 1)
                cards.add(createCard(idStart++, 1, 0, Gem.BLACK, Map.of(Gem.BLUE, 1, Gem.GREEN, 1, Gem.RED, 1, Gem.WHITE, 1)));
                cards.add(createCard(idStart++, 1, 0, Gem.BLUE, Map.of(Gem.BLACK, 1, Gem.GREEN, 1, Gem.RED, 1, Gem.WHITE, 1)));
                cards.add(createCard(idStart++, 1, 0, Gem.WHITE, Map.of(Gem.BLUE, 1, Gem.GREEN, 1, Gem.RED, 1, Gem.BLACK, 1)));
                cards.add(createCard(idStart++, 1, 0, Gem.GREEN, Map.of(Gem.BLUE, 1, Gem.BLACK, 1, Gem.RED, 1, Gem.WHITE, 1)));
                cards.add(createCard(idStart++, 1, 0, Gem.RED, Map.of(Gem.BLUE, 1, Gem.GREEN, 1, Gem.BLACK, 1, Gem.WHITE, 1)));
                
                cards.add(createCard(idStart++, 1, 0, Gem.BLACK, Map.of(Gem.WHITE, 3)));
                cards.add(createCard(idStart++, 1, 0, Gem.BLUE, Map.of(Gem.BLACK, 3)));
                cards.add(createCard(idStart++, 1, 0, Gem.WHITE, Map.of(Gem.BLUE, 3)));
                cards.add(createCard(idStart++, 1, 0, Gem.GREEN, Map.of(Gem.RED, 3)));
                cards.add(createCard(idStart++, 1, 0, Gem.RED, Map.of(Gem.WHITE, 3)));

                cards.add(createCard(idStart++, 1, 1, Gem.BLACK, Map.of(Gem.BLUE, 4)));
                cards.add(createCard(idStart++, 1, 1, Gem.BLUE, Map.of(Gem.RED, 4)));
                cards.add(createCard(idStart++, 1, 1, Gem.WHITE, Map.of(Gem.GREEN, 4)));
                cards.add(createCard(idStart++, 1, 1, Gem.GREEN, Map.of(Gem.BLACK, 4)));
                cards.add(createCard(idStart++, 1, 1, Gem.RED, Map.of(Gem.WHITE, 4)));
                break;

            case 2:
                // Tier 2: Medium cost, 1-3 points
                cards.add(createCard(idStart++, 2, 1, Gem.BLACK, Map.of(Gem.BLUE, 3, Gem.GREEN, 2, Gem.RED, 2)));
                cards.add(createCard(idStart++, 2, 1, Gem.BLUE, Map.of(Gem.BLACK, 3, Gem.GREEN, 2, Gem.WHITE, 2)));
                cards.add(createCard(idStart++, 2, 1, Gem.WHITE, Map.of(Gem.RED, 3, Gem.BLACK, 2, Gem.BLUE, 2)));
                cards.add(createCard(idStart++, 2, 1, Gem.GREEN, Map.of(Gem.WHITE, 3, Gem.BLUE, 2, Gem.RED, 2)));
                cards.add(createCard(idStart++, 2, 1, Gem.RED, Map.of(Gem.BLACK, 3, Gem.WHITE, 2, Gem.GREEN, 2)));

                cards.add(createCard(idStart++, 2, 2, Gem.BLACK, Map.of(Gem.WHITE, 5)));
                cards.add(createCard(idStart++, 2, 2, Gem.BLUE, Map.of(Gem.BLUE, 5)));
                cards.add(createCard(idStart++, 2, 2, Gem.WHITE, Map.of(Gem.RED, 5)));
                cards.add(createCard(idStart++, 2, 2, Gem.GREEN, Map.of(Gem.GREEN, 5)));
                cards.add(createCard(idStart++, 2, 2, Gem.RED, Map.of(Gem.BLACK, 5)));

                cards.add(createCard(idStart++, 2, 3, Gem.BLACK, Map.of(Gem.WHITE, 6)));
                cards.add(createCard(idStart++, 2, 3, Gem.BLUE, Map.of(Gem.BLUE, 6)));
                cards.add(createCard(idStart++, 2, 3, Gem.WHITE, Map.of(Gem.RED, 6)));
                cards.add(createCard(idStart++, 2, 3, Gem.GREEN, Map.of(Gem.GREEN, 6)));
                cards.add(createCard(idStart++, 2, 3, Gem.RED, Map.of(Gem.BLACK, 6)));
                break;

            case 3:
                // Tier 3: High cost, 3-5 points
                cards.add(createCard(idStart++, 3, 3, Gem.BLACK, Map.of(Gem.WHITE, 3, Gem.BLUE, 3, Gem.GREEN, 5, Gem.RED, 3)));
                cards.add(createCard(idStart++, 3, 3, Gem.BLUE, Map.of(Gem.WHITE, 3, Gem.BLACK, 3, Gem.RED, 5, Gem.GREEN, 3)));
                cards.add(createCard(idStart++, 3, 3, Gem.WHITE, Map.of(Gem.BLACK, 3, Gem.BLUE, 3, Gem.RED, 3, Gem.GREEN, 5)));
                cards.add(createCard(idStart++, 3, 3, Gem.GREEN, Map.of(Gem.WHITE, 5, Gem.BLUE, 3, Gem.RED, 3, Gem.BLACK, 3)));
                cards.add(createCard(idStart++, 3, 3, Gem.RED, Map.of(Gem.WHITE, 3, Gem.BLUE, 5, Gem.GREEN, 3, Gem.BLACK, 3)));

                cards.add(createCard(idStart++, 3, 4, Gem.BLACK, Map.of(Gem.RED, 7)));
                cards.add(createCard(idStart++, 3, 4, Gem.BLUE, Map.of(Gem.WHITE, 7)));
                cards.add(createCard(idStart++, 3, 4, Gem.WHITE, Map.of(Gem.BLACK, 7)));
                cards.add(createCard(idStart++, 3, 4, Gem.GREEN, Map.of(Gem.BLUE, 7)));
                cards.add(createCard(idStart++, 3, 4, Gem.RED, Map.of(Gem.GREEN, 7)));

                cards.add(createCard(idStart++, 3, 5, Gem.BLACK, Map.of(Gem.RED, 7, Gem.BLACK, 3)));
                cards.add(createCard(idStart++, 3, 5, Gem.BLUE, Map.of(Gem.WHITE, 7, Gem.BLUE, 3)));
                cards.add(createCard(idStart++, 3, 5, Gem.WHITE, Map.of(Gem.BLACK, 7, Gem.WHITE, 3)));
                cards.add(createCard(idStart++, 3, 5, Gem.GREEN, Map.of(Gem.BLUE, 7, Gem.GREEN, 3)));
                cards.add(createCard(idStart++, 3, 5, Gem.RED, Map.of(Gem.GREEN, 7, Gem.RED, 3)));
                break;
        }

        final int targetCount;
        switch (tier) {
            case 1:
                targetCount = 40;
                break;
            case 2:
                targetCount = 30;
                break;
            case 3:
                targetCount = 20;
                break;
            default:
                targetCount = cards.size();
                break;
        }
        if (cards.size() < targetCount) {
            final List<Card> baseCards = new ArrayList<>(cards);
            int id = idStart;
            int index = 0;
            while (cards.size() < targetCount) {
                final Card base = baseCards.get(index % baseCards.size());
                cards.add(createCard(id++, tier, base.getPoints(), base.getBonusGem(), base.getCost()));
                index++;
            }
        }

        Collections.shuffle(cards);
        return cards;
    }

    /**
     * Generates a list of Nobles.
     *
     * @return A shuffled list of nobles
     */
    public static List<Noble> loadNobles() {
        List<Noble> nobles = new ArrayList<>();
        int id = 1;

        nobles.add(new Noble(id++, 3, Map.of(Gem.RED, 4, Gem.GREEN, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLACK, 4, Gem.WHITE, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLUE, 4, Gem.GREEN, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.RED, 4, Gem.BLACK, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLUE, 4, Gem.WHITE, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.RED, 3, Gem.GREEN, 3, Gem.BLUE, 3)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLACK, 3, Gem.WHITE, 3, Gem.RED, 3)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLUE, 3, Gem.GREEN, 3, Gem.WHITE, 3)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.GREEN, 4, Gem.WHITE, 4)));
        nobles.add(new Noble(id++, 3, Map.of(Gem.BLUE, 4, Gem.BLACK, 4)));
        
        Collections.shuffle(nobles);
        return nobles;
    }

    private static Card createCard(int id, int tier, int points, Gem bonusGem, Map<Gem, Integer> cost) {
        return new Card(id, tier, points, bonusGem, cost);
    }
}
