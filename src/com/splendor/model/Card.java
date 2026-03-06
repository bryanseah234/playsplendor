/**
 * Represents a development card in the Splendor game.
 * Contains card properties including cost, points, tier, and gem bonuses.
 * 
 * @author Splendor Development Team
 * @version 1.0
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
    private int id;
    private int tier;
    private int points;
    private Gem bonusGem;
    private Map<Gem, Integer> cost;

    /**
     * Creates an empty card with default values.
     */
    public Card() {
        this(0, 0, 0, null, new HashMap<Gem, Integer>());
    }

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
     * Sets the unique card identifier.
     *
     * @param id Card ID
     */
    public void setId(final int id) {
        this.id = id;
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
     * Sets the card tier.
     *
     * @param tier Card tier
     */
    public void setTier(final int tier) {
        this.tier = tier;
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
     * Sets the victory points for this card.
     *
     * @param points Victory points
     */
    public void setPoints(final int points) {
        this.points = points;
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
     * Sets the bonus gem type for this card.
     *
     * @param bonusGem Bonus gem type
     */
    public void setBonusGem(final Gem bonusGem) {
        this.bonusGem = bonusGem;
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
     * Sets the cost map for this card.
     *
     * @param cost Cost map by gem
     */
    public void setCost(final Map<Gem, Integer> cost) {
        this.cost = cost == null ? new HashMap<Gem, Integer>() : new HashMap<Gem, Integer>(cost);
    }
    
    /**
     * Checks if this card provides any victory points.
     * 
     * @return true if card provides points, false otherwise
     */
    public boolean providesPoints() {
        return points > 0;
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
     * Gets the total number of gems required to purchase this card.
     * 
     * @return Total gem cost
     */
    public int getTotalCost() {
        return cost.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Checks if the card has a specific gem in its cost.
     * 
     * @param gem Gem type to check
     * @return true if card requires the specified gem, false otherwise
     */
    public boolean requiresGem(final Gem gem) {
        return cost.containsKey(gem) && cost.get(gem) > 0;
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
