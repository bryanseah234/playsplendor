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
    RED("Red", "R"),
    GREEN("Green", "G"),
    BLUE("Blue", "B"),
    WHITE("White", "W"),
    BLACK("Black", "K"),
    GOLD("Gold", "Au");

    private final String displayName;
    private final String label;

    /**
     * Creates a new Gem with the specified display name and abbreviation label.
     *
     * @param displayName Human-readable name for the gem
     * @param label Single or two-character abbreviation used in UI display
     */
    Gem(final String displayName, final String label) {
        this.displayName = displayName;
        this.label = label;
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
     * Gets the single or two-character label for the gem.
     * Used for compact gem token display in the UI.
     *
     * @return Abbreviation: "R", "G", "B", "W", "K", or "Au"
     */
    public String label() {
        return label;
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