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
        if (trimmedMessage.startsWith(NetworkProtocol.MOVE_COMMAND)) {
            processMoveCommand(trimmedMessage);
        } else if (trimmedMessage.startsWith(NetworkProtocol.QUERY_COMMAND)) {
            processQueryCommand(trimmedMessage);
        } else if (trimmedMessage.equalsIgnoreCase(NetworkProtocol.DISCONNECT_COMMAND)) {
            handleDisconnect();
        } else {
            sendError("Unknown command: " + trimmedMessage);
        }
    }
    
    /**
     * Processes a move command from the client.
     * 
     * @param command Move command
     */
    private void processMoveCommand(final String command) {
        // Parse move command format: MOVE:action:parameters
        final String[] parts = command.split(Constants.PROTOCOL_DELIMITER);
        
        if (parts.length < 2) {
            sendError("Invalid move command format");
            return;
        }
        
        final String action = parts[1];
        
        try {
            // Process the move action
            switch (action.toUpperCase()) {
                case "TAKE":
                    processTakeAction(parts);
                    break;
                case "BUY":
                    processBuyAction(parts);
                    break;
                case "RESERVE":
                    processReserveAction(parts);
                    break;
                default:
                    sendError("Unknown move action: " + action);
            }
        } catch (final Exception e) {
            sendError("Move processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Processes a take action (gems).
     * 
     * @param parts Command parts
     */
    private void processTakeAction(final String[] parts) {
        // Implementation would process gem taking
        sendSuccess("Take action processed");
    }
    
    /**
     * Processes a buy action (card purchase).
     * 
     * @param parts Command parts
     */
    private void processBuyAction(final String[] parts) {
        // Implementation would process card purchase
        sendSuccess("Buy action processed");
    }
    
    /**
     * Processes a reserve action (card reservation).
     * 
     * @param parts Command parts
     */
    private void processReserveAction(final String[] parts) {
        // Implementation would process card reservation
        sendSuccess("Reserve action processed");
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