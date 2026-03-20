package com.splendor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameFlowTest {

    @Test
    void turnAdvancementCyclesForTwoPlayers() {
        Game game = new Game(players(2), 15, 10);

        assertEquals("P1", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P2", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P1", game.getCurrentPlayer().getName());
    }

    @Test
    void turnAdvancementCyclesForFourPlayers() {
        Game game = new Game(players(4), 15, 10);

        assertEquals("P1", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P2", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P3", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P4", game.getCurrentPlayer().getName());
        game.advanceToNextPlayer();
        assertEquals("P1", game.getCurrentPlayer().getName());
    }

    @Test
    void finalRoundTriggersWhenPlayerReachesFifteenPoints() {
        Game game = new Game(players(2), 15, 10);
        game.getPlayers().get(0).addPurchasedCard(card(9001, 1, 15, Gem.RED, Map.of()));

        assertFalse(game.isFinalRound());
        game.advanceToNextPlayer();

        assertTrue(game.isFinalRound());
        assertFalse(game.isGameFinished());
    }

    @Test
    void finalRoundCompletesAfterRemainingPlayersTakeOneTurnEach() {
        Game game = new Game(players(4), 15, 10);
        game.getPlayers().get(0).addPurchasedCard(card(9002, 1, 15, Gem.BLUE, Map.of()));

        game.advanceToNextPlayer();
        assertTrue(game.isFinalRound());
        assertFalse(game.isGameFinished());
        assertEquals("P2", game.getCurrentPlayer().getName());

        game.advanceToNextPlayer();
        assertEquals("P3", game.getCurrentPlayer().getName());
        assertFalse(game.isGameFinished());

        game.advanceToNextPlayer();
        assertEquals("P4", game.getCurrentPlayer().getName());
        assertFalse(game.isGameFinished());

        game.advanceToNextPlayer();
        assertTrue(game.isGameFinished());
    }

    @Test
    void winnerIsHighestScore() {
        Game game = new Game(players(2), 15, 10);
        game.getPlayers().get(0).addPurchasedCard(card(9003, 1, 16, Gem.WHITE, Map.of()));
        game.getPlayers().get(1).addPurchasedCard(card(9004, 1, 14, Gem.BLACK, Map.of()));

        game.advanceToNextPlayer();
        game.advanceToNextPlayer();

        assertTrue(game.isGameFinished());
        assertEquals("P1", game.getWinner().getName());
    }

    @Test
    void tieBreakerUsesFewestPurchasedCards() {
        Game game = new Game(players(2), 15, 10);

        game.getPlayers().get(0).addPurchasedCard(card(9100, 1, 7, Gem.RED, Map.of()));
        game.getPlayers().get(0).addPurchasedCard(card(9101, 1, 8, Gem.BLUE, Map.of()));
        game.getPlayers().get(1).addPurchasedCard(card(9102, 1, 15, Gem.GREEN, Map.of()));

        game.advanceToNextPlayer();
        game.advanceToNextPlayer();

        assertTrue(game.isGameFinished());
        assertEquals(15, game.getPlayers().get(0).getTotalPoints());
        assertEquals(15, game.getPlayers().get(1).getTotalPoints());
        assertEquals("P2", game.getWinner().getName());
    }

    @Test
    void gameIsNotFinishedWhileOngoing() {
        Game game = new Game(players(3), 15, 10);
        assertFalse(game.isGameFinished());
        assertFalse(game.isFinalRound());
    }

    @Test
    void advanceToNextPlayerDoesNothingWhenGameFinished() {
        Game game = new Game(players(2), 15, 10);
        game.getPlayers().get(0).addPurchasedCard(card(9200, 1, 15, Gem.RED, Map.of()));

        game.advanceToNextPlayer();
        game.advanceToNextPlayer();
        assertTrue(game.isGameFinished());

        String currentBefore = game.getCurrentPlayer().getName();
        Player winnerBefore = game.getWinner();
        game.advanceToNextPlayer();

        assertEquals(currentBefore, game.getCurrentPlayer().getName());
        assertSame(winnerBefore, game.getWinner());
        assertTrue(game.isGameFinished());
    }

    @Test
    void saveUndoStateAndUndoRestorePreviousState() {
        Game game = new Game(players(2), 15, 10);
        Player p1 = game.getPlayers().get(0);
        int initialBlueBank = game.getBoard().getGemCount(Gem.BLUE);

        game.saveUndoState();
        p1.addTokens(Gem.BLUE, 2);
        game.getBoard().removeGems(Map.of(Gem.BLUE, 1));
        game.addRecentMove("P1 took blue");
        game.advanceToNextPlayer();

        assertEquals("P2", game.getCurrentPlayer().getName());
        assertEquals(2, p1.getTokenCount(Gem.BLUE));
        assertEquals(initialBlueBank - 1, game.getBoard().getGemCount(Gem.BLUE));
        assertFalse(game.getRecentMoves().isEmpty());

        assertTrue(game.undo());
        assertEquals("P1", game.getCurrentPlayer().getName());
        assertEquals(0, p1.getTokenCount(Gem.BLUE));
        assertEquals(initialBlueBank, game.getBoard().getGemCount(Gem.BLUE));
        assertTrue(game.getRecentMoves().isEmpty());
    }

    @Test
    void tokenLimitIsConfiguredMaxTokensDefaultTen() {
        Game game = new Game(players(2), 15, 10);
        assertEquals(10, game.getMaxTokens());
    }

    @Test
    void recentMovesKeepsAtMostFiveInFifoOrder() {
        Game game = new Game(players(2), 15, 10);

        game.addRecentMove("M1");
        game.addRecentMove("M2");
        game.addRecentMove("M3");
        game.addRecentMove("M4");
        game.addRecentMove("M5");
        game.addRecentMove("M6");

        List<String> recent = game.getRecentMoves();
        assertEquals(5, recent.size());
        assertEquals(List.of("M2", "M3", "M4", "M5", "M6"), recent);
    }

    private static List<Player> players(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player("P" + i));
        }
        return players;
    }

    private static Card card(int id, int tier, int points, Gem bonusGem, Map<Gem, Integer> cost) {
        return new Card(id, tier, points, bonusGem, new HashMap<>(cost));
    }
}
