// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Represents a player in the Splendor game.
 * Tracks player state including tokens, cards, reserved cards, and score.
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
     * Creates a player with the specified name.
     *
     * @param name Player name
     */
    public Player(final String name) {
        this(name, null, null, null, null);
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
     * Gets the name of the player.
     *
     * @return The player's name
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
        int total = 0;
        for (final int count : tokens.values()) {
            total += count;
        }
        return total;
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
        int cardPoints = 0;
        for (final Card card : purchasedCards) {
            cardPoints += card.getPoints();
        }
        int noblePoints = 0;
        for (final Noble noble : nobles) {
            noblePoints += noble.getPoints();
        }
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
        final int existing = tokens.getOrDefault(gem, 0);
        tokens.put(gem, existing + quantity);
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
     * @param maxReserved maximum number of reserved cards allowed
     * @return true if player has fewer than maxReserved reserved cards
     */
    public boolean canReserveCard(final int maxReserved) {
        return reservedCards.size() < maxReserved;
    }

    /**
     * Package-private method to restore full player state for undo operations.
     * Keeps state encapsulated from outside the model package.
        *
        * @param restoredName      Snapshot player name.
        * @param restoredTokens    Snapshot token counts.
        * @param restoredPurchased Snapshot purchased cards.
        * @param restoredReserved  Snapshot reserved cards.
        * @param restoredNobles    Snapshot nobles claimed by the player.
     */
    void restoreState(final String restoredName, final Map<Gem, Integer> restoredTokens,
                      final List<Card> restoredPurchased, final List<Card> restoredReserved,
                      final List<Noble> restoredNobles) {
        this.name = restoredName;
        this.tokens.clear();
        if (restoredTokens != null) {
            this.tokens.putAll(restoredTokens);
        }
        ensureTokenEntries();

        this.purchasedCards.clear();
        if (restoredPurchased != null) {
            this.purchasedCards.addAll(restoredPurchased);
        }

        this.reservedCards.clear();
        if (restoredReserved != null) {
            this.reservedCards.addAll(restoredReserved);
        }

        this.nobles.clear();
        if (restoredNobles != null) {
            this.nobles.addAll(restoredNobles);
        }
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
