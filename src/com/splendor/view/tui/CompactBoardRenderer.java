package com.splendor.view.tui;

import com.splendor.model.Board;
import com.splendor.model.Card;
import com.splendor.model.Game;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import com.splendor.model.Player;
import com.splendor.model.validator.MoveValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class CompactBoardRenderer {
    private final MoveValidator moveValidator = new MoveValidator();
    private int maxVerticalOffset;

    String render(final Game game, final int terminalWidth, final int viewportHeight, final int verticalOffset) {
        if (game == null) {
            maxVerticalOffset = 0;
            return "No game state available.";
        }

        final int width = Math.max(60, terminalWidth);
        final int inner = Math.max(40, width - 4);
        final List<String> all = new ArrayList<>();

        all.addAll(renderCardsBox(game, inner));
        all.add("");
        all.addAll(renderBankAndNoblesBox(game, inner));
        all.add("");
        all.addAll(renderPlayersBox(game, inner));

        final int availableHeight = Math.max(8, viewportHeight);
        maxVerticalOffset = Math.max(0, all.size() - availableHeight);
        final int offset = Math.max(0, Math.min(verticalOffset, maxVerticalOffset));
        final int end = Math.min(all.size(), offset + availableHeight);

        final List<String> visible = new ArrayList<>(all.subList(offset, end));
        while (visible.size() < availableHeight) {
            visible.add("");
        }
        if (!visible.isEmpty()) {
            final String indicator = "Board " + (offset + 1) + "-" + end + "/" + all.size();
            visible.set(visible.size() - 1, pad(indicator, width));
        }
        return String.join("\n", visible);
    }

    int maxVerticalOffset() {
        return maxVerticalOffset;
    }

    private List<String> renderCardsBox(final Game game, final int inner) {
        final List<String> lines = new ArrayList<>();
        final Player current = game.getCurrentPlayer();
        final Board board = game.getBoard();

        lines.addAll(boxHeader("Board", inner));
        for (int tier = 3; tier >= 1; tier--) {
            lines.add(frameLine("Tier " + tier + " (deck " + board.getDeckSize(tier) + ")", inner));
            final List<Card> cards = board.getAvailableCards(tier);
            if (cards.isEmpty()) {
                lines.add(frameLine("  (no visible cards)", inner));
                continue;
            }
            for (final Card card : cards) {
                final boolean affordable = moveValidator.canPlayerAffordCard(current, card);
                final String marker = affordable ? "✓" : "·";
                final String cardText = "  [ID:" + card.getId()
                        + " Pts:" + card.getPoints()
                        + " Bonus:" + shortGem(card.getBonusGem())
                        + " Cost:" + formatGemMap(card.getCost())
                        + "] ";
                appendWrappedWithSuffix(lines, cardText, marker, inner);
            }
        }
        lines.addAll(boxFooter(inner));
        return lines;
    }

    private List<String> renderBankAndNoblesBox(final Game game, final int inner) {
        final List<String> lines = new ArrayList<>();
        final Board board = game.getBoard();
        lines.addAll(boxHeader("Bank & Nobles", inner));

        final String bank = "Bank: W:" + board.getGemCount(Gem.WHITE)
                + " B:" + board.getGemCount(Gem.BLUE)
                + " G:" + board.getGemCount(Gem.GREEN)
                + " R:" + board.getGemCount(Gem.RED)
                + " K:" + board.getGemCount(Gem.BLACK)
                + " Au:" + board.getGemCount(Gem.GOLD);
        appendWrapped(lines, bank, inner);

        if (board.getAvailableNobles().isEmpty()) {
            lines.add(frameLine("Nobles: (none)", inner));
        } else {
            lines.add(frameLine("Nobles:", inner));
            for (final Noble noble : board.getAvailableNobles()) {
                final String nobleLine = "  Noble N" + noble.getId()
                        + " (" + noble.getPoints() + "pts) needs: "
                        + formatGemMap(noble.getRequirements());
                appendWrapped(lines, nobleLine, inner);
            }
        }

        lines.addAll(boxFooter(inner));
        return lines;
    }

    private List<String> renderPlayersBox(final Game game, final int inner) {
        final List<String> lines = new ArrayList<>();
        lines.addAll(boxHeader("Players", inner));

        final Player current = game.getCurrentPlayer();
        for (final Player player : game.getPlayers()) {
            final boolean isCurrent = player == current;
            final String prefix = isCurrent ? "→ " : "  ";
            final String line = prefix + player.getName()
                    + " (" + player.getTotalPoints() + "pts)"
                    + " Tokens:"
                    + formatPlayerTokens(player)
                    + " (" + player.getTotalTokenCount() + "/" + game.getMaxTokens() + ")"
                    + " Bonus:"
                    + formatGemMap(player.getGemDiscounts());
            appendWrapped(lines, line, inner);
        }

        lines.addAll(boxFooter(inner));
        return lines;
    }

    private List<String> boxHeader(final String title, final int inner) {
        final List<String> out = new ArrayList<>();
        out.add("┌" + repeat("─", inner + 2) + "┐");
        out.add("│ " + pad(title, inner) + " │");
        out.add("├" + repeat("─", inner + 2) + "┤");
        return out;
    }

    private List<String> boxFooter(final int inner) {
        return List.of("└" + repeat("─", inner + 2) + "┘");
    }

    private String frameLine(final String text, final int width) {
        return "│ " + pad(text, width) + " │";
    }

    private void appendWrapped(final List<String> lines, final String text, final int width) {
        final List<String> wrapped = wrap(text, width);
        for (final String w : wrapped) {
            lines.add(frameLine(w, width));
        }
    }

    private void appendWrappedWithSuffix(final List<String> lines, final String text, final String suffix, final int width) {
        final List<String> wrapped = wrap(text, width - 2);
        for (int i = 0; i < wrapped.size(); i++) {
            final String w = wrapped.get(i);
            if (i == wrapped.size() - 1) {
                lines.add("│ " + pad(w, width - 2) + suffix + " │");
            } else {
                lines.add(frameLine(w, width));
            }
        }
    }

    private List<String> wrap(final String text, final int width) {
        final List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            out.add("");
            return out;
        }
        final String[] words = text.split("\\s+");
        String line = "";
        for (final String word : words) {
            if (word.length() > width) {
                if (!line.isEmpty()) {
                    out.add(line);
                    line = "";
                }
                int start = 0;
                while (start < word.length()) {
                    final int end = Math.min(word.length(), start + width);
                    out.add(word.substring(start, end));
                    start = end;
                }
                continue;
            }
            final String next = line.isEmpty() ? word : line + " " + word;
            if (next.length() <= width) {
                line = next;
            } else {
                out.add(line);
                line = word;
            }
        }
        if (!line.isEmpty()) {
            out.add(line);
        }
        return out.isEmpty() ? List.of("") : out;
    }

    private String pad(final String text, final int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + repeat(" ", width - text.length());
    }

    private String repeat(final String s, final int count) {
        return String.valueOf(s).repeat(Math.max(0, count));
    }

    private String formatPlayerTokens(final Player player) {
        return "W" + player.getTokenCount(Gem.WHITE)
                + " B" + player.getTokenCount(Gem.BLUE)
                + " G" + player.getTokenCount(Gem.GREEN)
                + " R" + player.getTokenCount(Gem.RED)
                + " K" + player.getTokenCount(Gem.BLACK)
                + " Au" + player.getTokenCount(Gem.GOLD);
    }

    private String formatGemMap(final Map<Gem, Integer> values) {
        final StringBuilder sb = new StringBuilder();
        appendGem(sb, values, Gem.WHITE, "W");
        appendGem(sb, values, Gem.BLUE, "B");
        appendGem(sb, values, Gem.GREEN, "G");
        appendGem(sb, values, Gem.RED, "R");
        appendGem(sb, values, Gem.BLACK, "K");
        if (values.getOrDefault(Gem.GOLD, 0) > 0) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append("Au").append(values.get(Gem.GOLD));
        }
        return sb.isEmpty() ? "-" : sb.toString();
    }

    private void appendGem(final StringBuilder sb, final Map<Gem, Integer> values, final Gem gem, final String shortName) {
        final int amount = values.getOrDefault(gem, 0);
        if (amount <= 0) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(shortName).append(amount);
    }

    private String shortGem(final Gem gem) {
        if (gem == null) {
            return "-";
        }
        return switch (gem) {
            case WHITE -> "W";
            case BLUE -> "B";
            case GREEN -> "G";
            case RED -> "R";
            case BLACK -> "K";
            case GOLD -> "Au";
        };
    }
}
