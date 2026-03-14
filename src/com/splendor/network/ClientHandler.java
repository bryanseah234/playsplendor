/**
 * Handles individual client connections in the network layer.
 * Manages communication with a single remote client.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.network;

import com.splendor.config.IConfigProvider;
import com.splendor.util.Constants;
import com.splendor.util.GameLogger;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles communication with a single network client.
 * Manages message sending/receiving and connection lifecycle.
 */
public class ClientHandler {
    
    private final String clientId;
    private final Socket clientSocket;
    private final ServerSocketHandler serverHandler;
    private final IConfigProvider configProvider;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private volatile boolean isConnected;
    
    /**
     * Creates a new ClientHandler for the specified socket.
     * 
     * @param clientSocket Client socket
     * @param serverHandler Parent server handler
     * @param configProvider Configuration provider
     */
    public ClientHandler(final Socket clientSocket, final ServerSocketHandler serverHandler,
                        final IConfigProvider configProvider) {
        this.clientId = UUID.randomUUID().toString();
        this.clientSocket = clientSocket;
        this.serverHandler = serverHandler;
        this.configProvider = configProvider;
        this.isConnected = true;
    }
    
    /**
     * Handles client communication.
     * Processes incoming messages and manages connection lifecycle.
     * 
     * @throws NetworkException if client handling fails
     */
    public void handleClient() throws NetworkException {
        try {
            serverHandler.registerClientQueue(clientId);
            initializeStreams();
            GameLogger.info("Client handler initialized for: " + getClientAddress());
            
            // Send welcome message
            sendWelcomeMessage();
            
            // Main message processing loop
            processClientMessages();
            
        } catch (final IOException e) {
            throw new NetworkException("Client communication error: " + e.getMessage(), e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * Initializes input/output streams for client communication.
     * 
     * @throws IOException if stream initialization fails
     */
    private void initializeStreams() throws IOException {
        inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    
    /**
     * Processes incoming client messages.
     * 
     * @throws IOException if message processing fails
     */
    private void processClientMessages() throws IOException {
        String message;
        
        while (isConnected && (message = inputReader.readLine()) != null) {
            GameLogger.debug("Received from client " + clientId + ": " + message);
            
            try {
                processMessage(message);
            } catch (final Exception e) {
                GameLogger.error("Error processing client message: " + message, e);
                sendError("Error processing message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Processes a single client message.
     * 
     * @param message Message to process
     */
    private void processMessage(final String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        final String trimmedMessage = message.trim();
        
        // Handle different message types
        if (trimmedMessage.startsWith(NetworkProtocol.MOVE_COMMAND + NetworkProtocol.FIELD_DELIMITER)) {
            processMoveCommand(trimmedMessage);
        } else if (trimmedMessage.startsWith(NetworkProtocol.QUERY_COMMAND + NetworkProtocol.FIELD_DELIMITER)) {
            processQueryCommand(trimmedMessage);
        } else if (trimmedMessage.equalsIgnoreCase(NetworkProtocol.DISCONNECT_COMMAND)) {
            handleDisconnect();
        } else {
            // Plain response to a server prompt (e.g. player count, player name, noble choice)
            serverHandler.enqueueClientResponse(clientId, trimmedMessage);
        }
    }
    
    /**
     * Processes a move command from the client.
     * Validates the action type then forwards the raw command to the game engine
     * via the server's per-client response queue.
     *
     * @param command Move command (e.g. MOVE:TAKE:RGB, MOVE:BUY:42, MOVE:RESERVE:D2)
     */
    private void processMoveCommand(final String command) {
        final String[] parts = command.split(Constants.PROTOCOL_DELIMITER);

        if (parts.length < 3) {
            sendError("Invalid move command. Usage: MOVE:<action>:<params>");
            return;
        }

        final String action = parts[1].toUpperCase();

        switch (action) {
            case NetworkProtocol.ACTION_TAKE_3:
            case NetworkProtocol.ACTION_TAKE_2:
            case NetworkProtocol.ACTION_BUY:
            case NetworkProtocol.ACTION_RESERVE:
            case NetworkProtocol.ACTION_DISCARD:
                serverHandler.enqueueClientResponse(clientId, command);
                sendSuccess("Move received");
                break;
            default:
                sendError("Unknown move action: " + action
                        + ". Valid actions: TAKE_3, TAKE_2, BUY, RESERVE, DISCARD");
        }
    }
    
    /**
     * Processes a query command from the client.
     * 
     * @param command Query command
     */
    private void processQueryCommand(final String command) {
        // Parse query command format: QUERY:type
        final String[] parts = command.split(Constants.PROTOCOL_DELIMITER);
        
        if (parts.length < 2) {
            sendError("Invalid query command format");
            return;
        }
        
        final String queryType = parts[1];
        
        switch (queryType.toUpperCase()) {
            case "STATE":
                sendGameState();
                break;
            case "PLAYERS":
                sendPlayerList();
                break;
            case "BOARD":
                sendBoardState();
                break;
            default:
                sendError("Unknown query type: " + queryType);
        }
    }
    
    /**
     * Sends the current game state to the client.
     */
    private void sendGameState() {
        sendMessage(NetworkProtocol.SUCCESS_RESPONSE + ":Game state data");
    }
    
    /**
     * Sends the player list to the client.
     */
    private void sendPlayerList() {
        sendMessage(NetworkProtocol.SUCCESS_RESPONSE + ":Player list data");
    }
    
    /**
     * Sends the board state to the client.
     */
    private void sendBoardState() {
        sendMessage(NetworkProtocol.SUCCESS_RESPONSE + ":Board state data");
    }
    
    /**
     * Sends a success response to the client.
     * 
     * @param message Success message
     */
    private void sendSuccess(final String message) {
        sendMessage(NetworkProtocol.SUCCESS_RESPONSE + Constants.PROTOCOL_DELIMITER + message);
    }
    
    /**
     * Sends an error response to the client.
     * 
     * @param errorMessage Error message
     */
    private void sendError(final String errorMessage) {
        sendMessage(NetworkProtocol.ERROR_RESPONSE + Constants.PROTOCOL_DELIMITER + errorMessage);
    }
    
    /**
     * Sends a message to the client.
     * 
     * @param message Message to send
     */
    public void sendMessage(final String message) {
        if (outputWriter != null && isConnected) {
            outputWriter.println(message);
            GameLogger.debug("Sent to client " + clientId + ": " + message);
        }
    }
    
    /**
     * Sends the welcome message to the client.
     */
    private void sendWelcomeMessage() {
        sendMessage("Welcome to Splendor Network Game!");
        sendMessage("Commands: MOVE:action:params, QUERY:type, DISCONNECT");
    }
    
    /**
     * Handles client disconnection.
     */
    private void handleDisconnect() {
        GameLogger.info("Client requested disconnect: " + clientId);
        isConnected = false;
    }
    
    /**
     * Disconnects the client and cleans up resources.
     */
    public void disconnect() {
        isConnected = false;
        cleanup();
    }
    
    /**
     * Cleans up client resources.
     */
    private void cleanup() {
        serverHandler.unregisterClientQueue(clientId);
        try {
            if (outputWriter != null) {
                outputWriter.close();
            }
            if (inputReader != null) {
                inputReader.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (final IOException e) {
            GameLogger.error("Error cleaning up client resources", e);
        }
    }
    
    /**
     * Gets the client identifier.
     * 
     * @return Client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Gets the client network address.
     * 
     * @return Client address string
     */
    public String getClientAddress() {
        return clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    }
    
    /**
     * Checks if the client is connected.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected && !clientSocket.isClosed();
    }
}