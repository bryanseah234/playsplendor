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
import com.splendor.view.NetworkMessageHandler;
import com.splendor.view.RemoteView;
import java.util.ArrayList;
import java.util.List;

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
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
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
        
        final IGameView consoleView = new ConsoleView();
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
                return serverHandler.pollClientResponse(id, timeoutMs);
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
        if (!serverHandler.waitForClients(1, 0)) {
            System.err.println("Interrupted while waiting for host.");
            return;
        }

        // Step 2: ask host how many players
        final String hostId = serverHandler.getConnectedClientIds().get(0);
        final RemoteView hostView = new RemoteView(hostId, messageHandler);
        final int playerCount = hostView.promptForPlayerCount();
        System.out.println("Host selected " + playerCount + " players. Waiting for remaining clients...");
        serverHandler.broadcastToAllClients("Lobby: 1/" + playerCount + " players joined. Waiting for " + (playerCount - 1) + " more...");

        // Step 3: wait for the remaining clients to connect one at a time, updating all clients after each join
        for (int joined = 1; joined < playerCount; joined++) {
            if (!serverHandler.waitForClients(1, 0)) {
                System.err.println("Interrupted while waiting for players.");
                return;
            }
            final int nowJoined = joined + 1;
            final String status = nowJoined == playerCount
                ? "Lobby: " + nowJoined + "/" + playerCount + " players joined. Starting game..."
                : "Lobby: " + nowJoined + "/" + playerCount + " players joined. Waiting for " + (playerCount - nowJoined) + " more...";
            serverHandler.broadcastToAllClients(status);
            System.out.println(status);
        }

        // Step 4: build one RemoteView per connected client (in connection order)
        final List<RemoteView> playerViews = new ArrayList<>();
        for (final String clientId : serverHandler.getConnectedClientIds()) {
            playerViews.add(new RemoteView(clientId, messageHandler));
        }

        System.out.println("All " + playerCount + " players connected. Starting game...");

        // Step 5: start the game with a view that routes to each player's client
        final IGameView gameView = new NetworkGameView(playerViews, playerCount);
        final GameController gameController = new GameController(gameView, configProvider);

        gameController.initializeGame();
        serverHandler.markGameStarted();
        gameController.startGame();
    }
}