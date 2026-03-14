package com.splendor.view;

import com.splendor.model.*;
import com.splendor.network.ServerSocketHandler;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * A dispatcher view that manages multiple remote clients.
 * It broadcasts board state/notifications to all clients and prompts specific clients for input.
 */
public class MultiRemoteView implements IGameView {

    private final ServerSocketHandler serverHandler;
    private final List<String> clientIds;
    private final Map<String, RemoteView> remoteViews;
    private final Map<Player, String> playerToClientMap;

    public MultiRemoteView(ServerSocketHandler serverHandler, List<String> clientIds) {
        this.serverHandler = serverHandler;
        this.clientIds = clientIds;
        this.remoteViews = new HashMap<>();
        this.playerToClientMap = new HashMap<>();
        
        for (String id : clientIds) {
            remoteViews.put(id, new RemoteView(id, serverHandler));
        }
    }

    private RemoteView getRemoteViewForPlayer(Player player) {
        String clientId = playerToClientMap.get(player);
        if (clientId == null) {
            // If mapping doesn't exist yet (e.g. during initialization), 
            // we might be prompting for player names in sequence.
            // This is a simplified fallback.
            int playerIndex = -1;
            // Try to guess based on player search (very rough)
            // In practice, we'll map them during name prompts.
            return remoteViews.get(clientIds.get(0)); 
        }
        return remoteViews.get(clientId);
    }

    @Override
    public void displayGameState(Game game) {
        // Broadcast to all
        for (RemoteView rv : remoteViews.values()) {
            rv.displayGameState(game);
        }
    }

    @Override
    public void displayPlayerTurn(Player player) {
        // Broadcast to all
        for (RemoteView rv : remoteViews.values()) {
            rv.displayPlayerTurn(player);
        }
    }

    @Override
    public String displayMessage(String message) {
        // Broadcast message to all
        for (RemoteView rv : remoteViews.values()) {
            rv.displayMessage(message);
        }
        return "";
    }

    @Override
    public String displayError(String errorMessage) {
        // Errors are usually specific to the player who made the move
        // But for generic errors, we could broadcast.
        // For simplicity in the dispatcher, we broadcast.
        for (RemoteView rv : remoteViews.values()) {
            rv.displayError(errorMessage);
        }
        return "";
    }

    @Override
    public void displayNotification(String message) {
        for (RemoteView rv : remoteViews.values()) {
            rv.displayNotification(message);
        }
    }

    @Override
    public String promptForCommand(Player player, Game game) {
        return getRemoteViewForPlayer(player).promptForCommand(player, game);
    }

    @Override
    public Move promptForMove(Player player, Game game, List<MenuOption> options) {
        return getRemoteViewForPlayer(player).promptForMove(player, game, options);
    }

    @Override
    public Move promptForTokenDiscard(Player player, int excessCount) {
        return getRemoteViewForPlayer(player).promptForTokenDiscard(player, excessCount);
    }

    @Override
    public void displayWinner(Player winner, Map<String, Integer> finalScores) {
        for (RemoteView rv : remoteViews.values()) {
            rv.displayWinner(winner, finalScores);
        }
    }

    @Override
    public void clearDisplay() {
        for (RemoteView rv : remoteViews.values()) {
            rv.clearDisplay();
        }
    }

    @Override
    public void displayAvailableMoves(List<MenuOption> options, Game game) {
        // RemoteView.displayAvailableMoves is currently empty/handled by client side
    }

    @Override
    public Noble promptForNobleChoice(Player player, List<Noble> nobles) {
        return getRemoteViewForPlayer(player).promptForNobleChoice(player, nobles);
    }

    @Override
    public String promptForPlayerName(int playerNumber, int totalPlayers) {
        // Sequentially prompt each client
        String clientId = clientIds.get(playerNumber - 1);
        String name = remoteViews.get(clientId).promptForPlayerName(playerNumber, totalPlayers);
        
        // We can't easily map the Player object yet because it hasn't been created
        // The controller usually creates players after prompting for all names.
        // We'll need a way for the Controller or Main to register the mapping.
        // For now, we'll rely on the playerToClientMap being populated later.
        return name;
    }
    
    /**
     * Maps a Player object to a specific network client ID.
     */
    public void mapPlayerToClient(Player player, int index) {
        playerToClientMap.put(player, clientIds.get(index));
    }

    @Override
    public int promptForPlayerCount() {
        // In network mode, player count is fixed by connected clients
        return clientIds.size();
    }

    @Override
    public void displayWelcomeMessage() {
        for (RemoteView rv : remoteViews.values()) {
            rv.displayWelcomeMessage();
        }
    }

    @Override
    public String waitForEnter() {
        return "";
    }

    @Override
    public void close() {
        for (RemoteView rv : remoteViews.values()) {
            rv.close();
        }
    }
}
