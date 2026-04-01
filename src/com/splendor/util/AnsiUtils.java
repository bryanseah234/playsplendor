package com.splendor.util;

import java.util.*;

/**
 * Utility class for terminal string manipulation that is aware of ANSI escape codes.
 *
 * Standard Java string operations (length, substring, padding) measure characters
 * by code-unit count, which breaks whenever a string contains ANSI color sequences
 * like "\u001B[32m". These sequences add invisible bytes that inflate the apparent
 * length, causing misaligned table columns in the terminal UI.
 *
 * Every method in this class strips or skips ANSI sequences when counting visible
 * width, so the rendered output always aligns correctly regardless of coloring.
 */
public final class AnsiUtils {

    /** Prevent instantiation of this utility class. */
    private AnsiUtils() {}

    /**
     * Removes all ANSI CSI SGR escape sequences (color/style codes) from a string,
     * returning only the printable characters.
     * The pattern matches sequences of the form ESC [ ... m.
     *
     * @param str The string that may contain ANSI codes.
     * @return The string with all ANSI color/style codes removed.
     */
    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[0-9;]*m", "");
    }

    /**
     * Right-pads a string to a target visible width, respecting embedded ANSI codes.
     * If the string is already at or beyond visibleWidth visible characters, it is
     * truncated to fit exactly.
     *
     * @param s            The string to pad (may contain ANSI codes).
     * @param visibleWidth The desired number of visible (non-ANSI) characters in the result.
     * @return The padded or truncated string, preserving ANSI codes.
     */
    public static String padRightAnsi(String s, int visibleWidth) {
        final String truncated = truncateAnsi(s, visibleWidth); // ensure it fits
        final int currentVisible = stripAnsi(truncated).length(); // visible chars after truncation
        final int padding = visibleWidth - currentVisible;        // spaces needed to reach target width
        if (padding > 0) {
            return truncated + " ".repeat(padding);
        }
        return truncated;
    }

    /**
     * Truncates a string to at most maxVisible printable characters, preserving ANSI
     * codes intact and appending a reset sequence if any part of the string is cut off.
     *
     * The algorithm walks the string character by character, counting only non-ANSI
     * characters toward the visible limit, and copies entire escape sequences verbatim
     * without counting them. A trailing reset ("\u001B[0m") is appended if truncation
     * occurred, to avoid color bleed onto subsequent text.
     *
     * @param s          The string to truncate (may contain ANSI codes).
     * @param maxVisible Maximum number of visible characters to retain.
     * @return The truncated string with ANSI codes intact.
     */
    public static String truncateAnsi(final String s, final int maxVisible) {
        if (maxVisible <= 0) return "";
        final StringBuilder sb = new StringBuilder();
        int visible = 0; // count of non-ANSI characters written so far
        int i = 0;
        boolean inAnsi = false; // tracks whether we are currently inside an escape sequence
        while (i < s.length() && visible < maxVisible) {
            final char c = s.charAt(i);
            if (c == '\u001B') {
                // Start of an ANSI escape sequence: copy it verbatim without counting visible chars.
                inAnsi = true;
                sb.append(c);
                i++;
                while (i < s.length()) {
                    final char c2 = s.charAt(i);
                    sb.append(c2);
                    i++;
                    if (c2 == 'm') {
                        inAnsi = false; // 'm' terminates a CSI SGR sequence
                        break;
                    }
                }
            } else {
                // Normal printable character: counts toward the visible limit.
                sb.append(c);
                i++;
                visible++;
            }
        }
        // If we stopped before the end of the string, reset colors to prevent bleed.
        if (i < s.length()) {
            sb.append("\u001B[0m");
        }
        return sb.toString();
    }

    /**
     * Joins multiple vertical text blocks side-by-side with a fixed horizontal gap,
     * producing a single list of lines suitable for printing as a multi-column layout.
     *
     * Each block is left-padded to its own maximum visible width so that columns
     * line up even when individual lines have different lengths or ANSI codes.
     * Shorter blocks are padded with blank lines to match the tallest block's height.
     *
     * @param blocks List of column blocks; each block is a list of pre-colored lines.
     * @param gap    Number of space characters to insert between adjacent columns.
     * @return A single list of combined rows ready for sequential printing.
     */
    public static List<String> combineHorizontal(final List<List<String>> blocks, final int gap) {
        // First pass: determine the visible width and height of each block.
        final List<Integer> widths = new ArrayList<>();
        int maxHeight = 0;
        for (final List<String> block : blocks) {
            int width = 0;
            for (final String line : block) {
                width = Math.max(width, stripAnsi(line).length());
            }
            widths.add(width);
            maxHeight = Math.max(maxHeight, block.size());
        }

        // Second pass: assemble each output row by stitching one line from each block.
        final List<String> combined = new ArrayList<>();
        for (int i = 0; i < maxHeight; i++) {
            final StringBuilder row = new StringBuilder();
            for (int b = 0; b < blocks.size(); b++) {
                if (b > 0) row.append(" ".repeat(gap)); // gap between columns
                final List<String> block = blocks.get(b);
                // Use an empty string for rows beyond a short block's height.
                final String line = i < block.size() ? block.get(i) : "";
                row.append(padRightAnsi(line, widths.get(b))); // pad to column width
            }
            combined.add(row.toString());
        }
        return combined;
    }
}
