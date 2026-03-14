/**
 * Handles network server functionality for remote client connections.
 * Manages client connections and message routing for multiplayer network play.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented concurrent client handling
 */
package com.splendor.network;

import com.splendor.config.IConfigProvider;
import com.splendor.exception.SplendorException;
import com.splendor.util.Constants;
import com.splendor.util.GameLogger;
import com.splendor.view.RemoteView;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Network server that handles remote client connections.
 * Uses thread-per-client model for concurrent player handling.
 */
public class ServerSocketHandler implements RemoteView.NetworkMessageHandler {
    
    private final int serverPort;
    private final IConfigProvider configProvider;
    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private final List<ClientHandler> connectedClients;
    private volatile boolean isRunning;
    private final ConcurrentHashMap<String, LinkedBlockingQueue<String>> clientResponseQueues;
    private volatile CountDownLatch clientReadyLatch;
    
    /**
     * Creates a new ServerSocketHandler with the specified port and configuration.
     * 
     * @param serverPort Port to listen on
     * @param configProvider Configuration provider
     */
    public ServerSocketHandler(final int serverPort, final IConfigProvider configProvider) {
        this.serverPort = serverPort;
        this.configProvider = configProvider;
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.clientResponseQueues = new ConcurrentHashMap<>();
        this.clientReadyLatch = null;
        this.isRunning = false;
    }
    
