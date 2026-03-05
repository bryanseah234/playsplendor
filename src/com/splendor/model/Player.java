/**
 * Represents a player in the Splendor game.
 * Tracks player state including tokens, cards, reserved cards, and score.
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

/**
 * Represents a player in the game, tracking their resources and progress.
 * Maintains token inventory, purchased cards, reserved cards, and score.
 */
public class Player {
    
    private final String name;
    private final Map<Gem, Integer> tokens;
    private final List<Card> purchasedCards;
    private final List<Card> reservedCards;
    private final List<Noble> nobles;
    private int totalPoints;
    
    /**
     * Creates a new player with the specified name.
     * 
     * @param name Player name
     */
    public Player(final String name) {
        this.name = name;
        this.tokens = new HashMap<>();
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.nobles = new ArrayList<>();
        this.totalPoints = 0;
        
        // Initialize token counts to zero for all gem types
        for (final Gem gem : Gem.values()) {
            tokens.put(gem, 0);
        }
    }
    
    /**
     * Gets the player's name.
     * 
     * @return Player name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the player's token inventory.
     * 
     * @return Unmodifiable map of gems and quantities
     */
    public Map<Gem, Integer> getTokens() {
        return Collections.unmodifiableMap(tokens);
    }
    
    /**
     * Gets the count of a specific gem type.
     * 
     * @param gem Gem type to check
     * @return Quantity of the specified gem
     */
    public int getTokenCount(final Gem gem) {
        return tokens.getOrDefault(gem, 0);
    }
    
    /**
     * Gets the total number of tokens the player possesses.
     * 
     * @return Total token count
     */
    public int getTotalTokenCount() {
        return tokens.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Gets the player's purchased cards.
     * 
     * @return Unmodifiable list of purchased cards
     */
    public List<Card> getPurchasedCards() {
        return Collections.unmodifiableList(purchasedCards);
    }
    
    /**
     * Gets the player's reserved cards.
     * 
     * @return Unmodifiable list of reserved cards
     */
    public List<Card> getReservedCards() {
        return Collections.unmodifiableList(reservedCards);
    }
    
    /**
     * Gets the nobles awarded to the player.
     * 
     * @return Unmodifiable list of nobles
     */
    public List<Noble> getNobles() {
        return Collections.unmodifiableList(nobles);
    }
    
    /**
     * Gets the player's total victory points.
     * 
     * @return Total points from cards and nobles
     */
    public int getTotalPoints() {
        return totalPoints;
    }
    
    /**
     * Gets the gem discounts provided by purchased cards.
     * 
     * @return Map of gem types and discount counts
     */
    public Map<Gem, Integer> getGemDiscounts() {
        final Map<Gem, Integer> discounts = new HashMap<>();
        
        for (final Gem gem : Gem.values()) {
            discounts.put(gem, 0);
        }
        
        for (final Card card : purchasedCards) {
            if (card.providesDiscount()) {
                final Gem bonusGem = card.getBonusGem();
                discounts.put(bonusGem, discounts.get(bonusGem) + 1);
            }
        }
        
        return discounts;
    }
    
    /**
     * Adds tokens to the player's inventory.
     * 
     * @param gem Gem type to add
     * @param quantity Number of tokens to add
     */
    public void addTokens(final Gem gem, final int quantity) {
        final int currentCount = tokens.get(gem);
        tokens.put(gem, currentCount + quantity);
    }
    
    /**
     * Removes tokens from the player's inventory.
     * 
     * @param gem Gem type to remove
     * @param quantity Number of tokens to remove
     * @throws IllegalArgumentException if player doesn't have enough tokens
     */
    public void removeTokens(final Gem gem, final int quantity) {
        final int currentCount = tokens.get(gem);
        if (currentCount < quantity) {
            throw new IllegalArgumentException("Insufficient " + gem + " tokens");
        }
        tokens.put(gem, currentCount - quantity);
    }
    
    /**
     * Adds a purchased card to the player's tableau.
     * 
     * @param card Card to add
     */
    public void addPurchasedCard(final Card card) {
        purchasedCards.add(card);
        totalPoints += card.getPoints();
    }
    
    /**
     * Adds a card to the player's reserved cards.
     * 
     * @param card Card to reserve
     */
    public void addReservedCard(final Card card) {
        reservedCards.add(card);
    }
    
    /**
     * Removes a card from reserved cards (when purchased).
     * 
     * @param card Card to remove from reserved
     * @return true if card was found and removed, false otherwise
     */
    public boolean removeReservedCard(final Card card) {
        return reservedCards.remove(card);
    }
    
    /**
     * Adds a noble to the player's collection.
     * 
     * @param noble Noble to add
     */
    public void addNoble(final Noble noble) {
        nobles.add(noble);
        totalPoints += noble.getPoints();
    }
    
    /**
     * Checks if the player can reserve more cards.
     * 
     * @return true if player has less than 3 reserved cards, false otherwise
     */
    public boolean canReserveCard() {
        return reservedCards.size() < 3;
    }
    
    /**
     * Checks if the player has any reserved cards.
     * 
     * @return true if player has reserved cards, false otherwise
     */
    public boolean hasReservedCards() {
        return !reservedCards.isEmpty();
    }
    
    /**
     * Returns a string representation of the player.
     * 
     * @return Player summary
     */
    @Override
    public String toString() {
        return String.format("Player: %s (Points: %d, Tokens: %d, Cards: %d, Reserved: %d, Nobles: %d)",
                           name, totalPoints, getTotalTokenCount(), purchasedCards.size(),
                           reservedCards.size(), nobles.size());
    }
}