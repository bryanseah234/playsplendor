/**
 * Main entry point for the Splendor game application.
 * Supports both console and server modes based on command line arguments.
 * 
 */
package com.splendor;

import com.splendor.config.ConfigValidator;
import com.splendor.config.FileConfigProvider;
import com.splendor.config.IConfigProvider;
import com.splendor.controller.GameController;
import com.splendor.exception.ConfigException;
import com.splendor.exception.SplendorException;
import com.splendor.network.ServerSocketHandler;
import com.splendor.util.Constants;
import com.splendor.view.ConsoleView;
import com.splendor.view.IGameView;
import com.splendor.view.NetworkGameView;
import com.splendor.network.NetworkMessageHandler;
import com.splendor.view.RemoteView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application entry point that handles mode selection and initialization.
 * Supports console mode (default) and server mode (--server flag).
 */
public class Main {
    
    /**
     * Application entry point.
     * 
     * @param args Command line arguments. Use "--server" to start in server mode.
     */
    public static void main(String[] args) {
        try {
            final IConfigProvider configProvider = new FileConfigProvider();
            configProvider.loadConfiguration();
            
            // Fail-fast validation: halt immediately if config or data is invalid
            System.out.println("Validating game configuration...");
            ConfigValidator.validateAll(configProvider);
            System.out.println("Configuration validated successfully.");
            
            if (isServerMode(args)) {
                startServerMode(configProvider);
            } else {
                startConsoleMode(configProvider);
            }
        } catch (ConfigException e) {
            System.err.println("CRITICAL: Configuration validation failed!");
            System.err.println("  " + e.getMessage());
            System.err.println("\nPlease fix the configuration and try again.");
            System.exit(1);
        } catch (SplendorException e) {
            System.err.println("Failed to start application: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected startup failure: " + safeMessage(e));
            System.err.println("Please restart the server or check your network and configuration.");
            System.exit(1);
        }
    }
    
    /**
     * Checks if server mode is requested.
     * 
     * @param args Command line arguments
     * @return true if "--server" flag is present
     */
    private static boolean isServerMode(final String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }
        
