package com.splendor.view;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import java.util.*;

/**
 * Handles the rendering of game components to the terminal.
 * Separates visualization logic from input handling.
 */
public class GameRenderer {
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);
    private static final int LEFT_CONTENT_WIDTH = 38;
    private static final int TOP_PANEL_CONTENT_WIDTH = 20;
    private static final int MENU_CONTENT_WIDTH = 60;
    private static final int CARD_CONTENT_WIDTH = 14;
    private static final int CURRENT_PLAYER_CONTENT_WIDTH = 26;
    private static final int RECENT_MOVES_CONTENT_WIDTH = 40;
    private List<String> menuLines = List.of();
    private final MoveValidator moveValidator;

    public GameRenderer() {
        this.moveValidator = new MoveValidator();
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
        System.out.println(
                Colors.colorize("╔══════════════════════════════════════════════════════════════╗", Colors.GOLD));
        System.out.println(
                Colors.colorize("║                           SPLENDOR                           ║", Colors.GOLD));
        System.out.println(
                Colors.colorize("╚══════════════════════════════════════════════════════════════╝", Colors.GOLD));
    }

    public void displayGameState(final Game game) {
        clearDisplay();
        System.out.println(getRenderedGameState(game));
    }

    /**
     * Returns the full ASCII game state as a String.
     * Useful for network transmission.
     * 
     * @param game The current game state
     * @return Formatted ASCII board
     */
    public String getRenderedGameState(final Game game) {
        final List<String> leftPanel = renderCardTiers(game.getBoard(), game.getCurrentPlayer());
        final List<String> rightPanel = new ArrayList<>();
        rightPanel.addAll(renderTopRow(game.getBoard(), game.getPlayers(), game.getCurrentPlayer()));
        
        final List<String> currentPlayer = renderCurrentPlayer(game.getCurrentPlayer(), game.getMaxTokens(),
                CURRENT_PLAYER_CONTENT_WIDTH);
        final List<String> recentMoves = renderRecentMoves(game.getRecentMoves(), RECENT_MOVES_CONTENT_WIDTH);
        
        rightPanel.addAll(combineHorizontal(List.of(currentPlayer, recentMoves), 2));
        rightPanel.addAll(renderMenu(menuLines));
        
        return combineSideBySide(leftPanel, rightPanel);
    }

    /**
     * Renders the full game state to a String instead of printing to stdout.
     * Used by the network layer to send the board to remote clients.
     *
     * @param game Current game state
     * @return The complete board layout as a single String with embedded newlines
     */
    public String renderToString(final Game game) {
        final List<String> leftPanel = renderCardTiers(game.getBoard(), game.getCurrentPlayer());
        final List<String> rightPanel = new ArrayList<>();
        rightPanel.addAll(renderTopRow(game.getBoard(), game.getPlayers(), game.getCurrentPlayer()));
        final List<String> currentPlayerPanel = renderCurrentPlayer(game.getCurrentPlayer(), game.getMaxTokens(),
                CURRENT_PLAYER_CONTENT_WIDTH);
        final List<String> recentMoves = renderRecentMoves(game.getRecentMoves(), RECENT_MOVES_CONTENT_WIDTH);
        rightPanel.addAll(combineHorizontal(List.of(currentPlayerPanel, recentMoves), 2));
        rightPanel.addAll(renderMenu(menuLines));
        return sideBySideToString(leftPanel, rightPanel);
    }

    private String sideBySideToString(final List<String> left, final List<String> right) {
        int leftWidth = 0;
        for (final String line : left) {
            leftWidth = Math.max(leftWidth, stripAnsi(line).length());
        }
        final StringBuilder sb = new StringBuilder();
        final int maxLines = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLines; i++) {
            String leftLine = i < left.size() ? left.get(i) : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            leftLine = padRightAnsi(leftLine, leftWidth);
            sb.append(leftLine).append("  ").append(rightLine).append("\n");
        }
        return sb.toString();
    }

    /**
     * Displays the complete game board with a side-by-side layout.
     * Left Panel: Gem Bank, Nobles, Opponents, Status.
     * Right Panel: Card Levels.
     * 
     * @param board         The game board state
     * @param currentPlayer The player whose turn it is (for affordability checks)
     */
    public void displayBoard(final Board board, final Player currentPlayer) {
        final List<String> leftPanel = renderCardTiers(board, currentPlayer);
        final List<String> rightPanel = new ArrayList<>();
        rightPanel.addAll(renderTopRow(board, List.of(currentPlayer), currentPlayer));
        printSideBySide(leftPanel, rightPanel);
    }

    private String stripAnsi(String str) {
        return str.replaceAll("\u001B\\[[;\\d]*m", "").replaceAll("\u001B\\[\\d+;\\d+;\\d+m", "");
    }

    public String formatCardAscii(Card card) {
        return formatCardAscii(card, true);
    }

    public String formatCardAscii(Card card, boolean affordable) {
        final String borderColor = affordable ? Colors.WHITE : Colors.DIM;
        final String textColor = affordable ? "" : Colors.DIM;
        final String bonusLabel = card.getBonusGem() == null
                ? "-"
                : (affordable
                        ? Colors.colorize(gemLabel(card.getBonusGem()), Colors.getGemColor(card.getBonusGem()))
                        : Colors.colorize(gemLabel(card.getBonusGem()), Colors.DIM));
        final String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : "-";
        final String line1 = Colors.colorize("┌" + "─".repeat(CARD_CONTENT_WIDTH) + "┐", borderColor);
        final String line2 = Colors.colorize("│", borderColor)
                + padRightAnsi(applyDim("ID: " + card.getId(), textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line3 = Colors.colorize("│", borderColor)
                + padRightAnsi(applyDim("Pts: " + points, textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line4 = Colors.colorize("│", borderColor)
                + padRightAnsi(applyDim("Bonus: ", textColor) + bonusLabel, CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line5 = Colors.colorize("│", borderColor)
                + padRightAnsi(applyDim("Cost:", textColor), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final List<String> costLines = affordable ? formatCardCostLines(card, 2) : formatCardCostLinesDim(card, 2);
        final String line6 = Colors.colorize("│", borderColor)
                + padRightAnsi(costLines.get(0), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line7 = Colors.colorize("│", borderColor)
                + padRightAnsi(costLines.get(1), CARD_CONTENT_WIDTH)
                + Colors.colorize("│", borderColor);
        final String line8 = Colors.colorize("└" + "─".repeat(CARD_CONTENT_WIDTH) + "┘", borderColor);

        return String.join("\n", line1, line2, line3, line4, line5, line6, line7, line8);
    }

    private String applyDim(final String text, final String dimColor) {
        if (dimColor.isEmpty()) {
            return text;
        }
        return Colors.colorize(text, dimColor);
    }

    private String padRightAnsi(String s, int visibleWidth) {
        final String truncated = truncateAnsi(s, visibleWidth);
        final int currentVisible = stripAnsi(truncated).length();
        final int padding = visibleWidth - currentVisible;
        if (padding > 0) {
            return truncated + " ".repeat(padding);
        }
        return truncated;
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
                System.out.printf("%s%d ",
                        Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())),
                        entry.getValue());
            }
        }
        if (!hasTokens)
            System.out.print("- ");

        // Compact bonus display
        System.out.print("| Bon: ");
        boolean hasBonuses = false;
        for (Map.Entry<Gem, Integer> entry : player.getGemDiscounts().entrySet()) {
            if (entry.getValue() > 0) {
                hasBonuses = true;
                System.out.printf("%s%d ",
                        Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())),
                        entry.getValue());
            }
        }
        if (!hasBonuses)
            System.out.print("-");
        System.out.println();
    }

    /**
     * Displays the status bar for the current player.
     * Shows detailed stats including points, specific discounts, and token
     * inventory.
     * 
     * @param currentPlayer The player whose turn it is
     */
    public void displayStatus(final Player currentPlayer) {
        System.out
                .println("\n" + Colors.colorize("╔════════════════════ STATUS BAR ════════════════════╗", Colors.CYAN));
        System.out.printf("║ PLAYER: %-15s POINTS: %-17d ║%n",
                currentPlayer.getName(), currentPlayer.getTotalPoints());

        System.out.print("║ DISCOUNTS: ");
        StringBuilder discSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getGemDiscounts().entrySet()) {
            if (entry.getValue() > 0) {
                discSb.append(
                        Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                        .append(":")
                        .append(entry.getValue())
                        .append(" ");
            }
        }
        if (discSb.length() == 0)
            discSb.append("None");
        System.out.print(discSb.toString());
        System.out.println(Colors.colorize("", Colors.RESET));

        System.out.print("║ TOKENS:    ");
        StringBuilder tokSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getTokens().entrySet()) {
            if (entry.getValue() > 0) {
                tokSb.append(
                        Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                        .append(":")
                        .append(entry.getValue())
                        .append(" ");
            }
        }
        if (tokSb.length() == 0)
            tokSb.append("None");
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
        return renderBank(board, LEFT_CONTENT_WIDTH);
    }

    private List<String> renderBank(final Board board, final int contentWidth) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(contentWidth + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("BANK", Colors.WHITE, contentWidth));
        for (final String line : formatGemCountsVertical(board.getGemBank(), true)) {
            lines.add(frameLine(line, Colors.WHITE, contentWidth));
        }
        lines.add(colorBorder("└" + "─".repeat(contentWidth + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderNobles(final Board board) {
        return renderNobles(board, LEFT_CONTENT_WIDTH);
    }

    private List<String> renderNobles(final Board board, final int contentWidth) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(contentWidth + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("NOBLES", Colors.WHITE, contentWidth));
        if (board.getAvailableNobles().isEmpty()) {
            lines.add(frameLine("None", Colors.WHITE, contentWidth));
        } else {
            for (final Noble noble : board.getAvailableNobles()) {
                final String header = String.format("N%d %dpts", noble.getId(), noble.getPoints());
                lines.add(frameLine(header, Colors.WHITE, contentWidth));
                for (final String reqLine : formatRequirementsLines(noble.getRequirements(), contentWidth)) {
                    lines.add(frameLine("Req: " + reqLine, Colors.WHITE, contentWidth));
                }
            }
        }
        lines.add(colorBorder("└" + "─".repeat(contentWidth + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderCurrentPlayer(final Player player, final int maxTokens, final int contentWidth) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(contentWidth + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("CURRENT", Colors.WHITE, contentWidth));
        lines.add(frameLine("Name: " + player.getName(), Colors.WHITE, contentWidth));
        lines.add(frameLine("Score: " + player.getTotalPoints(), Colors.WHITE, contentWidth));
        final String tokenStr = formatGemCounts(player.getTokens(), false);
        lines.add(frameLine("Tokens " + player.getTotalTokenCount() + "/" + maxTokens + ": " + tokenStr, Colors.WHITE,
                contentWidth));
        final String bonusStr = formatGemCounts(player.getGemDiscounts(), false);
        lines.add(frameLine("Bonuses: " + bonusStr, Colors.WHITE, contentWidth));
        lines.add(frameLine("Reserved: " + formatReserved(player.getReservedCards()), Colors.WHITE, contentWidth));
        lines.add(colorBorder("└" + "─".repeat(contentWidth + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderRecentMoves(final List<String> moves, final int contentWidth) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(contentWidth + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("MOVES", Colors.WHITE, contentWidth));
        final List<String> padded = new ArrayList<>(Collections.nCopies(5, "-"));
        final int startIndex = Math.max(0, 5 - moves.size());
        for (int i = 0; i < moves.size(); i++) {
            padded.set(startIndex + i, moves.get(i));
        }
        for (int i = 0; i < 5; i++) {
            lines.add(frameLine((i + 1) + ") " + padded.get(i), Colors.WHITE, contentWidth));
        }
        lines.add(colorBorder("└" + "─".repeat(contentWidth + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderCardTiers(final Board board, final Player currentPlayer) {
        final List<String> lines = new ArrayList<>();
        for (int tier = 3; tier >= 1; tier--) {
            final List<Card> cards = board.getAvailableCards().get(tier);
            if (cards == null || cards.isEmpty()) {
                continue;
            }
            lines.add(Colors.colorize("LEVEL " + tier + ":  " + board.getDeckSize(tier) + " cards available in deck",
                    Colors.WHITE));
            final List<String> tierLines = new ArrayList<>();
            final int cardHeight = formatCardAscii(cards.get(0)).split("\n").length;
            for (int i = 0; i < cardHeight; i++) {
                tierLines.add("");
            }
            for (final Card card : cards) {
                final boolean affordable = moveValidator.canPlayerAffordCard(currentPlayer, card);
                final String[] cardLines = formatCardAscii(card, affordable).split("\n");
                for (int i = 0; i < cardLines.length; i++) {
                    tierLines.set(i, tierLines.get(i) + cardLines[i] + "  ");
                }
            }
            lines.addAll(tierLines);
        }
        return lines;
    }

    private void printSideBySide(final List<String> left, final List<String> right) {
        System.out.println(combineSideBySide(left, right));
    }

    private String combineSideBySide(final List<String> left, final List<String> right) {
        final StringBuilder sb = new StringBuilder();
        int leftWidth = 0;
        for (final String line : left) {
            leftWidth = Math.max(leftWidth, stripAnsi(line).length());
        }
        final int maxLines = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLines; i++) {
            String leftLine = i < left.size() ? left.get(i) : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            leftLine = padRightAnsi(leftLine, leftWidth);
            sb.append(leftLine).append("  ").append(rightLine).append("\n");
        }
        return sb.toString();
    }

    private List<String> renderPlayersTrack(final List<Player> players, final Player currentPlayer) {
        return renderPlayersTrack(players, currentPlayer, LEFT_CONTENT_WIDTH);
    }

    private List<String> renderPlayersTrack(final List<Player> players, final Player currentPlayer,
            final int contentWidth) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(contentWidth + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("PLAYERS", Colors.WHITE, contentWidth));
        for (final Player player : players) {
            final String name = player == currentPlayer
                    ? Colors.colorize(player.getName(), Colors.GOLD)
                    : Colors.colorize(player.getName(), Colors.CYAN);
            lines.add(frameLine(name + " (" + player.getTotalPoints() + ")", Colors.WHITE, contentWidth));
        }
        lines.add(colorBorder("└" + "─".repeat(contentWidth + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderMenu(final List<String> menu) {
        final List<String> lines = new ArrayList<>();
        lines.add(colorBorder("┌" + "─".repeat(MENU_CONTENT_WIDTH + 2) + "┐", Colors.WHITE));
        lines.add(frameLine("MENU", Colors.WHITE, MENU_CONTENT_WIDTH));
        if (menu == null || menu.isEmpty()) {
            lines.add(frameLine("None", Colors.WHITE, MENU_CONTENT_WIDTH));
        } else {
            for (final String line : menu) {
                lines.add(frameLine(line, Colors.WHITE, MENU_CONTENT_WIDTH));
            }
        }
        lines.add(colorBorder("└" + "─".repeat(MENU_CONTENT_WIDTH + 2) + "┘", Colors.WHITE));
        return lines;
    }

    public void setMenuLines(final List<String> menuLines) {
        this.menuLines = menuLines == null ? List.of() : new ArrayList<>(menuLines);
    }

    /**
     * Builds the menu line list from a set of MenuOptions.
     * Shared by both ConsoleView and RemoteView so the menu always looks the same.
     *
     * @param options Available menu options for the current player
     * @return List of formatted menu lines ready to pass to setMenuLines()
     */
    public List<String> buildMenuLines(final List<MenuOption> options) {
        final List<String> lines = new ArrayList<>();
        lines.add("Goal: 15 points");
        lines.add("Pick one action (or 'Z' to Undo)");
        for (final MenuOption option : options) {
            final String base = option.getNumber() + ") " + option.getLabel() + ": ";
            final String detail = option.getDetail();
            final String reason = option.isAvailable() || option.getReason().isBlank()
                    ? "" : " (" + option.getReason() + ")";
            final String line = base + detail + reason;
            lines.add(option.isAvailable() ? line : Colors.colorize(line, Colors.DIM));
        }
        return lines;
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

    private List<String> formatGemCountsVertical(final Map<Gem, Integer> counts, final boolean includeZero) {
        final List<String> lines = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (includeZero || count > 0) {
                final String label = Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)) + ": " + count;
                lines.add(label);
            }
        }
        if (lines.isEmpty()) {
            lines.add("None");
        }
        return lines;
    }

    private List<String> formatRequirementsLines(final Map<Gem, Integer> requirements) {
        return formatRequirementsLines(requirements, LEFT_CONTENT_WIDTH);
    }

    private List<String> formatRequirementsLines(final Map<Gem, Integer> requirements, final int contentWidth) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = requirements.getOrDefault(gem, 0);
            if (count > 0) {
                tokens.add(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)) + count);
            }
        }
        if (tokens.isEmpty()) {
            return List.of("None");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (stripAnsi(next).length() > Math.max(1, contentWidth - 5) && !current.isEmpty()) {
                lines.add(current);
                current = token;
            } else {
                current = next;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
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

    private List<String> formatCardCostLines(final Card card, final int maxLines) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = card.getCost().getOrDefault(gem, 0);
            if (count > 0) {
                tokens.add(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)) + count);
            }
        }
        if (tokens.isEmpty()) {
            return List.of("None", "");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (stripAnsi(next).length() > CARD_CONTENT_WIDTH && !current.isEmpty()) {
                lines.add(current);
                current = token;
            } else {
                current = next;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        if (lines.size() < maxLines) {
            while (lines.size() < maxLines) {
                lines.add("");
            }
            return lines;
        }
        if (lines.size() > maxLines) {
            final String merged = String.join(" ", lines.subList(1, lines.size()));
            return List.of(lines.get(0), truncateAnsi(merged, CARD_CONTENT_WIDTH));
        }
        return lines;
    }

    private List<String> formatCardCostLinesDim(final Card card, final int maxLines) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = card.getCost().getOrDefault(gem, 0);
            if (count > 0) {
                tokens.add(Colors.colorize(gemLabel(gem) + count, Colors.DIM));
            }
        }
        if (tokens.isEmpty()) {
            return List.of(Colors.colorize("None", Colors.DIM), "");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (stripAnsi(next).length() > CARD_CONTENT_WIDTH && !current.isEmpty()) {
                lines.add(current);
                current = token;
            } else {
                current = next;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        if (lines.size() < maxLines) {
            while (lines.size() < maxLines) {
                lines.add("");
            }
            return lines;
        }
        if (lines.size() > maxLines) {
            final String merged = String.join(" ", lines.subList(1, lines.size()));
            return List.of(lines.get(0), truncateAnsi(merged, CARD_CONTENT_WIDTH));
        }
        return lines;
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
        if (gem == Gem.WHITE) {
            return "W";
        }
        if (gem == Gem.BLUE) {
            return "B";
        }
        if (gem == Gem.GREEN) {
            return "G";
        }
        if (gem == Gem.RED) {
            return "R";
        }
        if (gem == Gem.BLACK) {
            return "K";
        }
        if (gem == Gem.GOLD) {
            return "Au";
        }
        return "";
    }

    private String frameLine(final String content, final String color) {
        return frameLine(content, color, LEFT_CONTENT_WIDTH);
    }

    private String frameLine(final String content, final String color, final int contentWidth) {
        final String padded = padRightAnsi(content, contentWidth);
        return Colors.colorize("│", color) + " " + padded + " " + Colors.colorize("│", color);
    }

    private String colorBorder(final String line, final String color) {
        return Colors.colorize(line, color);
    }

    private String truncateAnsi(final String s, final int maxVisible) {
        if (maxVisible <= 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        int visible = 0;
        int i = 0;
        while (i < s.length() && visible < maxVisible) {
            final char c = s.charAt(i);
            if (c == '\u001B') {
                sb.append(c);
                i++;
                while (i < s.length()) {
                    final char c2 = s.charAt(i);
                    sb.append(c2);
                    i++;
                    if (c2 == 'm') {
                        break;
                    }
                }
            } else {
                sb.append(c);
                i++;
                visible++;
            }
        }
        if (i < s.length() && s.contains("\u001B")) {
            sb.append(Colors.RESET);
        }
        return sb.toString();
    }

    private List<String> renderTopRow(final Board board, final List<Player> players, final Player currentPlayer) {
        final int bankWidth = getBankContentWidth(board);
        final List<String> bank = renderBank(board, bankWidth);
        final List<String> nobles = renderNobles(board, TOP_PANEL_CONTENT_WIDTH);
        final int playersWidth = getPlayersTrackContentWidth(players);
        final List<String> playersTrack = renderPlayersTrack(players, currentPlayer, playersWidth);
        return combineHorizontal(List.of(bank, nobles, playersTrack), 2);
    }

    private int getBankContentWidth(final Board board) {
        int maxContent = "BANK".length();
        for (final String line : formatGemCountsVertical(board.getGemBank(), true)) {
            maxContent = Math.max(maxContent, stripAnsi(line).length());
        }
        return Math.max(8, maxContent);
    }

    private int getPlayersTrackContentWidth(final List<Player> players) {
        int maxContent = "PLAYERS".length();
        for (final Player player : players) {
            final String content = player.getName() + " (" + player.getTotalPoints() + ")";
            maxContent = Math.max(maxContent, stripAnsi(content).length());
        }
        return Math.max(10, maxContent);
    }

    private List<String> combineHorizontal(final List<List<String>> blocks, final int gap) {
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
                if (b > 0) {
                    row.append(" ".repeat(gap));
                }
                final List<String> block = blocks.get(b);
                final String line = i < block.size() ? block.get(i) : "";
                row.append(padRightAnsi(line, widths.get(b)));
            }
            combined.add(row.toString());
        }
        return combined;
    }
}
