/**
 * Simple logging utility for application events and errors.
 * Provides basic logging functionality for debugging and monitoring.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for application events.
 * Provides timestamped logging with different severity levels.
 */
public class GameLogger {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static boolean debugEnabled = false;
    
    /**
     * Enables or disables debug logging.
     * 
     * @param enabled true to enable debug logging, false to disable
     */
    public static void setDebugEnabled(final boolean enabled) {
        debugEnabled = enabled;
    }
    
    /**
     * Logs an informational message.
     * 
     * @param message Message to log
     */
    public static void info(final String message) {
        log(Constants.LOG_LEVEL_INFO, message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message Error message to log
     */
    public static void error(final String message) {
        log(Constants.LOG_LEVEL_ERROR, message);
    }
    
    /**
     * Logs an error message with exception details.
     * 
     * @param message Error message to log
     * @param exception Exception to log
     */
    public static void error(final String message, final Throwable exception) {
        log(Constants.LOG_LEVEL_ERROR, message + " - " + exception.getMessage());
        if (debugEnabled && exception != null) {
            exception.printStackTrace();
        }
    }
    
    /**
     * Logs a debug message (only if debug is enabled).
     * 
     * @param message Debug message to log
     */
    public static void debug(final String message) {
        if (debugEnabled) {
            log(Constants.LOG_LEVEL_DEBUG, message);
        }
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message Warning message to log
     */
    public static void warn(final String message) {
        log("WARN", message);
    }
    
    /**
     * Logs a message with the specified level.
     * 
     * @param level Log level
     * @param message Message to log
     */
    private static void log(final String level, final String message) {
        final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        final String formattedMessage = String.format(Constants.LOG_FORMAT, 
                                                    timestamp, level, message);
        System.out.println(formattedMessage);
    }
    
    /**
     * Logs game state information.
     * 
     * @param game Current game state
     */
    public static void logGameState(final String context, final Object gameState) {
        info(context + ": " + gameState.toString());
    }
    
    /**
     * Logs player action.
     * 
     * @param playerName Player name
     * @param action Action description
     */
    public static void logPlayerAction(final String playerName, final String action) {
        info(String.format("Player '%s' performed action: %s", playerName, action));
    }
    
    /**
     * Logs configuration loading.
     * 
     * @param configSource Configuration source description
     */
    public static void logConfigurationLoaded(final String configSource) {
        info("Configuration loaded from: " + configSource);
    }
    
    /**
     * Logs network events.
     * 
     * @param event Network event description
     */
    public static void logNetworkEvent(final String event) {
        debug("Network: " + event);
    }
}