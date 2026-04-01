package com.splendor.network;

/**
 * Protocol utility for validating, creating, and parsing network messages.
 */
public class NetworkProtocol {

    /**
     * Maximum allowed length for a single network message.
     */
    public static final int MAX_MESSAGE_LENGTH = 1024;

    /**
     * Checks if a network message is valid.
     * 
     * @param message the message to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return false;
        }
        return message.startsWith("MOVE:") || message.startsWith("QUERY:") || message.startsWith("DISCONNECT");
    }

    /**
     * Creates a formatted network message.
     * 
     * @param parts parts of the message to join with colon
     * @return formatted message
     */
    public static String createMessage(String... parts) {
        return String.join(":", parts);
    }

    /**
     * Parses a network message into its components.
     * 
     * @param message the message to parse
     * @return array of message parts
     */
    public static String[] parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return new String[0];
        }
        return message.split(":");
    }
}
