// Generated and inline suggested by ChatGPT-4 and Claude Code; modified for clarity

/**
 * Multi-client view for network multiplayer.
 * Routes prompts to the correct client's RemoteView based on whose turn it is,
 * and broadcasts display calls to all connected clients.
 *
 */
package com.splendor.view;

import com.splendor.model.ComputerPlayer;
import com.splendor.model.Game;
import com.splendor.model.MenuOption;
import com.splendor.model.Move;
import com.splendor.model.Noble;
import com.splendor.model.Player;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implements IGameView for a network game with one RemoteView per player.
 * Turn-based prompts are routed to the active player's client.
 * Broadcast calls (display, notifications) are sent to every client.
 */
public class NetworkGameView implements IGameView {

    private final List<RemoteView> playerViews;
    private final int playerCount;
    private final List<String> predefinedPlayerNames;
    private List<Player> playerOrder;
    private Player activePlayer;

    /**
     * Creates a NetworkGameView with one RemoteView per connected client.
     *
     * @param playerViews List of RemoteViews in player-order (index 0 = player 1)
     * @param playerCount Total number of players (already determined before game init)
     */
    public NetworkGameView(final List<RemoteView> playerViews, final int playerCount) {
        this(playerViews, playerCount, null);
    }

    /**
     * Creates a NetworkGameView with one RemoteView per connected client and optional
     * pre-collected names from lobby synchronization.
     *
     * @param playerViews List of RemoteViews in player-order (index 0 = player 1)
     * @param playerCount Total number of players (already determined before game init)
     * @param predefinedPlayerNames Optional ordered names captured during lobby setup.
     */
    public NetworkGameView(final List<RemoteView> playerViews, final int playerCount,
            final List<String> predefinedPlayerNames) {
        this.playerViews = playerViews;
        this.playerCount = playerCount;
        this.predefinedPlayerNames = predefinedPlayerNames;
        this.activePlayer = null;
    }

    // -------------------------------------------------------------------------
    // Routing helpers
    // -------------------------------------------------------------------------

    /**
     * Lazily captures the canonical player list from the game on the first call.
     * Subsequent calls are no-ops. The list is used by viewForPlayer() to map a
     * Player object to its corresponding RemoteView by index.
     *
     * @param game The current game, queried only on the first invocation.
     */
    private void initPlayerOrder(final Game game) {
        if (playerOrder == null) {
            playerOrder = new java.util.ArrayList<>(game.getPlayers());
        }
    }

    /**
     * Returns the RemoteView that corresponds to the given player.
     * Looks up the player's position in playerOrder and returns the RemoteView at
     * the same index. Falls back to playerViews index 0 if the player is not found,
     * which guards against calls before initPlayerOrder has been invoked.
     *
     * @param player The player whose RemoteView is needed.
     * @return The RemoteView for that player, or playerViews.get(0) as a fallback.
     */
    private RemoteView viewForPlayer(final Player player) {
        if (playerOrder != null) {
            final int idx = playerOrder.indexOf(player);
            if (idx >= 0 && idx < playerViews.size()) {
                return playerViews.get(idx);
            }
        }
        return playerViews.get(0);
    }

    /**
     * Applies an action to every RemoteView in playerViews.
     * Used by all display/notification methods to ensure every connected client
     * receives the same output simultaneously.
     *
     * @param action A Consumer that sends a specific message or display update to a RemoteView.
     */
    private void broadcast(final Consumer<RemoteView> action) {
        for (final RemoteView view : playerViews) {
            action.accept(view);
        }
    }

    // -------------------------------------------------------------------------
    // Broadcast display methods — sent to every client
    // -------------------------------------------------------------------------

    /** Broadcasts the current board rendering to every connected client. */
    @Override
    public void displayGameState(final Game game) {
        initPlayerOrder(game);
        broadcast(v -> v.displayGameState(game));
    }

    /** Broadcasts the active-player announcement and caches the active player for routing. */
    @Override
    public void displayPlayerTurn(final Player player) {
        this.activePlayer = player;
        broadcast(v -> v.displayPlayerTurn(player));
    }