        for (final String arg : args) {
            if (Constants.SERVER_MODE_FLAG.equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Starts the application in console mode.
     * 
     * @param configProvider Configuration provider
     * @throws SplendorException if initialization fails
     */
    private static void startConsoleMode(final IConfigProvider configProvider) throws SplendorException {
        System.out.println("Starting Splendor in console mode...");
        
        final IGameView consoleView = new ConsoleView(configProvider);
        final GameController gameController = new GameController(consoleView, configProvider);
        
        gameController.initializeGame();
        gameController.startGame();
    }
    
    /**th
     * Starts the application in server mode.
     * Waits for the required number of clients, then launches the game.
     *
     * @param configProvider Configuration provider
     * @throws SplendorException if server initialization fails
     */
    private static void startServerMode(final IConfigProvider configProvider) throws SplendorException {
        System.out.println("Starting Splendor in server mode...");

        final ServerSocketHandler serverHandler = new ServerSocketHandler(0, configProvider);

        // Accept connections in a background thread
        final Thread acceptThread = new Thread(() -> {
            try {
                serverHandler.startServer();
            } catch (final SplendorException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();

        final NetworkMessageHandler messageHandler = new NetworkMessageHandler() {
            @Override
            public void sendToClient(final String id, final String message) {
                serverHandler.sendToClient(id, message);
            }

            @Override
            public String waitForClientResponse(final String id, final int timeoutMs) {
                return serverHandler.waitForClientResponse(id, timeoutMs);
            }
        };

        // Step 1: wait for the host to connect
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        final int actualPort = serverHandler.getActualPort();
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   SPLENDOR SERVER STARTED                ║");
        System.out.println(String.format("║   Port: %-33d║", actualPort));
        System.out.println(String.format("║   Players connect with: nc <ip> %-8d ║", actualPort));
        System.out.println("╚══════════════════════════════════════════╝\n");

        System.out.println("Waiting for host to connect...");
        int playerCount;
        while (true) {
            if (!serverHandler.waitForAtLeastClients(1, 0)) {
                System.err.println("Interrupted while waiting for host.");
                return;
            }

            final List<String> connectedIds = serverHandler.getConnectedClientIds();
            if (connectedIds.isEmpty()) {
                continue;
            }

            // Current first connection becomes lobby leader for this cycle.
            final String hostId = connectedIds.get(0);
            final RemoteView hostView = new RemoteView(hostId, messageHandler, configProvider);
            hostView.displayWelcomeMessage();
            hostView.displayMessage("You are the lobby leader.");
            hostView.displayMessage("Please choose total players (2-4). Other players will wait for your choice.");
            playerCount = hostView.promptForPlayerCount();

            // Host may disconnect while choosing the player count; if so, restart lobby election.
            if (!serverHandler.isClientConnected(hostId)) {
                if (serverHandler.getConnectedClientCount() == 0) {
                    System.out.println("Lobby reset: all clients disconnected. Waiting for a new leader...");
                } else {
                    System.out.println("Lobby leader disconnected before setup completed. Electing next leader...");
                }
                continue;
            }

            System.out.println("Host selected " + playerCount + " players. Waiting for remaining clients...");
            serverHandler.broadcastToAllClients(
                    "Lobby: 1/" + playerCount + " players joined. Waiting for " + (playerCount - 1) + " more...");

            // Step 3: wait until total connected clients reaches the selected player count.
            // If lobby empties before game start, fully reset and assign a new leader.
            int lastAnnouncedCount = Math.max(1, serverHandler.getConnectedClientCount());
            boolean restartLobby = false;
            while (lastAnnouncedCount < playerCount) {
                if (serverHandler.getConnectedClientCount() == 0) {
                    System.out.println("Lobby reset: all clients disconnected. Waiting for a new leader...");
                    restartLobby = true;
                    break;
                }
                if (!serverHandler.waitForAtLeastClients(lastAnnouncedCount + 1, 0)) {
                    System.err.println("Interrupted while waiting for players.");
                    return;
                }
                if (serverHandler.getConnectedClientCount() == 0) {
                    System.out.println("Lobby reset: all clients disconnected. Waiting for a new leader...");
                    restartLobby = true;
                    break;
                }
                final int nowJoined = Math.min(serverHandler.getConnectedClientCount(), playerCount);
                final String status = nowJoined == playerCount
                        ? "Lobby: " + nowJoined + "/" + playerCount + " players joined. Starting game..."
                        : "Lobby: " + nowJoined + "/" + playerCount + " players joined. Waiting for "
                                + (playerCount - nowJoined) + " more...";
                serverHandler.broadcastToAllClients(status);
                System.out.println(status);
                lastAnnouncedCount = nowJoined;
            }

            if (restartLobby) {
                continue;
            }
            break;
        }

        // Step 4: build one RemoteView per connected client (in connection order)
        final List<RemoteView> playerViews = new ArrayList<>();
        for (final String clientId : serverHandler.getConnectedClientIds()) {
            playerViews.add(new RemoteView(clientId, messageHandler, configProvider));
        }
        for (int i = 1; i < playerViews.size(); i++) {
            playerViews.get(i).displayWelcomeMessage();
        }

        final List<String> confirmedNames = synchronizePlayerNames(serverHandler, playerViews);
        System.out.println("All " + playerCount + " players connected. Starting game...");

        // Step 5: start the game with a view that routes to each player's client
        final IGameView gameView = new NetworkGameView(playerViews, playerCount, confirmedNames);
        final GameController gameController = new GameController(gameView, configProvider);

        gameController.initializeGame();
        serverHandler.setGameStarted(true);
        gameController.startGame();
    }

    private static List<String> synchronizePlayerNames(final ServerSocketHandler serverHandler,
            final List<RemoteView> playerViews) {
        final int playerCount = playerViews.size();
        final Map<String, String> namesByClient = new LinkedHashMap<>();
        final Map<String, Boolean> readyByClient = new LinkedHashMap<>();
        final List<String> clientIds = serverHandler.getConnectedClientIds();

        for (int i = 0; i < clientIds.size(); i++) {
            final String clientId = clientIds.get(i);
            namesByClient.put(clientId, "Player" + (i + 1));
            readyByClient.put(clientId, false);
        }

        serverHandler.broadcastToAllClients("All players connected.");
        serverHandler.broadcastToAllClients("Name setup: type your preferred name and press Enter.");
        serverHandler.broadcastToAllClients(
                "If you do not respond within 30 seconds, a default name will be assigned.");

        final long nameDeadline = System.currentTimeMillis() + 30000L;
        broadcastWaitingStatus(serverHandler, clientIds, namesByClient, readyByClient, playerCount);
        while (System.currentTimeMillis() < nameDeadline) {
            for (int i = 0; i < clientIds.size(); i++) {
                final String clientId = clientIds.get(i);
                if (Boolean.TRUE.equals(readyByClient.get(clientId))) {
                    continue;
                }
                final String input = serverHandler.waitForClientResponse(clientId, 200);
                if (input == null) {
                    continue;
                }
                final String trimmed = input.trim();
                if (!trimmed.isEmpty()) {
                    namesByClient.put(clientId, trimmed);
                }
                readyByClient.put(clientId, true);
                serverHandler.sendToClient(clientId, "Name received. Waiting for the other players...");
            }
            if (allReady(readyByClient, clientIds)) {
                break;
            }
        }

        for (int i = 0; i < clientIds.size(); i++) {
            final String clientId = clientIds.get(i);
            if (!Boolean.TRUE.equals(readyByClient.get(clientId))) {
                readyByClient.put(clientId, true);
                namesByClient.put(clientId, "Player" + (i + 1));
            }
        }
        broadcastWaitingStatus(serverHandler, clientIds, namesByClient, readyByClient, playerCount);

        serverHandler.broadcastToAllClients("Press Enter to confirm readiness and continue.");
        final long readyDeadline = System.currentTimeMillis() + 30000L;
        final Map<String, Boolean> continueReady = new LinkedHashMap<>();
        for (final String clientId : clientIds) {
            continueReady.put(clientId, false);
        }

        while (System.currentTimeMillis() < readyDeadline && !allReady(continueReady, clientIds)) {
            for (final String clientId : clientIds) {
                if (Boolean.TRUE.equals(continueReady.get(clientId))) {
                    continue;
                }
                final String response = serverHandler.waitForClientResponse(clientId, 200);
                if (response != null) {
                    continueReady.put(clientId, true);
                }
            }
        }
        for (final String clientId : clientIds) {
            if (!Boolean.TRUE.equals(continueReady.get(clientId))) {
                continueReady.put(clientId, true);
            }
        }
        serverHandler.broadcastToAllClients("All players are ready. Initializing game...");

        final List<String> orderedNames = new ArrayList<>();
        for (final String clientId : clientIds) {
            orderedNames.add(namesByClient.get(clientId));
        }
        return orderedNames;
    }

    private static void broadcastWaitingStatus(final ServerSocketHandler serverHandler,
            final List<String> clientIds,
            final Map<String, String> namesByClient,
            final Map<String, Boolean> readyByClient,
            final int totalPlayers) {
        final StringBuilder status = new StringBuilder();
        status.append("Waiting room (").append(totalPlayers).append(" players)\n");
        for (int i = 0; i < clientIds.size(); i++) {
            final String clientId = clientIds.get(i);
            final String name = namesByClient.get(clientId);
            final boolean ready = Boolean.TRUE.equals(readyByClient.get(clientId));
            status.append(" - Slot ").append(i + 1).append(": ")
                    .append(name)
                    .append(ready ? " [READY]" : " [AWAITING NAME]")
                    .append('\n');
        }
        serverHandler.broadcastToAllClients(status.toString());
    }

    private static boolean allReady(final Map<String, Boolean> readyMap, final List<String> ids) {
        for (final String id : ids) {
            if (!Boolean.TRUE.equals(readyMap.get(id))) {
                return false;
            }
        }
        return true;
    }

    private static String safeMessage(final Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().trim().isEmpty()) {
            return "No additional error details were provided.";
        }
        return throwable.getMessage().trim();
    }
}
