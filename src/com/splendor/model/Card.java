/**
 * Represents a development card in the Splendor game.
 * Contains card properties including cost, points, tier, and gem bonuses.
 * 
 */
package com.splendor.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a development card that players can purchase.
 * Cards provide points, gem discounts, and contribute to noble requirements.
 */
public class Card {
    private final int id;
    private final int tier;
    private final int points;
    private final Gem bonusGem;
    private final Map<Gem, Integer> cost;

    /**
     * Creates a card with the specified properties.
     *
     * @param id Card ID
     * @param tier Card tier
     * @param points Victory points
     * @param bonusGem Bonus gem type
     * @param cost Cost map by gem
     */
    public Card(final int id, final int tier, final int points, final Gem bonusGem, final Map<Gem, Integer> cost) {
        this.id = id;
        this.tier = tier;
        this.points = points;
        this.bonusGem = bonusGem;
        this.cost = cost == null ? new HashMap<Gem, Integer>() : new HashMap<Gem, Integer>(cost);
    }

    /**
     * Gets the unique card identifier.
     *
     * @return Card ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the card tier (1, 2, or 3).
     *
     * @return Card tier
     */
    public int getTier() {
        return tier;
    }

    /**
     * Gets the victory points provided by this card.
     *
     * @return Victory points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Gets the gem type that this card provides as a discount.
     *
     * @return Bonus gem type
     */
    public Gem getBonusGem() {
        return bonusGem;
    }

    /**
     * Gets the cost to purchase this card.
     *
     * @return Unmodifiable map of required gems and quantities
     */
    public Map<Gem, Integer> getCost() {
        return Collections.unmodifiableMap(cost);
    }

    /**
     * Checks if this card provides a gem discount.
     *
     * @return true if card provides a bonus gem, false otherwise
     */
    public boolean providesDiscount() {
        return bonusGem != null;
    }

    /**
     * Returns a string representation of the card.
     *
     * @return Formatted card description
     */
    @Override
    public String toString() {
        return String.format("Card %d [Tier %d, %d pts, Bonus: %s, Cost: %s]", 
                           id, tier, points, bonusGem, cost);
    }
}
