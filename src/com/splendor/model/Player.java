/**
 * Represents a player in the Splendor game.
 * Tracks player state including tokens, cards, reserved cards, and score.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a player in the game, tracking their resources and progress.
 * Maintains token inventory, purchased cards, reserved cards, and score.
 */
public class Player {
    private String name;
    private Map<Gem, Integer> tokens;
    private List<Card> purchasedCards;
    private List<Card> reservedCards;
    private List<Noble> nobles;

    /**
     * Creates a player with default values.
     */
    public Player() {
        this("Player", new HashMap<Gem, Integer>(), new ArrayList<Card>(), new ArrayList<Card>(), new ArrayList<Noble>());
    }

    /**
     * Creates a player with the specified name.
     *
     * @param name Player name
     */
    public Player(final String name) {
        this(name, new HashMap<Gem, Integer>(), new ArrayList<Card>(), new ArrayList<Card>(), new ArrayList<Noble>());
    }

    /**
     * Creates a player with the specified properties.
     *
     * @param name Player name
     * @param tokens Token inventory
     * @param purchasedCards Purchased cards list
     * @param reservedCards Reserved cards list
     * @param nobles Nobles list
     */
    public Player(final String name, final Map<Gem, Integer> tokens, final List<Card> purchasedCards,
                  final List<Card> reservedCards, final List<Noble> nobles) {
        Objects.requireNonNull(name, "name");
        this.name = name;
        this.tokens = tokens == null ? new HashMap<Gem, Integer>() : new HashMap<Gem, Integer>(tokens);
        this.purchasedCards = purchasedCards == null ? new ArrayList<Card>() : new ArrayList<Card>(purchasedCards);
        this.reservedCards = reservedCards == null ? new ArrayList<Card>() : new ArrayList<Card>(reservedCards);
        this.nobles = nobles == null ? new ArrayList<Noble>() : new ArrayList<Noble>(nobles);
        ensureTokenEntries();
    }

    private void ensureTokenEntries() {
        for (final Gem gem : Gem.values()) {
            tokens.putIfAbsent(gem, 0);
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
     * Sets the player's name.
     *
     * @param name Player name
     */
    public void setName(final String name) {
        this.name = name == null ? "" : name;
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
     * Sets the player's token inventory.
     *
     * @param tokens Token inventory
     */
    public void setTokens(final Map<Gem, Integer> tokens) {
        this.tokens.clear();
        if (tokens != null) {
            this.tokens.putAll(tokens);
        }
        ensureTokenEntries();
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
     * Sets the player's purchased cards list.
     *
     * @param cards Purchased cards list
     */
    public void setPurchasedCards(final List<Card> cards) {
        this.purchasedCards.clear();
        if (cards != null) {
            this.purchasedCards.addAll(cards);
        }
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
     * Sets the player's reserved cards list.
     *
     * @param cards Reserved cards list
     */
    public void setReservedCards(final List<Card> cards) {
        this.reservedCards.clear();
        if (cards != null) {
            this.reservedCards.addAll(cards);
        }
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
     * Sets the player's nobles list.
     *
     * @param nobles Nobles list
     */
    public void setNobles(final List<Noble> nobles) {
        this.nobles.clear();
        if (nobles != null) {
            this.nobles.addAll(nobles);
        }
    }
    
    /**
     * Gets the player's total victory points.
     * 
     * @return Total points from cards and nobles
     */
    public int getTotalPoints() {
        final int cardPoints = purchasedCards.stream().mapToInt(Card::getPoints).sum();
        final int noblePoints = nobles.stream().mapToInt(Noble::getPoints).sum();
        return cardPoints + noblePoints;
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
        tokens.merge(gem, quantity, Integer::sum);
    }
    
    /**
     * Removes tokens from the player's inventory.
     * 
     * @param gem Gem type to remove
     * @param quantity Number of tokens to remove
     * @throws IllegalArgumentException if player doesn't have enough tokens
     */
    public void removeTokens(final Gem gem, final int quantity) {
        final int currentCount = tokens.getOrDefault(gem, 0);
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
                           name, getTotalPoints(), getTotalTokenCount(), purchasedCards.size(),
                           reservedCards.size(), nobles.size());
    }
}
