/**
 * Handles individual client connections in the network layer.
 * Manages communication with a single remote client.
 * 
 */
package com.splendor.network;

import com.splendor.exception.NetworkException;
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
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private volatile boolean isConnected;
    
    /**
     * Creates a new ClientHandler for the specified socket.
     * 
     * @param clientSocket Client socket
     * @param serverHandler Parent server handler
     */
    public ClientHandler(final Socket clientSocket, final ServerSocketHandler serverHandler) {
        this.clientId = UUID.randomUUID().toString();
        this.clientSocket = clientSocket;
        this.serverHandler = serverHandler;
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
            GameLogger.info("Client handler initialized for: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
            sendMessage("Connected to Splendor server. Awaiting lobby instructions...");
            
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
     * Processes a single client message by forwarding it to the server's response queue.
     * A blank line (bare Enter keypress) is enqueued as "" to unblock waitForEnter().
     * All other input is enqueued as-is for consumption by the game engine.
     *
     * @param message Message received from the client
     */
    private void processMessage(final String message) {
        if (message == null) {
            return;
        }
        serverHandler.enqueueClientResponse(clientId, message.trim());
    }

    /**
     * Sends an error notification to the client.
     *
     * @param errorMessage Error message
     */
    private void sendError(final String errorMessage) {
        sendMessage("ERROR: " + errorMessage);
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
     * Returns true if the client socket is open and the connection is active.
     */
    boolean isConnected() {
        return isConnected && !clientSocket.isClosed();
    }
}
