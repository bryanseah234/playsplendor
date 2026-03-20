package com.splendor.view.tui;

import com.splendor.model.Game;
import com.splendor.model.MenuOption;
import com.splendor.model.Noble;
import com.splendor.model.Player;

import java.util.List;

/**
 * Immutable request object posted from TuiGameView (sync side) to
 * TuiAppModel (async TUI4J side) via a blocking queue.
 *
 * Each request describes what the view layer needs from the user.
 */
public record TuiRequest(
        RequestType type,
        String payload,
        Player player,
        Game game,
        List<MenuOption> menuOptions,
        List<Noble> nobles,
        int intValue
) {
    /** Request types corresponding to IGameView methods. */
    public enum RequestType {
        DISPLAY_WELCOME,
        PROMPT_PLAYER_COUNT,
        PROMPT_PLAYER_NAME,
        DISPLAY_GAME_STATE,
        DISPLAY_PLAYER_TURN,
        DISPLAY_AVAILABLE_MOVES,
        PROMPT_MOVE,
        PROMPT_SUB_INPUT,
        PROMPT_TOKEN_DISCARD,
        PROMPT_NOBLE_CHOICE,
        DISPLAY_MESSAGE,
        DISPLAY_ERROR,
        DISPLAY_NOTIFICATION,
        DISPLAY_WINNER,
        WAIT_FOR_ENTER,
        CLOSE
    }

    // ── Convenience factory methods ──────────────────────────────────

    public static TuiRequest welcome() {
        return new TuiRequest(RequestType.DISPLAY_WELCOME, null, null, null, null, null, 0);
    }

    public static TuiRequest playerCount() {
        return new TuiRequest(RequestType.PROMPT_PLAYER_COUNT, null, null, null, null, null, 0);
    }

    public static TuiRequest playerName(final String promptText) {
        return new TuiRequest(RequestType.PROMPT_PLAYER_NAME, promptText, null, null, null, null, 0);
    }

    public static TuiRequest gameState(final Game game) {
        return new TuiRequest(RequestType.DISPLAY_GAME_STATE, null, null, game, null, null, 0);
    }

    public static TuiRequest playerTurn(final Player player) {
        return new TuiRequest(RequestType.DISPLAY_PLAYER_TURN, null, player, null, null, null, 0);
    }

    public static TuiRequest availableMoves(final List<MenuOption> options, final Game game) {
        return new TuiRequest(RequestType.DISPLAY_AVAILABLE_MOVES, null, null, game, options, null, 0);
    }

    public static TuiRequest promptMove(final Player player, final Game game, final List<MenuOption> options) {
        return new TuiRequest(RequestType.PROMPT_MOVE, null, player, game, options, null, 0);
    }

    public static TuiRequest subInput(final String promptText) {
        return new TuiRequest(RequestType.PROMPT_SUB_INPUT, promptText, null, null, null, null, 0);
    }

    public static TuiRequest tokenDiscard(final Player player, final int excessCount) {
        return new TuiRequest(RequestType.PROMPT_TOKEN_DISCARD, null, player, null, null, null, excessCount);
    }

    public static TuiRequest nobleChoice(final Player player, final List<Noble> nobles) {
        return new TuiRequest(RequestType.PROMPT_NOBLE_CHOICE, null, player, null, null, nobles, 0);
    }

    public static TuiRequest message(final String text) {
        return new TuiRequest(RequestType.DISPLAY_MESSAGE, text, null, null, null, null, 0);
    }

    public static TuiRequest error(final String text) {
        return new TuiRequest(RequestType.DISPLAY_ERROR, text, null, null, null, null, 0);
    }

    public static TuiRequest notification(final String text) {
        return new TuiRequest(RequestType.DISPLAY_NOTIFICATION, text, null, null, null, null, 0);
    }

    public static TuiRequest winner(final String formattedText) {
        return new TuiRequest(RequestType.DISPLAY_WINNER, formattedText, null, null, null, null, 0);
    }

    public static TuiRequest waitForEnter(final String text) {
        return new TuiRequest(RequestType.WAIT_FOR_ENTER, text, null, null, null, null, 0);
    }

    public static TuiRequest close() {
        return new TuiRequest(RequestType.CLOSE, null, null, null, null, null, 0);
    }
}
