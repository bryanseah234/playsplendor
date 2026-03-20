package com.splendor.view.tui;

/**
 * Represents the current screen/phase of the TUI application.
 * The TuiAppModel uses this to determine what to render and
 * how to handle input at any given moment.
 */
public enum ScreenState {
    /** Welcome splash before game setup. */
    WELCOME,
    /** Prompting for number of players. */
    SETUP_PLAYER_COUNT,
    /** Prompting for a player's name. */
    SETUP_PLAYER_NAME,
    /** Main gameplay — board visible, menu active. */
    PLAYING,
    /** Sub-prompt during PLAYING: selecting gems for take-3. */
    PROMPT_TAKE_THREE,
    /** Sub-prompt during PLAYING: selecting gem for take-2. */
    PROMPT_TAKE_TWO,
    /** Sub-prompt during PLAYING: entering card ID (buy/reserve). */
    PROMPT_CARD_ID,
    /** Sub-prompt during PLAYING: selecting deck tier. */
    PROMPT_DECK_TIER,
    /** Player must discard excess tokens. */
    DISCARD_TOKENS,
    /** Player must choose a noble. */
    NOBLE_CHOICE,
    /** Displaying a message and waiting for Enter. */
    MESSAGE,
    /** Displaying an error and waiting for Enter. */
    ERROR,
    /** Displaying a non-blocking notification. */
    NOTIFICATION,
    /** Game over — showing winner and final scores. */
    GAME_OVER
}
