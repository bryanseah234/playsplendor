/**
 * Multi-client view for network multiplayer.
 * Routes prompts to the correct client's RemoteView based on whose turn it is,
 * and broadcasts display calls to all connected clients.
 *
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.view;

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
    private List<Player> playerOrder;

    /**
     * Creates a NetworkGameView with one RemoteView per connected client.
     *
     * @param playerViews List of RemoteViews in player-order (index 0 = player 1)
     * @param playerCount Total number of players (already determined before game init)
     */
    public NetworkGameView(final List<RemoteView> playerViews, final int playerCount) {
        this.playerViews = playerViews;
        this.playerCount = playerCount;
    }

    // -------------------------------------------------------------------------
    // Routing helpers
    // -------------------------------------------------------------------------

    private void initPlayerOrder(final Game game) {
        if (playerOrder == null) {
            playerOrder = new java.util.ArrayList<>(game.getPlayers());
        }
    }

    private RemoteView viewForPlayer(final Player player) {
        if (playerOrder != null) {
            final int idx = playerOrder.indexOf(player);
            if (idx >= 0 && idx < playerViews.size()) {
                return playerViews.get(idx);
            }
        }
        return playerViews.get(0);
    }

    private void broadcast(final Consumer<RemoteView> action) {
        for (final RemoteView view : playerViews) {
            action.accept(view);
        }
    }

    // -------------------------------------------------------------------------
    // Broadcast display methods — sent to every client
    // -------------------------------------------------------------------------

    @Override
    public void displayGameState(final Game game) {
        initPlayerOrder(game);
        broadcast(v -> v.displayGameState(game));
    }

    @Override
    public void displayPlayerTurn(final Player player) {
        broadcast(v -> v.displayPlayerTurn(player));
    }

    @Override
    public String displayMessage(final String message) {
        broadcast(v -> v.displayMessage(message));
        return "";
    }

    @Override
    public void displayNotification(final String message) {
        broadcast(v -> v.displayNotification(message));
    }

    @Override
    public String displayError(final String errorMessage) {
        broadcast(v -> v.displayError(errorMessage));
        return "";
    }

    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        broadcast(v -> v.displayWinner(winner, finalScores));
    }

    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        // Handled client-side
    }

    @Override
    public void displayWelcomeMessage() {
        broadcast(RemoteView::displayWelcomeMessage);
    }

    @Override
    public void clearDisplay() {
        broadcast(RemoteView::clearDisplay);
    }

    @Override
    public void close() {
        broadcast(RemoteView::close);
    }

    @Override
    public String waitForEnter() {
        return "";
    }

    // -------------------------------------------------------------------------
    // Routed prompt methods — go to the active player's client only
    // -------------------------------------------------------------------------

    @Override
    public String promptForCommand(final Player player, final Game game) {
        initPlayerOrder(game);
        return viewForPlayer(player).promptForCommand(player, game);
    }

    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        initPlayerOrder(game);
        return viewForPlayer(player).promptForMove(player, game, options);
    }

    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        return viewForPlayer(player).promptForTokenDiscard(player, excessCount);
    }

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
        final int idx = Math.min(playerNumber - 1, playerViews.size() - 1);
        return playerViews.get(idx).promptForPlayerName(playerNumber, totalPlayers);
    }
}
