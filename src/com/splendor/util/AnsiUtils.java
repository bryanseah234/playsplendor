package com.splendor.util;

import java.util.*;

public final class AnsiUtils {
    private AnsiUtils() {}

    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[0-9;]*m", "");
    }

    public static String padRightAnsi(String s, int visibleWidth) {
        final String truncated = truncateAnsi(s, visibleWidth);
        final int currentVisible = stripAnsi(truncated).length();
        final int padding = visibleWidth - currentVisible;
        if (padding > 0) {
            return truncated + " ".repeat(padding);
        }
        return truncated;
    }

    public static String truncateAnsi(final String s, final int maxVisible) {
        if (maxVisible <= 0) return "";
        final StringBuilder sb = new StringBuilder();
        int visible = 0;
        int i = 0;
        boolean inAnsi = false;
        while (i < s.length() && visible < maxVisible) {
            final char c = s.charAt(i);
            if (c == '\u001B') {
                inAnsi = true;
                sb.append(c);
                i++;
                while (i < s.length()) {
                    final char c2 = s.charAt(i);
                    sb.append(c2);
                    i++;
                    if (c2 == 'm') {
                        inAnsi = false;
                        break;
                    }
                }
            } else {
                sb.append(c);
                i++;
                visible++;
            }
        }
        if (i < s.length()) {
            sb.append("\u001B[0m");
        }
        return sb.toString();
    }

    public static List<String> combineHorizontal(final List<List<String>> blocks, final int gap) {
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
        final List<String> combined = new ArrayList<>();
        for (int i = 0; i < maxHeight; i++) {
            final StringBuilder row = new StringBuilder();
            for (int b = 0; b < blocks.size(); b++) {
                if (b > 0) row.append(" ".repeat(gap));
                final List<String> block = blocks.get(b);
                final String line = i < block.size() ? block.get(i) : "";
                row.append(padRightAnsi(line, widths.get(b)));
            }
            combined.add(row.toString());
        }
        return combined;
    }
}
