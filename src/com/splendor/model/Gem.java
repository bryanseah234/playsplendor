/**
 * Enumeration of gem types in the Splendor game.
 * Defines the five standard gem colors plus gold (wild card).
 */
package com.splendor.model;

/**
 * Enumeration of all gem types available in Splendor.
 * 
 * Gems are the core resource in Splendor, used both as currency for purchasing cards
 * and as permanent bonuses (discounts) that reduce the cost of future purchases.
 * Each gem type corresponds to a colored token in the physical game.
 * 
 * <p>The six gem types are:
 * <ul>
 *   <li>{@link #RED} - Red gem token</li>
 *   <li>{@link #GREEN} - Green gem token</li>
 *   <li>{@link #BLUE} - Blue gem token</li>
 *   <li>{@link #WHITE} - White gem token</li>
 *   <li>{@link #BLACK} - Black gem token</li>
 *   <li>{@link #GOLD} - Gold token (wild card, used only for reservations)</li>
 * </ul>
 * 
 * <p>Gems serve two purposes in the game:
 * <ol>
 *   <li><b>Temporary tokens</b>: Taken from the bank during a player's turn, spent to buy cards</li>
 *   <li><b>Permanent discounts</b>: Earned when purchasing development cards, permanently reduce
 *       the cost of future purchases of the same color</li>
 * </ol>
 * 
 * <p>The gold gem ({@link #GOLD}) is special: it acts as a wild card that can substitute
 * for any other gem type, but can only be obtained when reserving a card.
 * 
 * @see Card#getCost() For gem costs of development cards
 * @see Noble#requirementsMet(Map) For gem requirements of noble tiles
 */
public enum Gem {
    RED("Red"),
    GREEN("Green"), 
    BLUE("Blue"),
    WHITE("White"),
    BLACK("Black"),
    GOLD("Gold");
    
    private final String displayName;
    
    /**
     * Creates a new Gem with the specified display name.
     * 
     * @param displayName Human-readable name for the gem
     */
    Gem(final String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the gem.
     * 
     * @return Human-readable gem name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns a string representation of the gem.
     * 
     * @return Display name of the gem
     */
    @Override
    public String toString() {
        return displayName;
    }
}