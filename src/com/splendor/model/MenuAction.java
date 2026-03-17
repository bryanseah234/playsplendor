package com.splendor.model;

/**
 * Represents the distinct actions a player can take from the main menu.
 * Used internally to map menu choices to actual game commands.
 */
public enum MenuAction {
    TAKE_THREE,
    TAKE_TWO,
    RESERVE_VISIBLE,
    RESERVE_DECK,
    BUY_VISIBLE,
    BUY_RESERVED,
    EXIT_GAME
}
