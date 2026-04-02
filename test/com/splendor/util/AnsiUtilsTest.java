package com.splendor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AnsiUtils}.
 * Validates ANSI-aware string manipulation: strip, pad, truncate, combine.
 */
@DisplayName("AnsiUtils Tests")
class AnsiUtilsTest {

    private static final String ESC = "\u001B";
    private static final String RED = ESC + "[31m";
    private static final String RESET = ESC + "[0m";

    // ── stripAnsi ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stripAnsi removes color codes from string")
    void stripAnsiRemovesCodes() {
        String colored = RED + "Hello" + RESET;
        assertEquals("Hello", AnsiUtils.stripAnsi(colored));
    }

    @Test
    @DisplayName("stripAnsi is no-op for plain text")
    void stripAnsiPlainText() {
        assertEquals("Hello", AnsiUtils.stripAnsi("Hello"));
    }

    @Test
    @DisplayName("stripAnsi handles multiple color codes")
    void stripAnsiMultipleCodes() {
        String s = RED + "A" + RESET + RED + "B" + RESET;
        assertEquals("AB", AnsiUtils.stripAnsi(s));
    }

    // ── padRightAnsi ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("padRightAnsi pads plain text to target width")
    void padRightPlainText() {
        String result = AnsiUtils.padRightAnsi("Hi", 5);
        assertEquals("Hi   ", result);
    }

    @Test
    @DisplayName("padRightAnsi pads colored text to correct visible width")
    void padRightColoredText() {
        String colored = RED + "Hi" + RESET;
        String result = AnsiUtils.padRightAnsi(colored, 5);
        // Visible chars should be "Hi" + 3 spaces = 5 visible chars
        assertEquals(5, AnsiUtils.stripAnsi(result).length());
    }

    @Test
    @DisplayName("padRightAnsi truncates text exceeding target width")
    void padRightTruncates() {
        String result = AnsiUtils.padRightAnsi("Hello World", 5);
        assertEquals(5, AnsiUtils.stripAnsi(result).length());
    }

    @Test
    @DisplayName("padRightAnsi with exact-width text returns text unchanged")
    void padRightExactWidth() {
        String result = AnsiUtils.padRightAnsi("exact", 5);
        assertEquals("exact", result);
    }

    // ── truncateAnsi ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("truncateAnsi preserves short plain text")
    void truncateShortPlainText() {
        assertEquals("Hi", AnsiUtils.truncateAnsi("Hi", 5));
    }

    @Test
    @DisplayName("truncateAnsi truncates long plain text")
    void truncateLongPlainText() {
        String result = AnsiUtils.truncateAnsi("Hello World", 5);
        assertEquals("Hello", AnsiUtils.stripAnsi(result));
    }

    @Test
    @DisplayName("truncateAnsi preserves ANSI codes but counts only visible chars")
    void truncatePreservesAnsi() {
        String colored = RED + "Hello World" + RESET;
        String result = AnsiUtils.truncateAnsi(colored, 5);
        // Visible text should be "Hello"
        assertEquals("Hello", AnsiUtils.stripAnsi(result));
        // Should still contain the RED code
        assertTrue(result.contains(RED));
    }

    @Test
    @DisplayName("truncateAnsi appends reset when truncation occurs in colored text")
    void truncateAppendsReset() {
        String colored = RED + "Hello World" + RESET;
        String result = AnsiUtils.truncateAnsi(colored, 3);
        // Should end with reset to prevent color bleed
        assertTrue(result.endsWith(RESET));
    }

    @Test
    @DisplayName("truncateAnsi with maxVisible 0 returns empty string")
    void truncateZeroReturnsEmpty() {
        assertEquals("", AnsiUtils.truncateAnsi("anything", 0));
    }

    // ── combineHorizontal ────────────────────────────────────────────────────

    @Test
    @DisplayName("combineHorizontal joins two blocks side by side")
    void combineHorizontalTwoBlocks() {
        List<String> left = List.of("AB", "CD");
        List<String> right = List.of("12", "34");
        List<List<String>> blocks = List.of(left, right);

        List<String> result = AnsiUtils.combineHorizontal(blocks, 2);

        assertEquals(2, result.size(), "Should have 2 rows");
        // Row 0: "AB" + 2-space gap + "12"
        assertTrue(result.get(0).contains("AB"));
        assertTrue(result.get(0).contains("12"));
        // Row 1: "CD" + 2-space gap + "34"
        assertTrue(result.get(1).contains("CD"));
        assertTrue(result.get(1).contains("34"));
    }

    @Test
    @DisplayName("combineHorizontal pads shorter blocks with blank lines")
    void combineHorizontalUnevenBlocks() {
        List<String> tall = List.of("A", "B", "C");
        List<String> short_ = List.of("1");
        List<List<String>> blocks = List.of(tall, short_);

        List<String> result = AnsiUtils.combineHorizontal(blocks, 1);

        assertEquals(3, result.size(), "Should pad short block to match tall block");
    }

    @Test
    @DisplayName("combineHorizontal with single block returns padded block")
    void combineHorizontalSingleBlock() {
        List<String> block = List.of("Hello", "Hi");
        List<List<String>> blocks = List.of(block);

        List<String> result = AnsiUtils.combineHorizontal(blocks, 0);

        assertEquals(2, result.size());
        // Both lines should be padded to the max visible width (5 for "Hello")
        assertEquals(5, AnsiUtils.stripAnsi(result.get(0)).length());
        assertEquals(5, AnsiUtils.stripAnsi(result.get(1)).length());
    }

    @Test
    @DisplayName("combineHorizontal with ANSI-colored blocks aligns correctly")
    void combineHorizontalWithAnsi() {
        List<String> left = List.of(RED + "AB" + RESET, "CD");
        List<String> right = List.of("12", "34");
        List<List<String>> blocks = List.of(left, right);

        List<String> result = AnsiUtils.combineHorizontal(blocks, 1);

        assertEquals(2, result.size());
        // Visible width of each row should be consistent
        int row0Width = AnsiUtils.stripAnsi(result.get(0)).length();
        int row1Width = AnsiUtils.stripAnsi(result.get(1)).length();
        assertEquals(row0Width, row1Width, "All rows should have same visible width");
    }
}
