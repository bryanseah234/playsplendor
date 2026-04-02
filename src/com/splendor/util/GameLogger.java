/**
 * Simple logging utility for application events and errors.
 * Provides basic logging functionality for debugging and monitoring.
 */
package com.splendor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging facility for the Splendor game application.
 * 
 * This utility class provides timestamped logging with multiple severity levels
 * (INFO, ERROR, DEBUG, WARN) to help track application behavior during both
 * development and runtime. All log messages are prefixed with a timestamp and
 * log level for easy filtering and debugging.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Timestamped log entries with configurable severity levels</li>
 *   <li>Optional debug mode for verbose output including stack traces</li>
 *   <li>Specialized logging methods for game state, player actions, and network events</li>
 *   <li>Consistent log format across the entire application</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 *   GameLogger.info("Game started");
 *   GameLogger.error("Failed to load configuration", exception);
 *   GameLogger.debug("Detailed trace information");
 * </pre>
 * 
 * <p>This class is thread-safe and designed to be used throughout the application
 * without instantiation.
 * 
 * @see GameLogger#LOG_FORMAT For the log message format template
 * @see GameLogger#LOG_LEVEL_INFO For standard log level identifiers
 */
public class GameLogger {
    
    /** Prevent instantiation of this static utility class. */
    private GameLogger() {}
    @Override
	public String toString() {
		return "GameLogger []";
	}

	private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static boolean debugEnabled = false;
    
    /** Format string template for log messages. */
    public static final String LOG_FORMAT = "[%s] %s: %s";
    /** Identifier for INFO level logs. */
    public static final String LOG_LEVEL_INFO = "INFO";
    /** Identifier for ERROR level logs. */
    public static final String LOG_LEVEL_ERROR = "ERROR";
    /** Identifier for DEBUG level logs. */
    public static final String LOG_LEVEL_DEBUG = "DEBUG";
    /** Identifier for WARN level logs. */
    public static final String LOG_LEVEL_WARN = "WARN";

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
        log(LOG_LEVEL_INFO, message);
    }
    
    /**
     * Logs an error message.
     * 
     * @param message Error message to log
     */
    public static void error(final String message) {
        log(LOG_LEVEL_ERROR, message);
    }
    
    /**
     * Logs an error message with exception details.
     * 
     * @param message Error message to log
     * @param exception Exception to log
     */
    public static void error(final String message, final Throwable exception) {
        log(LOG_LEVEL_ERROR, message + " - " + exception.getMessage());
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
            log(LOG_LEVEL_DEBUG, message);
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
        final String formattedMessage = String.format(LOG_FORMAT, 
                                                    timestamp, level, message);
        System.out.println(formattedMessage);
    }

}