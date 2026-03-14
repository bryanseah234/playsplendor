/**
 * Defines the network communication protocol between server and clients.
 * Specifies message formats and command structures for network gameplay.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.network;

/**
 * Defines network protocol constants and message formats.
 * Provides standardized communication between server and clients.
 */
public final class NetworkProtocol {
    
    // Command prefixes
    public static final String MOVE_COMMAND = "MOVE";
    public static final String QUERY_COMMAND = "QUERY";
    public static final String DISCONNECT_COMMAND = "DISCONNECT";
    
    // Response prefixes
    public static final String SUCCESS_RESPONSE = "SUCCESS";
    public static final String ERROR_RESPONSE = "ERROR";
    public static final String STATE_RESPONSE = "STATE";
    
    // Move action types
    public static final String ACTION_TAKE_3 = "TAKE_3";
    public static final String ACTION_TAKE_2 = "TAKE_2";
    public static final String ACTION_BUY = "BUY";
    public static final String ACTION_RESERVE = "RESERVE";
    public static final String ACTION_DISCARD = "DISCARD";
    
    // Query types
    public static final String QUERY_STATE = "STATE";
    public static final String QUERY_PLAYERS = "PLAYERS";
    public static final String QUERY_BOARD = "BOARD";
    public static final String QUERY_MOVES = "MOVES";
    
    // Message delimiters
    public static final String MESSAGE_DELIMITER = "\n";
    public static final String FIELD_DELIMITER = ":";
    
    // Protocol constants
    public static final int MAX_MESSAGE_LENGTH = 1024;
    public static final int MAX_COMMAND_PARTS = 10;
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private NetworkProtocol() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    /**
     * Validates a protocol message format.
     * 
     * @param message Message to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidMessage(final String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return false;
        }
        
        final String[] parts = message.split(FIELD_DELIMITER);
        return parts.length > 0 && parts.length <= MAX_COMMAND_PARTS;
    }
    
    /**
     * Creates a formatted protocol message.
     * 
     * @param command Command type
     * @param parameters Command parameters
     * @return Formatted message
     */
    public static String createMessage(final String command, final String... parameters) {
        final StringBuilder message = new StringBuilder(command);
        
        for (final String param : parameters) {
            message.append(FIELD_DELIMITER).append(param);
        }
        
        return message.toString();
    }
    
    /**
     * Creates a success response message.
     * 
     * @param data Response data
     * @return Success response message
     */
    public static String createSuccessResponse(final String data) {
        return createMessage(SUCCESS_RESPONSE, data);
    }
    
    /**
     * Creates an error response message.
     * 
     * @param errorMessage Error message
     * @return Error response message
     */
    public static String createErrorResponse(final String errorMessage) {
        return createMessage(ERROR_RESPONSE, errorMessage);
    }
    
    /**
     * Parses a protocol message into components.
     * 
     * @param message Message to parse
     * @return Array of message components
     */
    public static String[] parseMessage(final String message) {
        if (message == null || message.isEmpty()) {
            return new String[0];
        }
        
        return message.split(FIELD_DELIMITER);
    }
    
    /**
     * Gets the command type from a message.
     * 
     * @param message Message to examine
     * @return Command type or empty string if invalid
     */
    public static String getCommandType(final String message) {
        final String[] parts = parseMessage(message);
        return parts.length > 0 ? parts[0] : "";
    }
}