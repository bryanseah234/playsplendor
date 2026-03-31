/**
 * Interface for providing card and noble data to the Splendor game.
 * Supports both built-in data sources and custom user-provided decks.
 * 
 */
package com.splendor.data;

import com.splendor.model.Card;
import com.splendor.model.Noble;
import java.util.List;

/**
 * Contract for card data providers in the Splendor game.
 * 
 * This interface enables the injection of custom card decks at runtime,
 * allowing users to create their own card sets while maintaining
 * compatibility with the base game mechanics.
 * 
 * Implementations must provide:
 * - Card loading by tier (1, 2, or 3)
 * - Noble tile loading
 * - Proper shuffling and deck composition
 * 
 * @see CsvCardParser Default CSV-based implementation
 */
public interface CardDataProvider {
    
    /**
     * Loads all cards for a specific tier.
     * 
     * The implementation should return a shuffled list of cards for the
     * specified tier. If the base card count is insufficient, implementations
     * may duplicate base cards to reach the target count.
     * 
     * @param tier The card tier to load (1, 2, or 3)
     * @return List of cards for the specified tier, shuffled
     * @throws IllegalArgumentException if tier is not 1, 2, or 3
     * @throws DataLoadException if card data cannot be loaded
     */
    List<Card> loadCards(int tier);
    
    /**
     * Loads all available noble tiles.
     * 
     * Returns a shuffled list of noble tiles that will be available
     * for the game. The actual number of nobles used is determined
     * by the player count (playerCount + 1).
     * 
     * @return List of noble tiles, shuffled
     * @throws DataLoadException if noble data cannot be loaded
     */
    List<Noble> loadNobles();
}
