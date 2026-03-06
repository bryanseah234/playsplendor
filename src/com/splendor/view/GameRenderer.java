package com.splendor.view;

import com.splendor.model.*;
import java.util.*;

/**
 * Handles the rendering of game components to the terminal.
 * Separates visualization logic from input handling.
 */
public class GameRenderer {
    private static final List<Gem> GEM_ORDER = List.of(
        Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD
    );
    private static final int LEFT_CONTENT_WIDTH = 38;
    private static final int CARD_CONTENT_WIDTH = 20;

    public GameRenderer() {
    }

    /**
     * Clears the terminal display.
     * Uses ANSI escape codes for modern terminals and attempts system-specific
     * commands (cls/clear) for Windows/Unix compatibility.
     */
    public void clearDisplay() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback to ANSI if system command fails
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    /**
     * Renders the game header banner.
     */
    public void displayHeader() {
        System.out.println(Colors.colorize("╔══════════════════════════════════════════════════════════════╗", Colors.GOLD));
        System.out.println(Colors.colorize("║                           SPLENDOR                           ║", Colors.GOLD));
        System.out.println(Colors.colorize("╚══════════════════════════════════════════════════════════════╝", Colors.GOLD));
    }

    public void displayGameState(final Game game) {
        clearDisplay();
        displayHeader();
        final List<String> leftPanel = new ArrayList<>();
        leftPanel.addAll(renderBank(game.getBoard()));
        leftPanel.add("");
        leftPanel.addAll(renderNobles(game.getBoard()));
        leftPanel.add("");
        leftPanel.addAll(renderCurrentPlayer(game.getCurrentPlayer(), game.getMaxTokens()));
        final List<String> rightPanel = renderCardTiers(game.getBoard());
        printSideBySide(leftPanel, rightPanel);
        System.out.println();
        displayPlayersTrack(game.getPlayers(), game.getCurrentPlayer());
    }

    /**
     * Displays the complete game board with a side-by-side layout.
     * Left Panel: Gem Bank, Nobles, Opponents, Status.
     * Right Panel: Card Levels.
     * 
     * @param board The game board state
     * @param currentPlayer The player whose turn it is (for affordability checks)
     */
    public void displayBoard(final Board board, final Player currentPlayer) {
        final List<String> leftPanel = new ArrayList<>();
        leftPanel.addAll(renderBank(board));
        leftPanel.add("");
        leftPanel.addAll(renderNobles(board));
        final List<String> rightPanel = renderCardTiers(board);
        printSideBySide(leftPanel, rightPanel);
    }
    
    private String stripAnsi(String str) {
        return str.replaceAll("\u001B\\[[;\\d]*m", "");
    }
    public String formatCardAscii(Card card) {
        final String borderColor = Colors.WHITE;
        final String bonusLabel = card.getBonusGem() == null
            ? "-"
            : Colors.colorize(gemLabel(card.getBonusGem()), Colors.getGemColor(card.getBonusGem()));
        final String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : "-";
        final String line1 = Colors.colorize("┌" + "─".repeat(CARD_CONTENT_WIDTH) + "┐", borderColor);
        final String line2Content = String.format("ID:%-3d P:%-2s B:%s", card.getId(), points, bonusLabel);
        final String line2 = Colors.colorize("│", borderColor)
            + padRightAnsi(line2Content, CARD_CONTENT_WIDTH)
            + Colors.colorize("│", borderColor);
        final String line3Content = "Cost: " + formatCardCost(card);
        final String line3 = Colors.colorize("│", borderColor)
            + padRightAnsi(line3Content, CARD_CONTENT_WIDTH)
            + Colors.colorize("│", borderColor);
        final String line4 = Colors.colorize("└" + "─".repeat(CARD_CONTENT_WIDTH) + "┘", borderColor);

        return String.join("\n", line1, line2, line3, line4);
    }
    
    private String padRightAnsi(String s, int visibleWidth) {
        int currentVisible = stripAnsi(s).length();
        int padding = visibleWidth - currentVisible;
        if (padding > 0) {
            return s + " ".repeat(padding);
        }
        return s;
    }

    private String formatRequirements(Map<Gem, Integer> reqs) {
        return formatGemCounts(reqs, false);
    }