    /**
     * Broadcasts the message to all clients, then waits for the active player's
     * acknowledgement (Enter) before returning, so the game does not advance until
     * the current player has read the message.
     */
    @Override
    public String displayMessage(final String message) {
        if (activePlayer == null) {
            broadcast(v -> v.displayNotification(message));
            return "";
        }

        final RemoteView activeView = viewForPlayer(activePlayer);
        for (final RemoteView view : playerViews) {
            if (view != activeView) {
                view.displayNotification(message);
            }
        }
        return activeView.displayMessage(message);
    }

    /** Broadcasts a notification line to every connected client without waiting for input. */
    @Override
    public void displayNotification(final String message) {
        broadcast(v -> v.displayNotification(message));
    }

    /**
     * Broadcasts an error message to all clients prefixed with "ERROR:", then
     * waits for the active player to acknowledge before returning.
     */
    @Override
    public String displayError(final String errorMessage) {
        if (activePlayer == null) {
            broadcast(v -> v.displayErrorNotification(errorMessage));
            return "";
        }

        final RemoteView activeView = viewForPlayer(activePlayer);
        for (final RemoteView view : playerViews) {
            if (view != activeView) {
                view.displayErrorNotification(errorMessage);
            }
        }
        return activeView.displayError(errorMessage);
    }

    /** Broadcasts the end-of-game summary and final scores to all clients. */
    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        broadcast(v -> v.displayWinner(winner, finalScores));
    }

    /**
     * For CPU players, broadcasts the board and menu to every client so they can
     * observe the bot's available options. For human players this is a no-op
     * because their RemoteView already sends the board inside promptForMove,
     * avoiding a double render.
     */
    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        if (activePlayer instanceof ComputerPlayer) {
            initPlayerOrder(game);
            broadcast(v -> v.displayAvailableMoves(options, game));
        }
    }

    /** Broadcasts the welcome banner to every client. */
    @Override
    public void displayWelcomeMessage() {
        broadcast(RemoteView::displayWelcomeMessage);
    }

    /** Sends a terminal-clear escape sequence to every client. */
    @Override
    public void clearDisplay() {
        broadcast(RemoteView::clearDisplay);
    }

    /** Closes all client connections by broadcasting the close signal. */
    @Override
    public void close() {
        broadcast(RemoteView::close);
    }

    /**
     * Waits for the active player's client to press Enter.
     * Returns "" immediately if no active player has been set yet.
     */
    @Override
    public String waitForEnter() {
        if (activePlayer != null) {
            return viewForPlayer(activePlayer).waitForEnter();
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Routed prompt methods — go to the active player's client only
    // -------------------------------------------------------------------------

    /** Routes the command prompt to the specified player's remote client. */
    @Override
    public String promptForCommand(final Player player, final Game game) {
        initPlayerOrder(game);
        return viewForPlayer(player).promptForCommand(player, game);
    }

    /** Routes the full move-selection flow to the specified player's remote client. */
    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        initPlayerOrder(game);
        return viewForPlayer(player).promptForMove(player, game, options);
    }

    /** Routes the token-discard prompt to the specified player's remote client. */
    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        return viewForPlayer(player).promptForTokenDiscard(player, excessCount);
    }

    /** Routes the noble-choice prompt to the specified player's remote client. */
    @Override
    public Noble promptForNobleChoice(final Player player, final List<Noble> nobles) {
        return viewForPlayer(player).promptForNobleChoice(player, nobles);
    }

    // -------------------------------------------------------------------------
    // Setup prompts
    // -------------------------------------------------------------------------

    /**
     * Returns the pre-determined player count so initializeGame() does not
     * prompt the host a second time.
     */
    @Override
    public int promptForPlayerCount() {
        return playerCount;
    }

    /**
     * Routes each name prompt to the corresponding client.
     * Player 1 → client 0, Player 2 → client 1, etc.
     * If fewer clients than players, the last client answers for remaining players.
     */
    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        if (predefinedPlayerNames != null && playerNumber >= 1 && playerNumber <= predefinedPlayerNames.size()) {
            return predefinedPlayerNames.get(playerNumber - 1);
        }
        final int idx = Math.min(playerNumber - 1, playerViews.size() - 1);
        return playerViews.get(idx).promptForPlayerName(playerNumber, totalPlayers);
    }
}
