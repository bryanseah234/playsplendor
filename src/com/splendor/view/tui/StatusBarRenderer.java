package com.splendor.view.tui;

import com.splendor.model.Board;
import com.splendor.model.Game;
import com.splendor.model.Gem;
import com.splendor.model.Player;
final class StatusBarRenderer {
    String render(final Game game, final int width) {
        if (game == null) {
            return fit("SPLENDOR | Turn: - | Score: - | Bank: W0 B0 G0 R0 K0 Au0", width);
        }

        final Player currentPlayer = game.getCurrentPlayer();
        final Board board = game.getBoard();
        final String status = "SPLENDOR | Turn: " + currentPlayer.getName()
                + " | Score: " + currentPlayer.getTotalPoints()
                + " | Bank: " + bankSummary(board);
        return fit(status, width);
    }

    private String fit(final String text, final int width) {
        final int w = Math.max(20, width);
        if (text.length() >= w) {
            return text.substring(0, Math.max(1, w - 1)) + "…";
        }
        return text + " ".repeat(w - text.length());
    }

    private String bankSummary(final Board board) {
        return "W" + board.getGemCount(Gem.WHITE)
                + " B" + board.getGemCount(Gem.BLUE)
                + " G" + board.getGemCount(Gem.GREEN)
                + " R" + board.getGemCount(Gem.RED)
                + " K" + board.getGemCount(Gem.BLACK)
                + " Au" + board.getGemCount(Gem.GOLD);
    }
}
