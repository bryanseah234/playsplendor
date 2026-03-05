/**
 * Represents a development card in the Splendor game.
 * Contains card properties including cost, points, tier, and gem bonuses.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

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
     * Creates a new development card.
     * 
     * @param id Unique card identifier
     * @param tier Card tier (1, 2, or 3)
     * @param points Victory points provided by the card
     * @param bonusGem Gem type that this card provides as a discount
     * @param cost Map of gems and quantities required to purchase
     */
    public Card(final int id, final int tier, final int points, final Gem bonusGem, final Map<Gem, Integer> cost) {
        this.id = id;
        this.tier = tier;
        this.points = points;
        this.bonusGem = bonusGem;
        this.cost = new HashMap<>(cost);
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