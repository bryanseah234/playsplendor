package com.splendor.network;

/**
 * Abstraction over the network transport used by remote views.
 * Allows RemoteView to depend on an interface instead of socket implementation details.
 */
public interface NetworkMessageHandler {

    /**
     * Sends a message string to the specified client over the network.
     *
     * @param clientId The unique identifier of the target client connection.
     * @param message  The text to transmit.
     */
    void sendToClient(String clientId, String message);

    /**
     * Blocks until the specified client sends a response line or the timeout elapses.
     *
     * @param clientId  The unique identifier of the client to wait on.
     * @param timeoutMs Maximum time to wait in milliseconds.
     * @return The response string from the client, or null if the timeout expired.
     */
    String waitForClientResponse(String clientId, int timeoutMs);
}
