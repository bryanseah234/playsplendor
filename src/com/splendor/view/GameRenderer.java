package com.splendor.view;

import com.splendor.config.IConfigProvider;
import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.util.AnsiUtils;
import java.util.*;

/**
 * Responsible for composing and printing the full terminal game screen.
 *
 * GameRenderer owns all layout constants and rendering helpers for the board,
 * noble tiles, player track boxes, gem bank, menu panel, and move-history log.
 * It deliberately contains no input logic; ConsoleView handles that separately.
 *
 * Layout overview (two-column terminal layout):
 *
 *   LEFT COLUMN                  RIGHT COLUMN
 *   ─────────────────────        ─────────────────────────
 *   Card tiers (3 → 1)           Noble tiles (horizontal)
 *   Menu box | Bank box          Recent move history
 *                                Player track boxes (2 × 2)
 *
 * All rendering is ANSI-colour aware; card boxes are dimmed for unaffordable
 * cards, and the active player's box is highlighted in gold.
 */
public class GameRenderer {

    /** Canonical gem display order used throughout all rendering methods. */
    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);

    /** Visible content width (excluding border characters) of the menu panel. */
    private static final int MENU_CONTENT_WIDTH = 60;

    /** Visible content width of the recent-moves history panel. */
    private static final int RECENT_MOVES_CONTENT_WIDTH = 96;

    /** Visible content width of each individual player info box. */
    private static final int PLAYER_BOX_WIDTH = 45;

    /** Visible content width of each noble tile box. */
    private static final int NOBLE_CARD_WIDTH = 16;

    /** Current menu lines injected by ConsoleView before each render call. */
    private List<String> menuLines = List.of();

    /** Used to determine which visible cards are affordable for the current player. */
    private final MoveValidator moveValidator;

    /**
     * Creates a new GameRenderer with a config-aware MoveValidator.
     */
    public GameRenderer(final IConfigProvider config) {
        this.moveValidator = new MoveValidator(config);
    }

    /**
     * Clears the terminal screen using the OS-appropriate method.
     * Falls back to the ANSI cursor-home + erase-screen sequence on failure.
     */
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

    /**

     * Clears the terminal and prints the full rendered game state.
     *
     * @param game The current game whose state should be displayed.
     */
    public void displayGameState(final Game game) {
        clearDisplay();
        System.out.println(renderToString(game));
    }

    /**
     * Returns the full game screen as a single multi-line string without printing it.
     * Used by network views that need to send the screen to a remote client.
     *
     * @param game The current game whose state should be rendered.
     * @return The fully rendered terminal frame as a string.
     */
    public String renderToString(final Game game) {
        return renderGameStateInternal(game.getBoard(), game.getPlayers(), game.getCurrentPlayer(),
                game.getRecentMoves(), game.getMaxTokens());
    }

    /**
     * Core rendering method that composes the full two-column layout.
     *
     * Left column:  card tiers (rendered highest-to-lowest) + menu + bank
     * Right column: noble tiles + move history + player track boxes
     *
     * Both columns are built independently as lists of strings and then stitched
     * side-by-side once so that ANSI padding math only runs at the final join step.
     *
     * @param board         The game board containing cards, nobles, and gem bank.
     * @param players       All players in turn order.
     * @param currentPlayer The active player (used for affordability and gold box border).
     * @param recentMoves   Up to 5 most recent move-history entries.
     * @param maxTokens     The per-player token limit (displayed in player boxes).
     * @return The complete rendered screen as a single string.
     */
    private String renderGameStateInternal(Board board, List<Player> players, Player currentPlayer,
            List<String> recentMoves, int maxTokens) {

        // Build LEFT column
        List<String> leftColumn = new ArrayList<>();
        List<List<String>> tiers = renderCardTiersList(board, currentPlayer);
        for (List<String> tier : tiers) {
            leftColumn.addAll(tier);
            leftColumn.add(""); // blank separator between tiers
        }
        List<String> leftMenu = renderMenuBox(menuLines);
        List<String> leftBank = renderBankVertical(board);
        leftColumn.addAll(combineSideBySide(leftMenu, leftBank)); // menu and bank share one row

        // Build RIGHT column
        List<String> rightColumn = new ArrayList<>();
        rightColumn.addAll(renderNoblesHorizontal(board));
        rightColumn.add("");
        rightColumn.addAll(renderRecentMovesBox(recentMoves));
        rightColumn.add("");
        rightColumn.addAll(renderPlayersTrackBoxes(players, currentPlayer, maxTokens));

        // Stitch both columns side-by-side once to produce the final frame.
        return combineSideBySideRaw(leftColumn, rightColumn);
    }

    /**
     * Joins two lists of lines side-by-side, padding the left column to a fixed
     * width so the right column always starts in the same terminal column.
     * Returns the result as a single newline-delimited string.
     *
     * @param left  Lines for the left column.
     * @param right Lines for the right column.
     * @return A single string representing both columns side-by-side.
     */
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
            leftLine = AnsiUtils.padRightAnsi(leftLine, leftWidth); // align right column start
            sb.append(leftLine).append("  ").append(rightLine).append("\n");
        }
        return sb.toString();
    }

    /**
     * Same as combineSideBySideRaw but returns a List of Strings instead of a
     * single joined string, for use when building the left column incrementally.
     *
     * @param left  Lines for the left portion.
     * @param right Lines for the right portion.
     * @return List of combined rows.
     */
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

    /**
     * Renders all three card tiers as separate line blocks (highest tier first so
     * the rarest, most expensive cards appear at the top of the screen).
     * Cards are displayed horizontally within each tier.
     * Unaffordable cards are rendered dimmed.
     *
     * @param board         The board whose available cards are rendered.
     * @param currentPlayer Used to determine affordability for each card.
     * @return A list-of-line-lists, one inner list per tier.
     */
    private List<List<String>> renderCardTiersList(final Board board, final Player currentPlayer) {
        final List<List<String>> tiers = new ArrayList<>();
        for (int tier = 3; tier >= 1; tier--) { // render tier 3 at top, tier 1 at bottom
            final List<String> lines = new ArrayList<>();
            final List<Card> cards = board.getAvailableCards().get(tier);
            if (cards == null || cards.isEmpty()) {
                lines.add(Colors.colorize("Level " + tier + ": 0 cards available in deck", Colors.WHITE));
                tiers.add(lines);
                continue;
            }
            // Header shows how many cards remain face-down in the deck.
            lines.add(Colors.colorize("Level " + tier + ": " + board.getDeckSize(tier)
                    + " cards available in deck", Colors.WHITE));

            // Build one row of side-by-side card ASCII boxes.
            final List<String> tierLines = new ArrayList<>();
            final int cardHeight = CardRenderer.formatCardAscii(cards.get(0)).split("\n").length;
            for (int i = 0; i < cardHeight; i++) {
                tierLines.add(""); // initialise with empty rows
            }
            for (final Card card : cards) {
                final boolean affordable = moveValidator.canPlayerAffordCard(currentPlayer, card);
                final String[] cardLines = CardRenderer.formatCardAscii(card, affordable).split("\n");
                for (int i = 0; i < cardLines.length; i++) {
                    tierLines.set(i, tierLines.get(i) + cardLines[i] + "  "); // append card to its row
                }
            }
            lines.addAll(tierLines);
            tiers.add(lines);
        }
        return tiers;
    }

    /**
     * Renders the available noble tiles as a horizontal row of bordered boxes.
     *
     * @param board The board whose noble tiles are rendered.
     * @return Lines forming the noble section header and tile boxes.
     */
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
            nLines.add(Colors.colorize("┌" + "─".repeat(NOBLE_CARD_WIDTH) + "┐", Colors.WHITE));
            nLines.add(Colors.colorize("│", Colors.WHITE)
                    + AnsiUtils.padRightAnsi("ID: N" + noble.getId(), NOBLE_CARD_WIDTH)
                    + Colors.colorize("│", Colors.WHITE));
            nLines.add(Colors.colorize("│", Colors.WHITE)
                    + AnsiUtils.padRightAnsi("Pts: " + noble.getPoints(), NOBLE_CARD_WIDTH)
                    + Colors.colorize("│", Colors.WHITE));
            nLines.add(Colors.colorize("│", Colors.WHITE)
                    + AnsiUtils.padRightAnsi("Needs:", NOBLE_CARD_WIDTH)
                    + Colors.colorize("│", Colors.WHITE));

            // Noble requirements span up to 2 lines to fit inside the tile width.
            List<String> reqs = formatRequirementsLines(noble.getRequirements(), NOBLE_CARD_WIDTH);
            for (int i = 0; i < 2; i++) {
                String r = i < reqs.size() ? reqs.get(i) : "";
                nLines.add(Colors.colorize("│", Colors.WHITE)
                        + AnsiUtils.padRightAnsi(r, NOBLE_CARD_WIDTH)
                        + Colors.colorize("│", Colors.WHITE));
            }
            // Blank padding line keeps noble tiles the same height as card boxes.
            nLines.add(Colors.colorize("│", Colors.WHITE)
                    + AnsiUtils.padRightAnsi("", NOBLE_CARD_WIDTH)
                    + Colors.colorize("│", Colors.WHITE));
            nLines.add(Colors.colorize("└" + "─".repeat(NOBLE_CARD_WIDTH) + "┘", Colors.WHITE));
            nobleBlocks.add(nLines);
        }
        lines.addAll(AnsiUtils.combineHorizontal(nobleBlocks, 2));
        return lines;
    }

    /**
     * Renders the last 5 moves (padded with "-" placeholders for early turns)
     * inside a labelled bordered box.
     *
     * @param moves Up to 5 recent move history strings.
     * @return Lines forming the move history panel.
     */
    private List<String> renderRecentMovesBox(List<String> moves) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Move History", Colors.WHITE));
        lines.add(colorBorder("┌" + "─".repeat(RECENT_MOVES_CONTENT_WIDTH + 2) + "┐", Colors.WHITE));

        // Always show exactly 5 rows; pad with "-" if fewer moves exist.
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

    /**
     * Renders all players' info boxes arranged in a 2-per-row grid.
     * The active player's box is highlighted in gold; all others use white borders.
     *
     * @param players       All players in the game.
     * @param currentPlayer The player whose turn it is (highlighted).
     * @param maxTokens     Token cap shown in the "Tokens (x/max)" line.
     * @return Lines forming the players section.
     */
    private List<String> renderPlayersTrackBoxes(List<Player> players, Player currentPlayer, int maxTokens) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Players (" + players.size() + "/4)", Colors.WHITE));

        List<List<String>> playerBlocks = new ArrayList<>();
        for (Player p : players) {
            List<String> pLines = new ArrayList<>();
            // Gold border for the active player; white border for everyone else.
            String borderCol = p == currentPlayer ? Colors.GOLD : Colors.WHITE;
            pLines.add(colorBorder("┌" + "─".repeat(PLAYER_BOX_WIDTH + 2) + "┐", borderCol));

            String nameScore = p.getName() + " - Score: " + p.getTotalPoints();
            if (p == currentPlayer) nameScore = Colors.colorize(nameScore, Colors.GOLD);

            pLines.add(frameLine(nameScore, borderCol, PLAYER_BOX_WIDTH));
            pLines.add(frameLine("Bonus: " + formatGemCounts(p.getGemDiscounts(), true), borderCol, PLAYER_BOX_WIDTH));
            pLines.add(frameLine("Tokens (" + p.getTotalTokenCount() + "/" + maxTokens + "): "
                    + formatGemCounts(p.getTokens(), true), borderCol, PLAYER_BOX_WIDTH));

            // Noble IDs earned by this player.
            String noblesStr = "None";
            if (!p.getNobles().isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                for (Noble n : p.getNobles()) sj.add(String.valueOf(n.getId()));
                noblesStr = sj.toString();
            }
            pLines.add(frameLine("Nobles Bought: " + noblesStr, borderCol, PLAYER_BOX_WIDTH));

            // IDs of purchased development cards.
            String cardsStr = "None";
            if (!p.getPurchasedCards().isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                for (Card c : p.getPurchasedCards()) sj.add(String.valueOf(c.getId()));
                cardsStr = sj.toString();
            }
            pLines.add(frameLine("Cards Bought: " + cardsStr, borderCol, PLAYER_BOX_WIDTH));

            // IDs of reserved (not yet purchased) cards.
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

        // Arrange player boxes 2-per-row using horizontal combining.
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

    /**
     * Renders the action menu panel using the lines last set via setMenuLines().
     * Shows "None" when no menu has been set (e.g. during bot turns).
     *
     * @param menu The list of pre-formatted menu line strings to display.
     * @return Lines forming the menu panel box.
     */
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

    /**
     * Renders the gem bank as a compact vertical box showing each gem's current count.
     * Displayed alongside the menu panel in the bottom-left area.
     *
     * @param board The board whose gem bank is rendered.
     * @return Lines forming the bank panel box.
     */
    private List<String> renderBankVertical(Board board) {
        List<String> lines = new ArrayList<>();
        lines.add(Colors.colorize("Bank", Colors.WHITE));
        int bw = 6; // fixed inner width for the bank box
        lines.add(colorBorder("┌" + "─".repeat(bw + 2) + "┐", Colors.WHITE));
        for (final Gem gem : GEM_ORDER) {
            final int count = board.getGemCount(gem);
            String label = gem.getShortCode() + ":" + count;
            String colText = Colors.colorize(label, Colors.getGemColor(gem));
            lines.add(frameLine(colText, Colors.WHITE, bw));
        }
        lines.add(colorBorder("└" + "─".repeat(bw + 2) + "┘", Colors.WHITE));
        return lines;
    }

    /**
     * Prints each of the player's tokens that has a non-zero count to stdout,
     * coloured in its gem colour. Used when prompting for token discards.
     *
     * @param player The player whose token inventory is displayed.
     */
    public void displayPlayerTokens(final Player player) {
        for (final Map.Entry<Gem, Integer> entry : player.getTokens().entrySet()) {
            final int tokenCount = entry.getValue();
            if (tokenCount > 0) {
                final Gem gem = entry.getKey();
                final String colorizedGem = Colors.colorize(gem.toString(), Colors.getGemColor(gem));
                System.out.printf("%s: %d | ", colorizedGem, tokenCount);
            }
        }
        System.out.println();
    }

    /**
     * Replaces the current menu line list used in the next render call.
     * Called by ConsoleView after building menu options but before re-rendering the screen.
     *
     * @param menuLines Pre-formatted menu strings, or null to clear the menu.
     */
    public void setMenuLines(final List<String> menuLines) {
        this.menuLines = menuLines == null ? List.of() : new ArrayList<>(menuLines);
    }

    /**
     * Converts a list of MenuOption objects into a list of display strings for the
     * menu panel. Unavailable options are rendered in dim colour with their reason.
     *
     * @param options       The menu options built by MenuBuilder for the current turn.
     * @param winningPoints Current game winning-point threshold from configuration.
     * @return List of formatted strings ready to be passed to setMenuLines().
     */
    public List<String> buildMenuLines(final List<MenuOption> options, final int winningPoints) {
        final List<String> lines = new ArrayList<>();
        lines.add("Goal: " + winningPoints + " points");
        lines.add("Pick one action (or 'Z' to Undo)");
        for (final MenuOption option : options) {
            final String detail = option.getDetail();
            final String base = option.getNumber() + ") " + option.getLabel()
                    + (detail.isBlank() ? "" : ": ");
            final String reason = option.isAvailable() || option.getReason().isBlank()
                    ? "" : " (" + option.getReason() + ")";
            if (option.isAvailable()) {
                lines.add(base + detail + reason);
            } else {
                // Dim the entire line for unavailable options.
                lines.add(Colors.colorize(base + AnsiUtils.stripAnsi(detail) + reason, Colors.DIM));
            }
        }
        return lines;
    }

    /**
     * Formats a gem-count map as a space-separated coloured abbreviation string.
     *
     * @param counts       Map from gem type to quantity.
     * @param includeZero  If true, gems with zero count are still shown (useful for
     *                     showing the full token row even when empty).
     * @return Coloured gem summary string (e.g. "W0 B2 G1 R0 K0 Au1") or "None".
     */
    private String formatGemCounts(final Map<Gem, Integer> counts, final boolean includeZero) {
        final List<String> parts = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (includeZero || count > 0) {
                parts.add(Colors.colorize(gem.getShortCode() + count, Colors.getGemColor(gem)));
            }
        }
        return parts.isEmpty() ? "None" : String.join(" ", parts);
    }

    /**
     * Formats noble requirements as a word-wrapped list of coloured token strings
     * that fit within contentWidth visible characters per line.
     *
     * @param requirements  The noble's gem requirements map.
     * @param contentWidth  Maximum visible width per line.
     * @return List of lines (typically 1–2) containing coloured requirement tokens.
     */
    private List<String> formatRequirementsLines(final Map<Gem, Integer> requirements, final int contentWidth) {
        final List<String> tokens = new ArrayList<>();
        for (final Gem gem : GEM_ORDER) {
            final int count = requirements.getOrDefault(gem, 0);
            if (count > 0) {
                tokens.add(Colors.colorize(gem.getShortCode() + count, Colors.getGemColor(gem)));
            }
        }
        if (tokens.isEmpty()) {
            return List.of("None");
        }
        final List<String> lines = new ArrayList<>();
        String current = "";
        for (final String token : tokens) {
            final String next = current.isEmpty() ? token : current + " " + token;
            // Leave a 5-char margin inside the tile box for the border and padding.
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


    /**
     * Builds a single bordered content line padded to exactly contentWidth visible chars.
     * The border characters are coloured with the provided colour code.
     *
     * @param content      The text to display inside the border.
     * @param color        ANSI colour code applied to the border pipes.
     * @param contentWidth Target visible width for the content area.
     * @return A fully formatted border line.
     */
    private String frameLine(final String content, final String color, final int contentWidth) {
        final String padded = AnsiUtils.padRightAnsi(content, contentWidth);
        return Colors.colorize("│", color) + " " + padded + " " + Colors.colorize("│", color);
    }

    /**
     * Wraps a border string (top/bottom rule) in the given ANSI colour code.
     *
     * @param line  The border string to colour (e.g. "┌──────┐").
     * @param color ANSI colour code to apply.
     * @return The coloured border string.
     */
    private String colorBorder(final String line, final String color) {
        return Colors.colorize(line, color);
    }
}
