package com.splendor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.splendor.config.IConfigProvider;
import com.splendor.model.validator.MoveValidator;
import com.splendor.test.TestConfigProvider;
import java.util.List;

class BotStrategyTest {

    private Game game;
    private MoveValidator validator;
    private Player bot;

    @BeforeEach
    void setUp() {
        IConfigProvider configProvider = new TestConfigProvider();
        game = new Game(List.of(new ComputerPlayer("Bot")), 15, 10, configProvider);
        validator = new MoveValidator(configProvider);
        bot = game.getPlayers().get(0);
    }

    @Test
    void testBotSkipsEmptyTiersWhenReserving() {
        Board board = game.getBoard();
        
        // Empty all decks
        for (int tier = 1; tier <= 3; tier++) {
            while (board.getDeckSize(tier) > 0) {
                board.drawBlindCard(tier);
            }
        }
        
        // Make sure taking gems is not an option
        Map<Gem, Integer> allGems = new HashMap<>(board.getGemBank());
        board.removeGems(allGems);
        
        // Try to get bot move when everything is empty
        Move move = BotStrategy.chooseBotMove(bot, game, validator);
        
        // The fallback is an empty take 3 different
        assertEquals(MoveType.TAKE_THREE_DIFFERENT, move.getMoveType());
        assertTrue(move.getSelectedGems().isEmpty());
    }

    @Test
    void testBotReservesFromNonEmptyTier() {
        Board board = game.getBoard();
        
        // Empty tiers 1 and 2
        while (board.getDeckSize(1) > 0) {
            board.drawBlindCard(1);
        }
        while (board.getDeckSize(2) > 0) {
            board.drawBlindCard(2);
        }
        
        // Make sure taking gems is not an option
        Map<Gem, Integer> allGems = new HashMap<>(board.getGemBank());
        board.removeGems(allGems);
        
        // Bot should skip 1 and 2 and reserve from 3
        Move move = BotStrategy.chooseBotMove(bot, game, validator);
        
        assertEquals(MoveType.RESERVE_CARD, move.getMoveType());
        assertTrue(move.hasDeckSelection());
        assertEquals(3, move.getDeckTier());
    }
}
