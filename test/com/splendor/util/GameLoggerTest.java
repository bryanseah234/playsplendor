package com.splendor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GameLogger}.
 * Captures System.out to verify the formatted log output at each level.
 */
@DisplayName("GameLogger Tests")
class GameLoggerTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        // Reset debug mode before each test
        GameLogger.setDebugEnabled(false);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        GameLogger.setDebugEnabled(false);
    }

    // ── Log format ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("LOG_FORMAT constant matches expected pattern")
    void logFormatConstant() {
        assertEquals("[%s] %s: %s", GameLogger.LOG_FORMAT);
    }

    @Test
    @DisplayName("Level constants have expected values")
    void levelConstants() {
        assertEquals("INFO", GameLogger.LOG_LEVEL_INFO);
        assertEquals("ERROR", GameLogger.LOG_LEVEL_ERROR);
        assertEquals("DEBUG", GameLogger.LOG_LEVEL_DEBUG);
        assertEquals("WARN", GameLogger.LOG_LEVEL_WARN);
    }

    // ── info() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("info() prints message with INFO level and timestamp")
    void infoLogsWithInfoLevel() {
        GameLogger.info("Game started");

        String output = capturedOut.toString();
        assertTrue(output.contains("INFO"), "Output should contain INFO level");
        assertTrue(output.contains("Game started"), "Output should contain the message");
    }

    @Test
    @DisplayName("info() output follows LOG_FORMAT pattern")
    void infoOutputFollowsFormat() {
        GameLogger.info("test");

        String output = capturedOut.toString().trim();
        // Should match pattern: [YYYY-MM-DD HH:mm:ss] INFO: test
        assertTrue(output.matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] INFO: test"),
                "Output should match format: " + output);
    }

    // ── error() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("error(message) prints message with ERROR level")
    void errorLogsWithErrorLevel() {
        GameLogger.error("Something broke");

        String output = capturedOut.toString();
        assertTrue(output.contains("ERROR"), "Output should contain ERROR level");
        assertTrue(output.contains("Something broke"), "Output should contain the message");
    }

    @Test
    @DisplayName("error(message, exception) includes exception message")
    void errorWithExceptionIncludesExceptionMessage() {
        Exception ex = new RuntimeException("disk full");
        GameLogger.error("Write failed", ex);

        String output = capturedOut.toString();
        assertTrue(output.contains("ERROR"), "Output should contain ERROR level");
        assertTrue(output.contains("Write failed"), "Output should contain the message");
        assertTrue(output.contains("disk full"), "Output should contain exception message");
    }

    @Test
    @DisplayName("error(message, exception) does not print stack trace when debug is disabled")
    void errorWithExceptionNoStackTraceWhenDebugOff() {
        GameLogger.setDebugEnabled(false);
        GameLogger.error("fail", new RuntimeException("boom"));

        String output = capturedOut.toString();
        // Stack trace output would contain "at " or "RuntimeException"
        // Only one line expected: the formatted message
        String[] lines = output.trim().split("\\R");
        assertEquals(1, lines.length, "Without debug, only 1 log line expected");
    }

    // ── debug() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("debug() is silent when debug is disabled")
    void debugSilentWhenDisabled() {
        GameLogger.setDebugEnabled(false);
        GameLogger.debug("trace info");

        assertEquals("", capturedOut.toString(), "No output expected when debug is off");
    }

    @Test
    @DisplayName("debug() prints message when debug is enabled")
    void debugPrintsWhenEnabled() {
        GameLogger.setDebugEnabled(true);
        GameLogger.debug("trace info");

        String output = capturedOut.toString();
        assertTrue(output.contains("DEBUG"), "Output should contain DEBUG level");
        assertTrue(output.contains("trace info"), "Output should contain the message");
    }

    // ── warn() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("warn() prints message with WARN level")
    void warnLogsWithWarnLevel() {
        GameLogger.warn("Low memory");

        String output = capturedOut.toString();
        assertTrue(output.contains("WARN"), "Output should contain WARN level");
        assertTrue(output.contains("Low memory"), "Output should contain the message");
    }

    // ── setDebugEnabled() ────────────────────────────────────────────────────

    @Test
    @DisplayName("setDebugEnabled toggles debug output")
    void toggleDebugMode() {
        // Off -> silent
        GameLogger.setDebugEnabled(false);
        GameLogger.debug("hidden");
        assertEquals("", capturedOut.toString());

        // On -> visible
        GameLogger.setDebugEnabled(true);
        GameLogger.debug("visible");
        assertFalse(capturedOut.toString().isEmpty());
    }

    // ── toString (instance) ──────────────────────────────────────────────────

    @Test
    @DisplayName("toString returns class identifier")
    void toStringReturnsIdentifier() {
        GameLogger logger = new GameLogger();
        assertNotNull(logger.toString());
        assertEquals("GameLogger []", logger.toString());
    }
}
