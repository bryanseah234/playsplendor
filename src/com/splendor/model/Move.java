/**
 * Represents a player move in the game.
 * Encapsulates all information about a player's action during their turn.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.model;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a move made by a player during their turn.
 * Contains the move type and any associated data such as selected gems or cards.
 */
public class Move {
    
    private final MoveType moveType;
    private final Map<Gem, Integer> selectedGems;
    private final Integer cardId;
    private final boolean isReservedCard;
    
    /**
     * Creates a new move with just the type (for simple moves).
     * 
     * @param moveType Type of move
     */
    public Move(final MoveType moveType) {
        this(moveType, new HashMap<>(), null, false);
    }
    
    /**
     * Creates a new move with gem selection.
     * 
     * @param moveType Type of move
     * @param selectedGems Map of selected gems and quantities
     */
    public Move(final MoveType moveType, final Map<Gem, Integer> selectedGems) {
        this(moveType, selectedGems, null, false);
    }
    
    /**
     * Creates a new move with card selection.
     * 
     * @param moveType Type of move
     * @param cardId ID of the selected card
     * @param isReservedCard Whether the card is from reserved cards
     */
    public Move(final MoveType moveType, final Integer cardId, final boolean isReservedCard) {
        this(moveType, new HashMap<>(), cardId, isReservedCard);
    }
    
    /**
     * Creates a new move with all parameters.
     * 
     * @param moveType Type of move
     * @param selectedGems Map of selected gems and quantities
     * @param cardId ID of the selected card
     * @param isReservedCard Whether the card is from reserved cards
     */
    public Move(final MoveType moveType, final Map<Gem, Integer> selectedGems, 
                final Integer cardId, final boolean isReservedCard) {
        this.moveType = moveType;
        this.selectedGems = new HashMap<>(selectedGems);
        this.cardId = cardId;
        this.isReservedCard = isReservedCard;
    }
    
    /**
     * Gets the type of this move.
     * 
     * @return Move type
     */
    public MoveType getMoveType() {
        return moveType;
    }
    
    /**
     * Gets the selected gems for this move.
     * 
     * @return Unmodifiable map of selected gems and quantities
     */
    public Map<Gem, Integer> getSelectedGems() {
        return Collections.unmodifiableMap(selectedGems);
    }
    
    /**
     * Gets the ID of the selected card.
     * 
     * @return Card ID or null if no card selected
     */
    public Integer getCardId() {
        return cardId;
    }
    
    /**
     * Checks if the selected card is from reserved cards.
     * 
     * @return true if card is reserved, false otherwise
     */
    public boolean isReservedCard() {
        return isReservedCard;
    }
    
    /**
     * Checks if this move involves card selection.
     * 
     * @return true if a card is selected, false otherwise
     */
    public boolean hasCardSelection() {
        return cardId != null;
    }
    
    /**
     * Checks if this move involves gem selection.
     * 
     * @return true if gems are selected, false otherwise
     */
    public boolean hasGemSelection() {
        return !selectedGems.isEmpty();
    }
    
    /**
     * Returns a string representation of the move.
     * 
     * @return String describing the move
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(moveType.getDisplayName());
        
        if (hasGemSelection()) {
            sb.append(" Gems: ").append(selectedGems);
        }
        
        if (hasCardSelection()) {
            sb.append(" Card: ").append(cardId);
            if (isReservedCard) {
                sb.append(" (Reserved)");
            }
        }
        
        return sb.toString();
    }
}