/**
 * Main entry point for the Splendor game application.
 * Supports both console and server modes based on command line arguments.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented CLI argument parsing and server mode initialization
 */
package com.splendor;

import com.splendor.config.FileConfigProvider;
import com.splendor.config.IConfigProvider;
import com.splendor.controller.GameController;
import com.splendor.exception.SplendorException;
import com.splendor.network.ServerSocketHandler;
import com.splendor.util.Constants;
import com.splendor.view.ConsoleView;
import com.splendor.view.IGameView;
import com.splendor.view.NetworkGameView;
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
            
            if (isServerMode(args)) {
                startServerMode(configProvider);
            } else {
                startConsoleMode(configProvider);
            }
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
    
    /**
     * Starts the application in server mode.
     * Waits for the required number of clients, then launches the game.
     *
     * @param configProvider Configuration provider
     * @throws SplendorException if server initialization fails
     */
    private static void startServerMode(final IConfigProvider configProvider) throws SplendorException {
        System.out.println("Starting Splendor in server mode...");

        final int serverPort = configProvider.getIntProperty("server.port", Constants.DEFAULT_SERVER_PORT);
        final ServerSocketHandler serverHandler = new ServerSocketHandler(serverPort, configProvider);

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

        final RemoteView.NetworkMessageHandler messageHandler = new RemoteView.NetworkMessageHandler() {
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
        System.out.println("Waiting for host to connect on port " + serverPort + "...");
        if (!serverHandler.waitForClients(1, 0)) {
            System.err.println("Interrupted while waiting for host.");
            return;
        }

        // Step 2: ask host how many players
        final String hostId = serverHandler.getConnectedClientIds().get(0);
        final RemoteView hostView = new RemoteView(hostId, messageHandler);
        final int playerCount = hostView.promptForPlayerCount();
        System.out.println("Host selected " + playerCount + " players. Waiting for remaining clients...");

        // Step 3: wait for the remaining clients to connect
        if (playerCount > 1 && !serverHandler.waitForClients(playerCount - 1, 0)) {
            System.err.println("Interrupted while waiting for players.");
            return;
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
        gameController.startGame();
    }
}