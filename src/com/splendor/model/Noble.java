/**
 * Represents a noble tile in the Splendor game.
 * Nobles are automatically awarded to players who meet their gem requirements.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a noble tile that provides victory points.
 * Nobles are awarded automatically when a player's tableau meets their requirements.
 */
public class Noble {
    
    private final int id;
    private final int points;
    private final Map<Gem, Integer> requirements;
    
    /**
     * Creates a new noble tile.
     * 
     * @param id Unique noble identifier
     * @param points Victory points provided by the noble
     * @param requirements Map of gem types and quantities required
     */
    public Noble(final int id, final int points, final Map<Gem, Integer> requirements) {
        this.id = id;
        this.points = points;
        this.requirements = new HashMap<>(requirements);
    }
    
    /**
     * Gets the unique noble identifier.
     * 
     * @return Noble ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the victory points provided by this noble.
     * 
     * @return Victory points
     */
    public int getPoints() {
        return points;
    }
    
    /**
     * Gets the gem requirements for this noble.
     * 
     * @return Unmodifiable map of required gems and quantities
     */
    public Map<Gem, Integer> getRequirements() {
        return Collections.unmodifiableMap(requirements);
    }
    
    /**
     * Checks if a player's tableau meets this noble's requirements.
     * 
     * @param playerGemCounts Map of gems the player has from their tableau
     * @return true if requirements are met, false otherwise
     */
    public boolean requirementsMet(final Map<Gem, Integer> playerGemCounts) {
        for (final Map.Entry<Gem, Integer> requirement : requirements.entrySet()) {
            final Gem gem = requirement.getKey();
            final int requiredCount = requirement.getValue();
            final int playerCount = playerGemCounts.getOrDefault(gem, 0);
            
            if (playerCount < requiredCount) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the total number of gems required.
     * 
     * @return Total gem requirement count
     */
    public int getTotalRequirementCount() {
        return requirements.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Returns a string representation of the noble.
     * 
     * @return Formatted noble description
     */
    @Override
    public String toString() {
        return String.format("Noble %d [%d pts, Requirements: %s]", id, points, requirements);
    }
}