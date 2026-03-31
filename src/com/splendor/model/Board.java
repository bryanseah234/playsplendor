/**
 * Represents the game board containing all shared game elements.
 * Manages the gem bank, card decks, and available nobles.
 * 
 */
package com.splendor.model;

import com.splendor.data.CardLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
        initializeNobles();
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
     * Replaces the entire gem bank with the provided map.
     * Used exclusively by the undo/restore mechanism to reinstate a prior bank state.
     *
     * @param gemBank The gem bank snapshot to restore.
     */
    public void setGemBank(final Map<Gem, Integer> gemBank) {
        this.gemBank.clear();
        this.gemBank.putAll(gemBank);
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
     * Replaces all face-up available cards with the provided tier-to-list map.
     * Used by the undo/restore mechanism to reset the board face-up state.
     *
     * @param availableCards Snapshot map of tier (1–3) to list of face-up cards.
     */
    public void setAvailableCards(final Map<Integer, List<Card>> availableCards) {
        this.availableCards.clear();
        for (final Map.Entry<Integer, List<Card>> entry : availableCards.entrySet()) {
            this.availableCards.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
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
     * Returns the number of face-down cards remaining in the specified tier's deck.
     *
     * @param tier Deck tier to query (1, 2, or 3).
     * @return Number of cards remaining; 0 if the tier is unknown or the deck is empty.
     */
    public int getDeckSize(final int tier) {
        final Queue<Card> deck = cardDecks.get(tier);
        return deck == null ? 0 : deck.size();
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
     * Replaces the list of available noble tiles with the provided list.
     * Used by the undo/restore mechanism to reset the noble state.
     *
     * @param availableNobles Snapshot list of noble tiles to restore.
     */
    public void setAvailableNobles(final List<Noble> availableNobles) {
        this.availableNobles.clear();
        this.availableNobles.addAll(availableNobles);
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

        final Card newCard = deck.poll();
        if (newCard != null && availableCards.get(tier).size() < 4) {
            availableCards.get(tier).add(newCard);
        }
        return newCard;
    }

    /**
     * Draws the top card from the specified tier's deck without placing it in the
     * face-up slots. Used for blind deck-reserve moves where the player takes the
     * card directly into their hand without it touching the board display.
     *
     * @param tier Deck tier to draw from (1, 2, or 3).
     * @return The drawn card, or null if the deck is empty.
     */
    public Card drawBlindCard(final int tier) {
        final Queue<Card> deck = cardDecks.get(tier);
        if (deck == null || deck.isEmpty()) {
            return null;
        }
        return deck.poll();
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
     * Returns a shallow copy of the internal card decks as a tier-to-list map.
     * Each list contains the remaining face-down cards in queue order.
     * Used by the snapshot mechanism to capture deck state for undo.
     *
     * @return A new map containing copied lists of remaining cards per tier.
     */
    public Map<Integer, List<Card>> copyDecks() {
        final Map<Integer, List<Card>> copy = new HashMap<>();
        for (final Map.Entry<Integer, Queue<Card>> entry : cardDecks.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Restores all tier decks from a previously captured snapshot map,
     * rebuilding the internal LinkedList queues from the snapshot lists.
     * Used by the undo/restore mechanism.
     *
     * @param decks Snapshot map of tier (1–3) to ordered card list.
     */
    public void restoreDecks(final Map<Integer, List<Card>> decks) {
        for (final Map.Entry<Integer, List<Card>> entry : decks.entrySet()) {
            cardDecks.put(entry.getKey(), new LinkedList<>(entry.getValue()));
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
            final List<Card> cards = CardLoader.loadCards(tier);
            cardDecks.put(tier, new LinkedList<>(cards));
            availableCards.put(tier, new ArrayList<>());
        }
    }
    
    /**
     * Initializes the available cards display for each tier.
     */
    private void initializeAvailableCards() {
        // Deal initial 4 face-up cards for each tier
        for (int tier = 1; tier <= 3; tier++) {
            for (int i = 0; i < 4; i++) {
                final Card card = drawBlindCard(tier);
                if (card != null) {
                    availableCards.get(tier).add(card);
                }
            }
        }
    }

    /**
     * Initializes the available nobles.
     */
    private void initializeNobles() {
        List<Noble> allNobles = CardLoader.loadNobles();
        int nobleCount = maxPlayers + 1;
        // Take the first N nobles
        if (allNobles.size() > nobleCount) {
            availableNobles.addAll(allNobles.subList(0, nobleCount));
        } else {
            availableNobles.addAll(allNobles);
        }
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
