package com.splendor.util;

import com.splendor.model.Gem;
import java.util.ArrayList;
import java.util.List;

public final class GemParser {
    private GemParser() {
    }

    public static Gem parseGem(final String token) {
        final String normalized = token.trim().toUpperCase();
        switch (normalized) {
            case "W":
            case "WHITE":
                return Gem.WHITE;
            case "B":
            case "BLUE":
                return Gem.BLUE;
            case "G":
            case "GREEN":
                return Gem.GREEN;
            case "R":
            case "RED":
                return Gem.RED;
            case "K":
            case "BLACK":
                return Gem.BLACK;
            case "AU":
            case "GOLD":
                return Gem.GOLD;
            default:
                throw new IllegalArgumentException("Unknown gem: " + token);
        }
    }

    public static Gem parseGemCode(final char code) {
        switch (Character.toUpperCase(code)) {
            case 'R':
                return Gem.RED;
            case 'G':
                return Gem.GREEN;
            case 'B':
                return Gem.BLUE;
            case 'W':
                return Gem.WHITE;
            case 'K':
                return Gem.BLACK;
            default:
                return null;
        }
    }

    public static List<Gem> parseGemSelection(final String input) {
        final String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please enter at least one color");
        }
        final String normalized = trimmed.toUpperCase();
        final String spaced = normalized.replaceAll("[^A-Z]+", " ").trim();
        if (!spaced.isEmpty() && spaced.contains(" ")) {
            final String[] parts = spaced.split("\\s+");
            final List<Gem> gems = new ArrayList<>();
            for (final String part : parts) {
                gems.add(parseGem(part));
            }
            return gems;
        }
        final String compact = spaced.isEmpty() ? normalized.replaceAll("[^A-Z]+", "") : spaced;
        return parseGemSequence(compact);
    }

    public static List<Gem> parseGemSequence(final String input) {
        final List<Gem> gems = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            if (i + 1 < input.length() && input.startsWith("AU", i)) {
                gems.add(Gem.GOLD);
                i += 2;
                continue;
            }
            gems.add(parseGem(String.valueOf(input.charAt(i))));
            i += 1;
        }
        return gems;
    }
}
