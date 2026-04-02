// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Represents a noble tile in the Splendor game.
 * Nobles are automatically awarded to players who meet their gem requirements.
 * 
 */
package com.splendor.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a noble tile that provides victory points.
 * Nobles are awarded automatically when a player's tableau meets their requirements.
 */
public class Noble {
    private final int id;
    private final int points;
    private final Map<Gem, Integer> requirements;

    /**
     * Creates a noble with the specified properties.
     *
     * @param id Noble ID
     * @param points Victory points
     * @param requirements Required gems map
     */
    public Noble(final int id, final int points, final Map<Gem, Integer> requirements) {
        this.id = id;
        this.points = points;
        this.requirements = requirements == null ? new HashMap<Gem, Integer>() : new HashMap<Gem, Integer>(requirements);
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
        if (playerGemCounts == null) {
            return false;
        }
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
     * Returns a string representation of the noble.
     *
     * @return Formatted noble description
     */
    @Override
    public String toString() {
        return String.format("Noble %d [%d pts, Requirements: %s]", id, points, requirements);
    }
}
