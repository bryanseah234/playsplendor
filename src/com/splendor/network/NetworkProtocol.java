package com.splendor.network;

/**
 * Utility helpers for Splendor's lightweight text-based network protocol.
 * Provides message validation, creation, and parsing helpers used by tests and
 * networking components.
 */
public final class NetworkProtocol {

    public static final int MAX_MESSAGE_LENGTH = 1024;

    private NetworkProtocol() {
        // Utility class
    }

    /**
     * Validates a protocol message by checking null/empty and max length bounds.
     *
     * @param message Message to validate
     * @return true if message is non-null, non-empty, and within max length
     */
    public static boolean isValidMessage(final String message) {
        return message != null
                && !message.trim().isEmpty()
                && message.length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Creates a colon-delimited protocol message from parts.
     *
     * @param parts Message parts
     * @return Joined message string
     */
    public static String createMessage(final String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        return String.join(":", parts);
    }

    /**
     * Splits a protocol message into colon-delimited parts.
     *
     * @param message Message string
     * @return Parsed parts, or empty array for null input
     */
    public static String[] parseMessage(final String message) {
        if (message == null) {
            return new String[0];
        }
        return message.split(":");
    }
}
