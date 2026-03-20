package com.splendor.view;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.util.AnsiUtils;
import java.util.*;

/**
 * Handles the rendering of game components to the terminal.
 * Separates visualization logic from input handling.
 */
public class GameRenderer {
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);
    private static final int MENU_CONTENT_WIDTH = 60;

    private static final int RECENT_MOVES_CONTENT_WIDTH = 96;
    private static final int PLAYER_BOX_WIDTH = 45;
    private static final int NOBLE_CARD_WIDTH = 16;

    private List<String> menuLines = List.of();
    private final MoveValidator moveValidator;

    public GameRenderer() {
        this.moveValidator = new MoveValidator();
    }

    public void clearDisplay() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\u001B[H\u001B[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.print("\u001B[H\u001B[2J");
            System.out.flush();
        }
    }

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

    public String getRenderedGameState(final Game game) {
        return renderGameStateInternal(game.getBoard(), game.getPlayers(), game.getCurrentPlayer(), game.getRecentMoves(), game.getMaxTokens());
    }

    public String renderToString(final Game game) {
        return renderGameStateInternal(game.getBoard(), game.getPlayers(), game.getCurrentPlayer(), game.getRecentMoves(), game.getMaxTokens());
    }

    public void displayBoard(final Board board, final Player currentPlayer) {
        System.out.println(renderGameStateInternal(board, List.of(currentPlayer), currentPlayer, List.of(), 10));
    }

    private String renderGameStateInternal(Board board, List<Player> players, Player currentPlayer, List<String> recentMoves, int maxTokens) {
        // Build LEFT column independently
        List<String> leftColumn = new ArrayList<>();
        List<List<String>> tiers = renderCardTiersList(board, currentPlayer);
        for (List<String> tier : tiers) {
            leftColumn.addAll(tier);
            leftColumn.add("");
        }
        List<String> leftMenu = renderMenuBox(menuLines);
        List<String> leftBank = renderBankVertical(board);
        leftColumn.addAll(combineSideBySide(leftMenu, leftBank));

        // Build RIGHT column independently
        List<String> rightColumn = new ArrayList<>();
        rightColumn.addAll(renderNoblesHorizontal(board));
        rightColumn.add("");
        rightColumn.addAll(renderRecentMovesBox(recentMoves));
        rightColumn.add("");
        rightColumn.addAll(renderPlayersTrackBoxes(players, currentPlayer, maxTokens));

        // Stitch columns side-by-side ONCE
        return combineSideBySideRaw(leftColumn, rightColumn);
    }

    private String combineSideBySideRaw(final List<String> left, final List<String> right) {
        final StringBuilder sb = new StringBuilder();
        int leftWidth = 0;
        for (final String line : left) {
            leftWidth = Math.max(leftWidth, AnsiUtils.stripAnsi(line).length());
        }
        final int maxLines = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLines; i++) {
            String leftLine = i < left.size() ? left.get(i) : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            leftLine = AnsiUtils.padRightAnsi(leftLine, leftWidth);
            sb.append(leftLine).append("  ").append(rightLine).append("\n");
        }
        return sb.toString();
    }

    private List<String> combineSideBySide(final List<String> left, final List<String> right) {
        List<String> res = new ArrayList<>();
        int leftWidth = 0;
        for (final String line : left) {
            leftWidth = Math.max(leftWidth, AnsiUtils.stripAnsi(line).length());
        }
        final int maxLines = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLines; i++) {
            String leftLine = i < left.size() ? left.get(i) : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            leftLine = AnsiUtils.padRightAnsi(leftLine, leftWidth);
            res.add(leftLine + "  " + rightLine);
        }
        return res;
    }

    private List<List<String>> renderCardTiersList(final Board board, final Player currentPlayer) {
        final List<List<String>> tiers = new ArrayList<>();
        for (int tier = 3; tier >= 1; tier--) {
            final List<String> lines = new ArrayList<>();
            final List<Card> cards = board.getAvailableCards().get(tier);
            if (cards == null || cards.isEmpty()) {
                lines.add(Colors.colorize("Level " + tier + ": 0 cards available in deck", Colors.WHITE));
                tiers.add(lines);
                continue;
            }
            lines.add(Colors.colorize("Level " + tier + ": " + board.getDeckSize(tier) + " cards available in deck", Colors.WHITE));
            final List<String> tierLines = new ArrayList<>();
            final int cardHeight = CardRenderer.formatCardAscii(cards.get(0)).split("\n").length;
            for (int i = 0; i < cardHeight; i++) {
                tierLines.add("");
            }
            for (final Card card : cards) {
                final boolean affordable = moveValidator.canPlayerAffordCard(currentPlayer, card);
                final String[] cardLines = CardRenderer.formatCardAscii(card, affordable).split("\n");
                for (int i = 0; i < cardLines.length; i++) {
                    tierLines.set(i, tierLines.get(i) + cardLines[i] + "  ");
                }
            }
            lines.addAll(tierLines);
            tiers.add(lines);
        }
        return tiers;
    }

    private List<String> renderNoblesHorizontal(Board board) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Nobles", Colors.WHITE));
        if (board.getAvailableNobles().isEmpty()) {
            lines.add("None");
            return lines;
        }

        List<List<String>> nobleBlocks = new ArrayList<>();
        for (Noble noble : board.getAvailableNobles()) {
            List<String> nLines = new ArrayList<>();
            // line 1: top border
            nLines.add(Colors.colorize("┌" + "─".repeat(NOBLE_CARD_WIDTH) + "┐", Colors.WHITE));
            // line 2: ID
            nLines.add(Colors.colorize("│", Colors.WHITE) + AnsiUtils.padRightAnsi("ID: N" + noble.getId(), NOBLE_CARD_WIDTH) + Colors.colorize("│", Colors.WHITE));
            // line 3: Points
            nLines.add(Colors.colorize("│", Colors.WHITE) + AnsiUtils.padRightAnsi("Pts: " + noble.getPoints(), NOBLE_CARD_WIDTH) + Colors.colorize("│", Colors.WHITE));
            // line 4: "Needs:" label
            nLines.add(Colors.colorize("│", Colors.WHITE) + AnsiUtils.padRightAnsi("Needs:", NOBLE_CARD_WIDTH) + Colors.colorize("│", Colors.WHITE));
            // lines 5-6: requirements (2 lines, padded with blank if fewer)
            List<String> reqs = formatRequirementsLines(noble.getRequirements(), NOBLE_CARD_WIDTH);
            for (int i = 0; i < 2; i++) {
                String r = i < reqs.size() ? reqs.get(i) : "";
                nLines.add(Colors.colorize("│", Colors.WHITE) + AnsiUtils.padRightAnsi(r, NOBLE_CARD_WIDTH) + Colors.colorize("│", Colors.WHITE));
            }
            // line 7: blank padding for height parity with regular cards
            nLines.add(Colors.colorize("│", Colors.WHITE) + AnsiUtils.padRightAnsi("", NOBLE_CARD_WIDTH) + Colors.colorize("│", Colors.WHITE));
            // line 8: bottom border
            nLines.add(Colors.colorize("└" + "─".repeat(NOBLE_CARD_WIDTH) + "┘", Colors.WHITE));
            nobleBlocks.add(nLines);
        }
        lines.addAll(AnsiUtils.combineHorizontal(nobleBlocks, 2));
        return lines;
    }

    private List<String> renderRecentMovesBox(List<String> moves) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Move History", Colors.WHITE));
        lines.add(colorBorder("┌" + "─".repeat(RECENT_MOVES_CONTENT_WIDTH + 2) + "┐", Colors.WHITE));
        List<String> padded = new ArrayList<>(Collections.nCopies(5, "-"));
        final int startIndex = Math.max(0, 5 - moves.size());
        for (int i = 0; i < moves.size(); i++) {
            padded.set(startIndex + i, moves.get(i));
        }
        for (int i = 0; i < 5; i++) {
            lines.add(frameLine((i + 1) + ") " + padded.get(i), Colors.WHITE, RECENT_MOVES_CONTENT_WIDTH));
        }
        lines.add(colorBorder("└" + "─".repeat(RECENT_MOVES_CONTENT_WIDTH + 2) + "┘", Colors.WHITE));
        return lines;
    }

    private List<String> renderPlayersTrackBoxes(List<Player> players, Player currentPlayer, int maxTokens) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Players (" + players.size() + "/4)", Colors.WHITE));
        
        List<List<String>> playerBlocks = new ArrayList<>();
        for (Player p : players) {
            List<String> pLines = new ArrayList<>();
            String borderCol = p == currentPlayer ? Colors.GOLD : Colors.WHITE;
            pLines.add(colorBorder("┌" + "─".repeat(PLAYER_BOX_WIDTH + 2) + "┐", borderCol));
            
            String nameScore = p.getName() + " - Score: " + p.getTotalPoints();
            if (p == currentPlayer) nameScore = Colors.colorize(nameScore, Colors.GOLD);
            
            pLines.add(frameLine(nameScore, borderCol, PLAYER_BOX_WIDTH));
            
            String bonusStr = formatGemCounts(p.getGemDiscounts(), true);
            pLines.add(frameLine("Bonus: " + bonusStr, borderCol, PLAYER_BOX_WIDTH));
            
            String tokenStr = formatGemCounts(p.getTokens(), true);
            pLines.add(frameLine("Tokens (" + p.getTotalTokenCount() + "/" + maxTokens + "): " + tokenStr, borderCol, PLAYER_BOX_WIDTH));
            
            String noblesStr = "None";
            if (!p.getNobles().isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                for (Noble n : p.getNobles()) sj.add(String.valueOf(n.getId()));
                noblesStr = sj.toString();
            }
            pLines.add(frameLine("Nobles Bought: " + noblesStr, borderCol, PLAYER_BOX_WIDTH));
            
            String cardsStr = "None";
            if (!p.getPurchasedCards().isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                for (Card c : p.getPurchasedCards()) sj.add(String.valueOf(c.getId()));
                cardsStr = sj.toString();
            }
            pLines.add(frameLine("Cards Bought: " + cardsStr, borderCol, PLAYER_BOX_WIDTH));
            
            String res = "None";
            if (!p.getReservedCards().isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                for (Card c : p.getReservedCards()) sj.add(String.valueOf(c.getId()));
                res = sj.toString();
            }
            pLines.add(frameLine("Cards Reserved: " + res, borderCol, PLAYER_BOX_WIDTH));
            
            pLines.add(colorBorder("└" + "─".repeat(PLAYER_BOX_WIDTH + 2) + "┘", borderCol));
            playerBlocks.add(pLines);
        }
        
        // Stack horizontally 2 by 2
        List<List<String>> combinedRows = new ArrayList<>();
        for (int i = 0; i < playerBlocks.size(); i += 2) {
            List<List<String>> rowBlocks = new ArrayList<>();
            rowBlocks.add(playerBlocks.get(i));
            if (i + 1 < playerBlocks.size()) {
                rowBlocks.add(playerBlocks.get(i + 1));
            }
            combinedRows.add(AnsiUtils.combineHorizontal(rowBlocks, 2));
        }
        
        for (List<String> row : combinedRows) {
            lines.addAll(row);
        }
        
        return lines;
    }

    private List<String> renderMenuBox(List<String> menu) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Menu", Colors.WHITE));
        lines.add(colorBorder("┌" + "─".repeat(MENU_CONTENT_WIDTH + 2) + "┐", Colors.WHITE));
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

    private List<String> renderBankVertical(Board board) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Bank", Colors.WHITE));
        int bw = 6; // fixed width for bank box
        lines.add(colorBorder("┌" + "─".repeat(bw + 2) + "┐", Colors.WHITE));
        
        for (final Gem gem : GEM_ORDER) {
            final int count = board.getGemCount(gem);
            String label = gemLabel(gem) + ":" + count;
            String colText = Colors.colorize(label, Colors.getGemColor(gem));
            lines.add(frameLine(colText, Colors.WHITE, bw));
        }

        lines.add(colorBorder("└" + "─".repeat(bw + 2) + "┘", Colors.WHITE));
        return lines;
    }
    public String formatCardAscii(Card card) {
        return CardRenderer.formatCardAscii(card);
    }

    public String formatCardAscii(Card card, boolean affordable) {
        return CardRenderer.formatCardAscii(card, affordable);
    }

    public void displayPlayerTokens(final Player player) {
        player.getTokens().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .forEach(entry -> System.out.printf("%s: %d | ",
                        Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())),
                        entry.getValue()));
        System.out.println();
    }

    public void setMenuLines(final List<String> menuLines) {
        this.menuLines = menuLines == null ? List.of() : new ArrayList<>(menuLines);
    }

    public List<String> buildMenuLines(final List<MenuOption> options) {
        final List<String> lines = new ArrayList<>();
        lines.add("Goal: 15 points");
        lines.add("Pick one action (or 'Z' to Undo)");
        for (final MenuOption option : options) {
            final String base = option.getNumber() + ") " + option.getLabel() + ": ";
            final String detail = option.getDetail();
            final String reason = option.isAvailable() || option.getReason().isBlank()
                    ? "" : " (" + option.getReason() + ")";
            if (option.isAvailable()) {
                lines.add(base + detail + reason);
            } else {
                lines.add(Colors.colorize(base + AnsiUtils.stripAnsi(detail) + reason, Colors.DIM));
            }
        }
        return lines;
    }

    private String formatGemCounts(final Map<Gem, Integer> counts, final boolean includeZero) {
        final List<String> parts = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (includeZero || count > 0) {
                parts.add(Colors.colorize(gemLabel(gem) + count, Colors.getGemColor(gem)));
            }
        }
        if (parts.isEmpty()) {
            return "None";
        }
        return String.join(" ", parts);
    }

    private List<String> formatRequirementsLines(final Map<Gem, Integer> requirements, final int contentWidth) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = requirements.getOrDefault(gem, 0);
            if (count > 0) {
                tokens.add(Colors.colorize(gemLabel(gem) + count, Colors.getGemColor(gem)));
            }
        }
        if (tokens.isEmpty()) {
            return List.of("None");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            if (AnsiUtils.stripAnsi(next).length() > Math.max(1, contentWidth - 5) && !current.isEmpty()) {
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

    private String gemLabel(final Gem gem) {
        return CardRenderer.gemLabel(gem);
    }

    private String frameLine(final String content, final String color, final int contentWidth) {
        final String padded = AnsiUtils.padRightAnsi(content, contentWidth);
        return Colors.colorize("│", color) + " " + padded + " " + Colors.colorize("│", color);
    }

    private String colorBorder(final String line, final String color) {
        return Colors.colorize(line, color);
    }

}
