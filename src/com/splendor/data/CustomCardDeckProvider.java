/**
 * Custom card deck provider for runtime injection of user-defined decks.
 * Allows players to create and use custom card sets while maintaining game balance.
 * 
 */
package com.splendor.data;

import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides custom card decks injected at runtime.
 * 
 * This class allows users to programmatically define their own card sets
 * without modifying the base CSV files. It's useful for:
 * - Custom game variants
 * - Testing specific card combinations
 * - Educational purposes
 * 
 * The provider maintains separate card pools for each tier and ensures
 * proper shuffling and deck composition.
 * 
 * @see CardDataProvider Interface this implements
 * @see CsvCardParser Default CSV-based implementation
 */
public class CustomCardDeckProvider implements CardDataProvider {
    
    private final Map<Integer, List<Card>> customCards;
    private final List<Noble> customNobles;
    
    /**
     * Creates an empty custom deck provider.
     * 
     * Use {@link #addCard(Card)} and {@link #addNoble(Noble)} to populate
     * the custom decks before using them in a game.
     */
    public CustomCardDeckProvider() {
        this.customCards = new HashMap<>();
        this.customNobles = new ArrayList<>();
        
        // Initialize empty lists for each tier
        for (int tier = 1; tier <= 3; tier++) {
            customCards.put(tier, new ArrayList<>());
        }
    }
    
    /**
     * Creates a custom deck provider with predefined cards.
     * 
     * @param cards List of custom cards to use
     * @param nobles List of custom nobles to use
     */
    public CustomCardDeckProvider(final List<Card> cards, final List<Noble> nobles) {
        this();
        for (final Card card : cards) {
            addCard(card);
        }
        for (final Noble noble : nobles) {
            addNoble(noble);
        }
    }
    
    /**
     * Adds a card to the custom deck.
     * 
     * @param card Card to add
     * @return This provider for method chaining
     * @throws IllegalArgumentException if card tier is invalid
     */
    public CustomCardDeckProvider addCard(final Card card) {
        final int tier = card.getTier();
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Invalid card tier: " + tier);
        }
        customCards.get(tier).add(card);
        return this;
    }
    
    /**
     * Adds a noble to the custom deck.
     * 
     * @param noble Noble to add
     * @return This provider for method chaining
     */
    public CustomCardDeckProvider addNoble(final Noble noble) {
        customNobles.add(noble);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Card> loadCards(final int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Invalid tier: " + tier + ". Must be 1, 2, or 3.");
        }
        
        final List<Card> tierCards = new ArrayList<>(customCards.get(tier));
        
        if (tierCards.isEmpty()) {
            throw new IllegalStateException("No custom cards defined for tier " + tier);
        }
        
        // Duplicate cards if needed to reach reasonable deck size
        final int targetCount = 20; // Minimum viable deck size
        if (tierCards.size() < targetCount) {
            final List<Card> base = new ArrayList<>(tierCards);
            int nextId = tierCards.stream().mapToInt(Card::getId).max().orElse(0) + 1;
            for (int i = 0; tierCards.size() < targetCount; i++) {
                final Card baseCard = base.get(i % base.size());
                tierCards.add(new Card(nextId++, tier, baseCard.getPoints(), 
                                     baseCard.getBonusGem(), baseCard.getCost()));
            }
        }
        
        Collections.shuffle(tierCards);
        return tierCards;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Noble> loadNobles() {
        if (customNobles.isEmpty()) {
            // Provide default nobles if none are defined
            return createDefaultNobles();
        }
        
        final List<Noble> nobles = new ArrayList<>(customNobles);
        Collections.shuffle(nobles);
        return nobles;
    }
    
    /**
     * Gets the number of custom cards for a specific tier.
     * 
     * @param tier The card tier (1, 2, or 3)
     * @return Number of custom cards defined for this tier
     */
    public int getCardCount(final int tier) {
        return customCards.getOrDefault(tier, new ArrayList<>()).size();
    }
    
    /**
     * Gets the total number of custom cards across all tiers.
     * 
     * @return Total number of custom cards
     */
    public int getTotalCardCount() {
        return customCards.values().stream().mapToInt(List::size).sum();
    }
    
    /**
     * Gets the number of custom nobles.
     * 
     * @return Number of custom nobles defined
     */
    public int getNobleCount() {
        return customNobles.size();
    }
    
    /**
     * Clears all custom cards and nobles.
     * 
     * @return This provider for method chaining
     */
    public CustomCardDeckProvider clear() {
        customCards.clear();
        customNobles.clear();
        for (int tier = 1; tier <= 3; tier++) {
            customCards.put(tier, new ArrayList<>());
        }
        return this;
    }
    
    /**
     * Creates a set of default nobles for testing purposes.
     * 
     * @return List of 10 default nobles with standard requirements
     */
    private List<Noble> createDefaultNobles() {
        final List<Noble> nobles = new ArrayList<>();
        int id = 1;
        
        // Create simple nobles with various gem requirements
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
        
        return nobles;
    }
}