    public void displayPlayers(final List<Player> players) {
        System.out.println("\n" + Colors.colorize("--- OPPONENTS ---", Colors.WHITE));
        for (final Player player : players) {
            displayPlayerSummary(player);
        }
    }

    private void displayPlayerSummary(final Player player) {
        System.out.printf("%s: %d pts | Res: %d | ",
            Colors.colorize(player.getName(), Colors.CYAN), player.getTotalPoints(),
            player.getReservedCards().size());
            
        // Compact token display
        System.out.print("Tok: ");
        boolean hasTokens = false;
        for (Map.Entry<Gem, Integer> entry : player.getTokens().entrySet()) {
            if (entry.getValue() > 0) {
                hasTokens = true;
                System.out.printf("%s%d ", Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())), entry.getValue());
            }
        }
        if (!hasTokens) System.out.print("- ");
        
        // Compact bonus display
        System.out.print("| Bon: ");
        boolean hasBonuses = false;
        for (Map.Entry<Gem, Integer> entry : player.getGemDiscounts().entrySet()) {
            if (entry.getValue() > 0) {
                hasBonuses = true;
                System.out.printf("%s%d ", Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())), entry.getValue());
            }
        }
        if (!hasBonuses) System.out.print("-");
        System.out.println();
    }

    /**
     * Displays the status bar for the current player.
     * Shows detailed stats including points, specific discounts, and token inventory.
     * 
     * @param currentPlayer The player whose turn it is
     */
    public void displayStatus(final Player currentPlayer) {
        System.out.println("\n" + Colors.colorize("╔════════════════════ STATUS BAR ════════════════════╗", Colors.CYAN));
        System.out.printf("║ PLAYER: %-15s POINTS: %-17d ║%n", 
            currentPlayer.getName(), currentPlayer.getTotalPoints());
        
        System.out.print("║ DISCOUNTS: ");
        StringBuilder discSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getGemDiscounts().entrySet()) {
             if (entry.getValue() > 0) {
                 discSb.append(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                       .append(":")
                       .append(entry.getValue())
                       .append(" ");
             }
        }
        if (discSb.length() == 0) discSb.append("None");
        System.out.print(discSb.toString());
        System.out.println(Colors.colorize("", Colors.RESET)); 
        
        System.out.print("║ TOKENS:    ");
        StringBuilder tokSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getTokens().entrySet()) {
             if (entry.getValue() > 0) {
                 tokSb.append(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                      .append(":")
                      .append(entry.getValue())
                      .append(" ");
             }
        }
        if (tokSb.length() == 0) tokSb.append("None");
        System.out.println(tokSb.toString());

        String reservedText;
        if (currentPlayer.getReservedCards().isEmpty()) {
            reservedText = "None";
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            for (Card card : currentPlayer.getReservedCards()) {
                joiner.add(String.valueOf(card.getId()));
            }
            reservedText = joiner.toString();
        }
        System.out.println("║ RESERVED: " + padRightAnsi(reservedText, 38) + " ║");
        System.out.println(Colors.colorize("╚════════════════════════════════════════════════════╝", Colors.CYAN));
    }

    public void displayPlayerTokens(final Player player) {
        player.getTokens().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .forEach(entry -> System.out.printf("%s: %d | ", 
                Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())), 
                entry.getValue()));
        System.out.println();
    }

    private List<String> renderBank(final Board board) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("BANK", Colors.WHITE));
        lines.add(frameLine(formatGemCounts(board.getGemBank(), true), Colors.WHITE));
        lines.add(colorBorder("└" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderNobles(final Board board) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┐", Colors.PURPLE));
        lines.add(frameLine("NOBLES", Colors.PURPLE));
        if (board.getAvailableNobles().isEmpty()) {
            lines.add(frameLine("None", Colors.PURPLE));
        } else {
            for (final Noble noble : board.getAvailableNobles()) {
                final String nobleLine = String.format("N%d %dpts %s", noble.getId(), noble.getPoints(), formatRequirements(noble.getRequirements()));
                lines.add(frameLine(nobleLine, Colors.PURPLE));
            }
        }
        lines.add(colorBorder("└" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┘", Colors.PURPLE));
        return lines;
    }

    private List<String> renderCurrentPlayer(final Player player, final int maxTokens) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┐", Colors.CYAN));
        lines.add(frameLine("CURRENT PLAYER", Colors.CYAN));
        lines.add(frameLine("Name: " + player.getName(), Colors.CYAN));
        lines.add(frameLine("Score: " + player.getTotalPoints(), Colors.CYAN));
        lines.add(frameLine("Tokens " + player.getTotalTokenCount() + "/" + maxTokens + ": " + formatGemCounts(player.getTokens(), false), Colors.CYAN));
        lines.add(frameLine("Bonuses: " + formatGemCounts(player.getGemDiscounts(), false), Colors.CYAN));
        lines.add(frameLine("Reserved: " + formatReserved(player.getReservedCards()), Colors.CYAN));
        lines.add(colorBorder("└" + "─".repeat(LEFT_CONTENT_WIDTH + 2) + "┘", Colors.CYAN));
        return lines;
    }

    private List<String> renderCardTiers(final Board board) {
        final List<String> lines = new ArrayList<>();
        for (int tier = 3; tier >= 1; tier--) {
            final List<Card> cards = board.getAvailableCards().get(tier);
            if (cards == null || cards.isEmpty()) {
                continue;
            }
            lines.add(Colors.colorize("LEVEL " + tier + "  Deck: " + board.getDeckSize(tier), Colors.WHITE));
            final List<String> tierLines = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                tierLines.add("");
            }
            for (final Card card : cards) {
                final String[] cardLines = formatCardAscii(card).split("\n");
                for (int i = 0; i < cardLines.length; i++) {
                    tierLines.set(i, tierLines.get(i) + cardLines[i] + "  ");
                }
            }
            lines.addAll(tierLines);
            lines.add("");
        }
        return lines;
    }

    private void printSideBySide(final List<String> left, final List<String> right) {
        final int maxLines = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLines; i++) {
            String leftLine = i < left.size() ? left.get(i) : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            leftLine = padRightAnsi(leftLine, LEFT_CONTENT_WIDTH + 4);
            System.out.println(leftLine + "  " + rightLine);
        }
    }

    private void displayPlayersTrack(final List<Player> players, final Player currentPlayer) {
        final StringBuilder sb = new StringBuilder();
        sb.append(Colors.colorize("Players: ", Colors.WHITE));
        for (final Player player : players) {
            final String name = player == currentPlayer
                ? Colors.colorize(player.getName(), Colors.GOLD)
                : Colors.colorize(player.getName(), Colors.CYAN);
            sb.append(name).append("(").append(player.getTotalPoints()).append(") ");
        }
        System.out.println(sb.toString().trim());
    }

    private String formatGemCounts(final Map<Gem, Integer> counts, final boolean includeZero) {
        final StringBuilder sb = new StringBuilder();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (includeZero || count > 0) {
                sb.append(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)))
                  .append(count)
                  .append(" ");
            }
        }
        if (sb.length() == 0) {
            return "None";
        }
        return sb.toString().trim();
    }

    private String formatCardCost(final Card card) {
        final StringBuilder sb = new StringBuilder();
        for (final Gem gem : GEM_ORDER) {
            final int count = card.getCost().getOrDefault(gem, 0);
            if (count > 0) {
                sb.append(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)))
                  .append(count)
                  .append(" ");
            }
        }
        if (sb.length() == 0) {
            return "None";
        }
        return sb.toString().trim();
    }

    private String formatReserved(final List<Card> reserved) {
        if (reserved.isEmpty()) {
            return "None";
        }
        final StringJoiner joiner = new StringJoiner(", ");
        for (final Card card : reserved) {
            joiner.add(String.valueOf(card.getId()));
        }
        return joiner.toString();
    }

    private String gemLabel(final Gem gem) {
        return switch (gem) {
            case WHITE -> "W";
            case BLUE -> "B";
            case GREEN -> "G";
            case RED -> "R";
            case BLACK -> "K";
            case GOLD -> "Au";
        };
    }

    private String frameLine(final String content, final String color) {
        final String padded = padRightAnsi(content, LEFT_CONTENT_WIDTH);
        return Colors.colorize("│", color) + " " + padded + " " + Colors.colorize("│", color);
    }

    private String colorBorder(final String line, final String color) {
        return Colors.colorize(line, color);
    }
}