    /**
     * Starts the network server and begins accepting client connections.
     * 
     * @throws SplendorException if server startup fails
     */
    public void startServer() throws SplendorException {
        try {
            GameLogger.info("Starting Splendor network server on port " + serverPort);
            
            // Force IPv4 wildcard (0.0.0.0) so all network interfaces are reachable
            final java.net.InetAddress wildcard = java.net.InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
            serverSocket = new ServerSocket(serverPort, 50, wildcard);

            // Configure server socket properties
            serverSocket.setReuseAddress(true);
            
            // Initialize thread pool for client handling
            final int maxClients = configProvider.getIntProperty("network.max_clients", Constants.MAX_CLIENT_CONNECTIONS);
            clientExecutor = Executors.newFixedThreadPool(maxClients);
            
            isRunning = true;

            // Print real IPv4 addresses, skipping virtual/loopback adapters
            final StringBuilder ips = new StringBuilder();
            for (final java.util.Enumeration<java.net.NetworkInterface> ifaces =
                    java.net.NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                final java.net.NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                final String name = iface.getDisplayName().toLowerCase();
                if (name.contains("virtual") || name.contains("vmware")
                        || name.contains("vbox") || name.contains("hyper-v")
                        || name.contains("wsl") || name.contains("loopback")) continue;
                for (final java.util.Enumeration<java.net.InetAddress> addrs = iface.getInetAddresses();
                        addrs.hasMoreElements();) {
                    final java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        ips.append("\n  ").append(iface.getDisplayName())
                           .append(" -> ").append(addr.getHostAddress()).append(":").append(serverPort);
                    }
                }
            }
            GameLogger.info("Server started. Clients can connect at:" + ips);
            
            // Start accepting connections
            acceptClientConnections();
            
        } catch (final IOException e) {
            throw new NetworkException("Failed to start server: " + e.getMessage(), e);
        }
    }
    
    /**
     * Accepts incoming client connections in a loop.
     * Each connection is handled in a separate thread.
     */
    private void acceptClientConnections() {
        while (isRunning && !serverSocket.isClosed()) {
            try {
                GameLogger.debug("Waiting for client connection...");
                final Socket clientSocket = serverSocket.accept();
                
                // Handle client connection in separate thread
                handleClientConnection(clientSocket);
                
            } catch (final IOException e) {
                if (isRunning) {
                    GameLogger.error("Error accepting client connection", e);
                }
            }
        }
    }
    
    /**
     * Handles a new client connection.
     * 
     * @param clientSocket Client socket
     */
    private void handleClientConnection(final Socket clientSocket) {
        try {
            final String clientAddress = clientSocket.getInetAddress().getHostAddress();
            GameLogger.info("New client connected from: " + clientAddress);
            
            // Check connection limit
            if (connectedClients.size() >= Constants.MAX_CLIENT_CONNECTIONS) {
                GameLogger.warn("Connection limit reached. Rejecting connection from: " + clientAddress);
                closeSocket(clientSocket);
                return;
            }
            
            // Create client handler
            final ClientHandler clientHandler = new ClientHandler(clientSocket, this, configProvider);
            connectedClients.add(clientHandler);
            if (clientReadyLatch != null) {
                clientReadyLatch.countDown();
            }
            
            // Handle client in separate thread
            clientExecutor.submit(() -> {
                try {
                    clientHandler.handleClient();
                } catch (final Exception e) {
                    GameLogger.error("Error handling client: " + clientAddress, e);
                } finally {
                    removeClient(clientHandler);
                }
            });
            
        } catch (final Exception e) {
            GameLogger.error("Failed to handle client connection", e);
            closeSocket(clientSocket);
        }
    }
    
    /**
     * Blocks until the given number of clients have connected, or the timeout elapses.
     *
     * @param count     Number of clients to wait for
     * @param timeoutMs Maximum wait time in milliseconds (0 = wait forever)
     * @return true if the required clients connected in time, false on timeout
     */
    public boolean waitForClients(final int count, final long timeoutMs) {
        clientReadyLatch = new CountDownLatch(count);
        try {
            if (timeoutMs <= 0) {
                clientReadyLatch.await();
                return true;
            }
            return clientReadyLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Returns the IDs of all currently connected clients.
     *
     * @return List of client ID strings
     */
    public List<String> getConnectedClientIds() {
        final List<String> ids = new java.util.ArrayList<>();
        for (final ClientHandler client : connectedClients) {
            ids.add(client.getClientId());
        }
        return ids;
    }

    /**
     * Removes a disconnected client.
     *
     * @param clientHandler Client handler to remove
     */
    private void removeClient(final ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
        GameLogger.info("Client disconnected. Active connections: " + connectedClients.size());
    }
    
    /**
     * Broadcasts a message to all connected clients.
     * 
     * @param message Message to broadcast
     */
    public void broadcastToAllClients(final String message) {
        for (final ClientHandler client : connectedClients) {
            try {
                if (client.isConnected()) {
                    client.sendMessage(message);
                }
            } catch (final Exception e) {
                GameLogger.error("Failed to send message to client", e);
            }
        }
    }
    
    /**
     * Sends a message to a specific client.
     * 
     * @param clientId Client identifier
     * @param message Message to send
     */
    @Override
    public void sendToClient(final String clientId, final String message) {
        for (final ClientHandler client : connectedClients) {
            if (client.getClientId().equals(clientId)) {
                try {
                    client.sendMessage(message);
                } catch (final Exception e) {
                    GameLogger.error("Failed to send message to client: " + clientId, e);
                }
                return;
            }
        }
        GameLogger.warn("Client not found: " + clientId);
    }
    
    /**
     * Registers a response queue for a client when they connect.
     *
     * @param clientId Client identifier
     */
    public void registerClientQueue(final String clientId) {
        clientResponseQueues.put(clientId, new LinkedBlockingQueue<>());
    }

    /**
     * Removes a client's response queue when they disconnect.
     *
     * @param clientId Client identifier
     */
    public void unregisterClientQueue(final String clientId) {
        clientResponseQueues.remove(clientId);
    }

    /**
     * Enqueues a response message from a client for the game to consume.
     *
     * @param clientId Client identifier
     * @param message  Message to enqueue
     */
    public void enqueueClientResponse(final String clientId, final String message) {
        final LinkedBlockingQueue<String> queue = clientResponseQueues.get(clientId);
        if (queue != null) {
            queue.offer(message);
        } else {
            GameLogger.warn("No response queue found for client: " + clientId);
        }
    }

    /**
     * Blocks until a response is available from the client or the timeout elapses.
     *
     * @param clientId  Client identifier
     * @param timeoutMs Maximum wait time in milliseconds
     * @return The next response string, or null on timeout
     */
    public String pollClientResponse(final String clientId, final long timeoutMs) {
        final LinkedBlockingQueue<String> queue = clientResponseQueues.get(clientId);
        if (queue == null) {
            return null;
        }
        try {
            return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Blocks until a response is available from the client or the timeout elapses.
     * Implements NetworkMessageHandler interface.
     *
     * @param clientId  Client identifier
     * @param timeoutMs Maximum wait time in milliseconds
     * @return The next response string, or null on timeout
     */
    @Override
    public String waitForClientResponse(final String clientId, final int timeoutMs) {
        return pollClientResponse(clientId, timeoutMs);
    }

    /**
     * Stops the server and cleans up resources.
     */
    public void stopServer() {
        GameLogger.info("Stopping server...");
        isRunning = false;
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (final IOException e) {
                GameLogger.error("Error closing server socket", e);
            }
        }
        
        // Disconnect all clients
        for (final ClientHandler client : connectedClients) {
            try {
                client.disconnect();
            } catch (final Exception e) {
                GameLogger.error("Error disconnecting client", e);
            }
        }
        connectedClients.clear();
        
        // Shutdown thread pool
        if (clientExecutor != null && !clientExecutor.isShutdown()) {
            clientExecutor.shutdown();
        }
        
        GameLogger.info("Server stopped");
    }
    
    /**
     * Gets the number of connected clients.
     * 
     * @return Active client count
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    /**
     * Gets the list of currently connected clients.
     * 
     * @return List of client handlers
     */
    public List<ClientHandler> getConnectedClients() {
        return Collections.unmodifiableList(connectedClients);
    }
    
    /**
     * Checks if the server is running.
     * 
     * @return true if server is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Closes a socket safely.
     * 
     * @param socket Socket to close
     */
    private void closeSocket(final Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (final IOException e) {
                GameLogger.error("Error closing socket", e);
            }
        }
    }
}