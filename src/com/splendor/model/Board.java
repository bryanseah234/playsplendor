/**
 * Represents the game board containing all shared game elements.
 * Manages the gem bank, card decks, and available nobles.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Represents the game board containing all shared resources and cards.
 * Manages the gem bank, card decks for each tier, and available noble tiles.
 */
public class Board {
    
    private final Map<Gem, Integer> gemBank;
    private final Map<Integer, Queue<Card>> cardDecks; // Tier -> Card queue
    private final Map<Integer, List<Card>> availableCards; // Tier -> Available cards
    private final List<Noble> availableNobles;
    private final int maxPlayers;
    
    /**
     * Creates a new board for the specified number of players.
     * 
     * @param maxPlayers Maximum number of players (2-4)
     */
    public Board(final int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.gemBank = new HashMap<>();
        this.cardDecks = new HashMap<>();
        this.availableCards = new HashMap<>();
        this.availableNobles = new ArrayList<>();
        
        initializeGemBank();
        initializeCardDecks();
        initializeAvailableCards();
    }
    
    /**
     * Gets the current gem counts in the bank.
     * 
     * @return Unmodifiable map of gems and quantities
     */
    public Map<Gem, Integer> getGemBank() {
        return Collections.unmodifiableMap(gemBank);
    }
    
    /**
     * Gets the count of a specific gem in the bank.
     * 
     * @param gem Gem type to check
     * @return Quantity available in bank
     */
    public int getGemCount(final Gem gem) {
        return gemBank.getOrDefault(gem, 0);
    }
    
    /**
     * Gets the available cards for each tier.
     * 
     * @return Unmodifiable map of tier to available cards
     */
    public Map<Integer, List<Card>> getAvailableCards() {
        return Collections.unmodifiableMap(availableCards);
    }
    
    /**
     * Gets the available cards for a specific tier.
     * 
     * @param tier Tier to check (1, 2, or 3)
     * @return Unmodifiable list of available cards
     */
    public List<Card> getAvailableCards(final int tier) {
        return Collections.unmodifiableList(availableCards.getOrDefault(tier, new ArrayList<>()));
    }
    
    /**
     * Gets the available nobles.
     * 
     * @return Unmodifiable list of available nobles
     */
    public List<Noble> getAvailableNobles() {
        return Collections.unmodifiableList(availableNobles);
    }
    
    /**
     * Removes gems from the bank.
     * 
     * @param gems Map of gems and quantities to remove
     * @throws IllegalArgumentException if insufficient gems available
     */
    public void removeGems(final Map<Gem, Integer> gems) {
        for (final Map.Entry<Gem, Integer> entry : gems.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();
            final int currentCount = gemBank.getOrDefault(gem, 0);
            
            if (currentCount < quantity) {
                throw new IllegalArgumentException("Insufficient " + gem + " gems in bank");
            }
            
            gemBank.put(gem, currentCount - quantity);
        }
    }
    
    /**
     * Adds gems to the bank.
     * 
     * @param gems Map of gems and quantities to add
     */
    public void addGems(final Map<Gem, Integer> gems) {
        for (final Map.Entry<Gem, Integer> entry : gems.entrySet()) {
            final Gem gem = entry.getKey();
            final int quantity = entry.getValue();
            final int currentCount = gemBank.getOrDefault(gem, 0);
            
            gemBank.put(gem, currentCount + quantity);
        }
    }
    
    /**
     * Draws a card from the specified tier deck.
     * 
     * @param tier Tier to draw from (1, 2, or 3)
     * @return Drawn card or null if deck is empty
     */
    public Card drawCard(final int tier) {
        final Queue<Card> deck = cardDecks.get(tier);
        if (deck == null || deck.isEmpty()) {
            return null;
        }
        
        final Card drawnCard = deck.poll();
        
        // Replace the drawn card with a new one from the deck if available
        if (!deck.isEmpty() && availableCards.get(tier).size() < 4) {
            final Card newCard = deck.poll();
            if (newCard != null) {
                availableCards.get(tier).add(newCard);
            }
        }
        
        return drawnCard;
    }
    
    /**
     * Removes a card from the available cards.
     * 
     * @param tier Tier of the card
     * @param card Card to remove
     * @return true if card was found and removed, false otherwise
     */
    public boolean removeAvailableCard(final int tier, final Card card) {
        final List<Card> tierCards = availableCards.get(tier);
        if (tierCards != null) {
            return tierCards.remove(card);
        }
        return false;
    }
    
    /**
     * Adds a card to the available cards for a tier.
     * 
     * @param tier Tier to add card to
     * @param card Card to add
     */
    public void addAvailableCard(final int tier, final Card card) {
        final List<Card> tierCards = availableCards.get(tier);
        if (tierCards != null) {
            tierCards.add(card);
        }
    }
    
    /**
     * Removes a noble from the available nobles.
     * 
     * @param noble Noble to remove
     * @return true if noble was found and removed, false otherwise
     */
    public boolean removeAvailableNoble(final Noble noble) {
        return availableNobles.remove(noble);
    }
    
    /**
     * Initializes the gem bank based on player count.
     */
    private void initializeGemBank() {
        // Standard gem counts based on player count
        final int baseGemCount = getBaseGemCountForPlayers(maxPlayers);
        
        for (final Gem gem : Gem.values()) {
            if (gem == Gem.GOLD) {
                gemBank.put(gem, 5); // Always 5 gold tokens
            } else {
                gemBank.put(gem, baseGemCount);
            }
        }
    }
    
    /**
     * Initializes empty card decks for each tier.
     */
    private void initializeCardDecks() {
        for (int tier = 1; tier <= 3; tier++) {
            cardDecks.put(tier, new LinkedList<>());
            availableCards.put(tier, new ArrayList<>());
        }
    }
    
    /**
     * Initializes the available cards display for each tier.
     */
    private void initializeAvailableCards() {
        // This would typically load cards from a data source
        // For now, we'll leave the decks empty to be populated later
    }
    
    /**
     * Gets the base gem count for the specified number of players.
     * 
     * @param playerCount Number of players
     * @return Base gem count per color
     */
    private int getBaseGemCountForPlayers(final int playerCount) {
        switch (playerCount) {
            case 2:
                return 4;
            case 3:
                return 5;
            case 4:
                return 7;
            default:
                return 4; // Default to 2-player setup
        }
    }
    
    /**
     * Returns a string representation of the board state.
     * 
     * @return Board summary
     */
    @Override
    public String toString() {
        return String.format("Board [Gems: %s, Available Cards: %s, Nobles: %d]",
                           gemBank, availableCards, availableNobles.size());
    }
}