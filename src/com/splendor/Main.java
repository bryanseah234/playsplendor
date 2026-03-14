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
import com.splendor.network.ClientHandler;
import com.splendor.network.ServerSocketHandler;
import com.splendor.util.Constants;
import com.splendor.view.ConsoleView;
import com.splendor.view.IGameView;
import com.splendor.view.MultiRemoteView;
import com.splendor.model.Player;
import java.util.List;
import java.util.stream.Collectors;

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
     * 
     * @param configProvider Configuration provider
     * @throws SplendorException if server initialization fails
     */
    private static void startServerMode(final IConfigProvider configProvider) throws SplendorException {
        System.out.println("Starting Splendor in server mode...");
        
        final int serverPort = configProvider.getIntProperty("server.port", Constants.DEFAULT_SERVER_PORT);
        final ServerSocketHandler serverHandler = new ServerSocketHandler(serverPort, configProvider);
        
        // Start server in background thread so we can continue with game orchestration
        new Thread(() -> {
            try {
                serverHandler.startServer();
            } catch (SplendorException e) {
                System.err.println("Server failure: " + e.getMessage());
            }
        }).start();

        // Wait for players to connect
        final int targetPlayers = configProvider.getIntProperty("game.players", 2);
        System.out.println("Waiting for " + targetPlayers + " players to connect...");
        
        try {
            while (serverHandler.getConnectedClientCount() < targetPlayers) {
                Thread.sleep(1000);
                if (serverHandler.getConnectedClientCount() > 0) {
                    System.out.print("\rConnected: " + serverHandler.getConnectedClientCount() + "/" + targetPlayers);
                }
            }
            System.out.println("\nAll players connected. Initializing game...");
            
            // Get connected client IDs
            List<String> clientIds = serverHandler.getConnectedClients().stream()
                    .map(ClientHandler::getClientId)
                    .collect(Collectors.toList());
            
            // Create MultiRemoteView
            final MultiRemoteView multiView = new MultiRemoteView(serverHandler, clientIds);
            
            // Initialize GameController
            final GameController gameController = new GameController(multiView, configProvider);
            gameController.initializeGame();
            
            // Map the created Player objects to their network clients
            List<Player> players = gameController.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                multiView.mapPlayerToClient(players.get(i), i);
            }
            
            // Start the game loop
            gameController.startGame();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SplendorException("Connection wait interrupted", e);
        } finally {
            serverHandler.stopServer();
        }
    }
}