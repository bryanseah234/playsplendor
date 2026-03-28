package com.splendor.model;

/**
 * Enumerates the distinct actions a player can select from the main action menu.
 *
 * MenuBuilder creates a MenuOption for each value below every turn. The view
 * dispatches on this enum to decide which follow-up prompt to show, and the
 * controller maps the resulting input to a Move object.
 */
public enum MenuAction {

    /** Take one token each of three different (non-gold) gem colors. */
    TAKE_THREE,

    /** Take two tokens of the same (non-gold) gem color; requires >= 4 of that color in the bank. */
    TAKE_TWO,

    /** Reserve a face-up card from the board and receive a gold token if available. */
    RESERVE_VISIBLE,

    /** Reserve a blind card drawn from the top of a tier deck and receive a gold token if available. */
    RESERVE_DECK,

    /** Purchase a face-up card from the board using tokens and/or card bonuses. */
    BUY_VISIBLE,

    /** Purchase a previously reserved card from the player's hand. */
    BUY_RESERVED,

    /** Terminate the application cleanly; only offered to human players. */
    EXIT_GAME
}
