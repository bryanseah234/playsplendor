package com.splendor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.splendor.exception.SplendorException;
import com.splendor.model.Card;
import com.splendor.model.Board;
import com.splendor.model.Game;
import com.splendor.model.Gem;
import com.splendor.model.MenuOption;
import com.splendor.model.Move;
import com.splendor.model.MoveType;
import com.splendor.model.Noble;
import com.splendor.model.Player;
import com.splendor.model.validator.MoveValidator;
import com.splendor.view.IGameView;
import org.junit.jupiter.api.Test;

class GameLogicTest {

    private final com.splendor.config.IConfigProvider configProvider = new com.splendor.test.TestConfigProvider();
    private final MoveValidator moveValidator = new MoveValidator(configProvider);

    @Test
    void takeThreeDifferentAvailableWhenThreeOrMoreColorsHaveTokens() {
        Board board = new Board(2, configProvider);
        setGemBank(board, gems(1, 1, 1, 0, 0, 5));

        assertTrue(canTakeThreeDifferent(board));
    }

    @Test
    void takeThreeDifferentUnavailableWhenOnlyTwoColorsHaveTokens() {
        Board board = new Board(2, configProvider);
        setGemBank(board, gems(2, 2, 0, 0, 0, 5));

        assertFalse(canTakeThreeDifferent(board));
    }

    @Test
    void takeTwoSameAvailableWhenAtLeastOneGemHasFourOrMore() {
        Board board = new Board(2, configProvider);
        setGemBank(board, gems(4, 0, 0, 0, 0, 5));

        assertTrue(canTakeTwoSame(board));
    }

    @Test
    void takeTwoSameUnavailableWhenAllGemsAreThreeOrFewer() {
        Board board = new Board(2, configProvider);
        setGemBank(board, gems(3, 3, 3, 3, 3, 5));

        assertFalse(canTakeTwoSame(board));
    }

    @Test
    void reserveVisibleAvailableWhenCanReserveAndVisibleCardsExist() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);

        assertTrue(canReserveVisible(player, game.getBoard()));
    }

    @Test
    void reserveVisibleUnavailableWhenPlayerHasThreeReservedCards() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);
        player.addReservedCard(card(7001, 1, 0, Gem.RED, Map.of()));
        player.addReservedCard(card(7002, 1, 0, Gem.BLUE, Map.of()));
        player.addReservedCard(card(7003, 1, 0, Gem.GREEN, Map.of()));

        assertFalse(canReserveVisible(player, game.getBoard()));
    }

    @Test
    void reserveVisibleUnavailableWhenNoVisibleCardsOnBoard() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);
        setAvailableCards(game.getBoard(), emptyAvailableCards());

        assertFalse(canReserveVisible(player, game.getBoard()));
    }

    @Test
    void reserveDeckUnavailableWhenAllDeckSizesAreZero() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);
        game.getBoard().restoreDecks(emptyDecks());

        assertFalse(canReserveDeck(player, game.getBoard()));
    }

    @Test
    void buyVisibleAvailableWhenPlayerCanAffordAtLeastOneVisibleCard() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);
        Card affordable = card(7101, 1, 0, Gem.WHITE, Map.of(Gem.RED, 1));
        setAvailableCards(game.getBoard(), availableCardsWithTierOne(affordable));
        player.addTokens(Gem.RED, 1);

        assertTrue(canBuyVisible(player, game.getBoard()));
    }

    @Test
    void buyVisibleUnavailableWhenPlayerHasNoTokensAndNoDiscounts() {
        Game game = game(2);
        Player player = game.getPlayers().get(0);
        Card card = card(7102, 1, 0, Gem.WHITE, Map.of(Gem.RED, 1));
        setAvailableCards(game.getBoard(), availableCardsWithTierOne(card));

        assertFalse(canBuyVisible(player, game.getBoard()));
    }

    @Test
    void buyReservedAvailableWhenPlayerCanAffordAtLeastOneReservedCard() {
        Player player = new Player("P1");
        Card reserved = card(7201, 1, 0, Gem.BLACK, Map.of(Gem.BLUE, 1));
        player.addReservedCard(reserved);
        player.addTokens(Gem.BLUE, 1);

        assertTrue(canBuyReserved(player));
    }

    @Test
    void buyReservedUnavailableWhenPlayerHasNoReservedCards() {
        Player player = new Player("P1");

        assertFalse(canBuyReserved(player));
    }

    @Test
    void afterTakingGemsBankIsUpdatedCorrectly() throws SplendorException {
        Game game = game(2);
        Player player = game.getCurrentPlayer();
        TurnController turnController = new TurnController(game, new StubView());
        int redBefore = game.getBoard().getGemCount(Gem.RED);
        int blueBefore = game.getBoard().getGemCount(Gem.BLUE);
        int greenBefore = game.getBoard().getGemCount(Gem.GREEN);

        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, Map.of(Gem.RED, 1, Gem.BLUE, 1, Gem.GREEN, 1));
        turnController.executeMove(move, player);

        assertEquals(redBefore - 1, game.getBoard().getGemCount(Gem.RED));
        assertEquals(blueBefore - 1, game.getBoard().getGemCount(Gem.BLUE));
        assertEquals(greenBefore - 1, game.getBoard().getGemCount(Gem.GREEN));
        assertEquals(1, player.getTokenCount(Gem.RED));
        assertEquals(1, player.getTokenCount(Gem.BLUE));
        assertEquals(1, player.getTokenCount(Gem.GREEN));
    }

    @Test
    void afterBuyingCardItIsRemovedFromVisibleAndReplacedFromDeck() throws SplendorException {
        Game game = game(2);
        Player player = game.getCurrentPlayer();
        TurnController turnController = new TurnController(game, new StubView());

        Card target = card(7301, 1, 0, Gem.WHITE, Map.of(Gem.RED, 1));
        Card replacement = card(7303, 1, 0, Gem.GREEN, Map.of());
        setAvailableCards(game.getBoard(), availableCardsWithTierOne(target));
        game.getBoard().restoreDecks(decksWithTierOne(replacement));
        int bankRedBefore = game.getBoard().getGemCount(Gem.RED);

        player.addTokens(Gem.RED, 1);
        turnController.executeMove(new Move(MoveType.BUY_CARD, target.getId(), false), player);

        assertFalse(game.getBoard().getAvailableCards(1).contains(target));
        assertTrue(game.getBoard().getAvailableCards(1).contains(replacement));
        assertTrue(player.getPurchasedCards().contains(target));
        assertEquals(bankRedBefore + 1, game.getBoard().getGemCount(Gem.RED));
    }

    @Test
    void afterReservingPlayerGetsGoldTokenIfAvailable() throws SplendorException {
        Game game = game(2);
        Player player = game.getCurrentPlayer();
        TurnController turnController = new TurnController(game, new StubView());

        Card target = card(7401, 1, 0, Gem.WHITE, Map.of(Gem.RED, 1));
        setAvailableCards(game.getBoard(), availableCardsWithTierOne(target));
        int goldBefore = game.getBoard().getGemCount(Gem.GOLD);

        turnController.executeMove(new Move(MoveType.RESERVE_CARD, target.getId(), false), player);

        assertTrue(player.getReservedCards().contains(target));
        assertEquals(1, player.getTokenCount(Gem.GOLD));
        assertEquals(goldBefore - 1, game.getBoard().getGemCount(Gem.GOLD));
    }

    @Test
    void nobleIsAwardedWhenDiscountRequirementsAreMet() throws SplendorException {
        Game game = game(2);
        Player player = game.getCurrentPlayer();
        PlayerController playerController = new PlayerController(game, new StubView(), moveValidator);
        Noble noble = game.getBoard().getAvailableNobles().get(0);
        int idSeed = 7502;
        for (Map.Entry<Gem, Integer> requirement : noble.getRequirements().entrySet()) {
            for (int i = 0; i < requirement.getValue(); i++) {
                player.addPurchasedCard(card(idSeed++, 1, 0, requirement.getKey(), Map.of()));
            }
        }

        playerController.checkNobleVisits(player);

        assertTrue(player.getNobles().contains(noble));
        assertFalse(game.getBoard().getAvailableNobles().contains(noble));
    }

    private boolean canTakeThreeDifferent(Board board) {
        return getAvailableDifferentGems(board).size() >= 3;
    }

    private boolean canTakeTwoSame(Board board) {
        return !getAvailableTwoSameGems(board).isEmpty();
    }

    private boolean canReserveVisible(Player player, Board board) {
        return player.canReserveCard(moveValidator.getMaxReservedCards()) && hasVisibleCards(board);
    }

    private boolean canReserveDeck(Player player, Board board) {
        return player.canReserveCard(moveValidator.getMaxReservedCards()) && hasAnyDeckCards(board);
    }

    private boolean canBuyVisible(Player player, Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            for (Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canBuyReserved(Player player) {
        for (Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) {
                return true;
            }
        }
        return false;
    }

    private List<Gem> getAvailableDifferentGems(Board board) {
        List<Gem> gems = new ArrayList<>();
        for (Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) > 0) {
                gems.add(gem);
            }
        }
        return gems;
    }

    private List<Gem> getAvailableTwoSameGems(Board board) {
        List<Gem> gems = new ArrayList<>();
        for (Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) >= 4) {
                gems.add(gem);
            }
        }
        return gems;
    }

    private boolean hasVisibleCards(Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyDeckCards(Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) {
                return true;
            }
        }
        return false;
    }

    private Game game(int playerCount) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            players.add(new Player("P" + i));
        }
        return new Game(players, 15, 10, configProvider);
    }

    private static void setGemBank(Board board, Map<Gem, Integer> targetCounts) {
        for (Gem gem : Gem.values()) {
            int current = board.getGemCount(gem);
            int target = targetCounts.getOrDefault(gem, 0);
            if (current > target) {
                board.removeGems(singleGem(gem, current - target));
            } else if (target > current) {
                board.addGems(singleGem(gem, target - current));
            }
        }
    }

    private static void setAvailableCards(Board board, Map<Integer, List<Card>> targetAvailable) {
        clearAvailableCards(board);
        Map<Integer, List<Card>> decks = emptyDecks();
        for (int tier = 1; tier <= 3; tier++) {
            List<Card> tierCards = targetAvailable.getOrDefault(tier, List.of());
            decks.put(tier, new ArrayList<>(tierCards));
        }
        board.restoreDecks(decks);
        for (int tier = 1; tier <= 3; tier++) {
            List<Card> tierCards = targetAvailable.getOrDefault(tier, List.of());
            for (int i = 0; i < tierCards.size(); i++) {
                board.drawCard(tier);
            }
        }
    }

    private static void clearAvailableCards(Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            List<Card> snapshot = new ArrayList<>(board.getAvailableCards(tier));
            for (Card card : snapshot) {
                board.removeAvailableCard(tier, card);
            }
        }
    }

    private static Map<Gem, Integer> singleGem(Gem gem, int count) {
        Map<Gem, Integer> map = new HashMap<>();
        map.put(gem, count);
        return map;
    }

    private static Card card(int id, int tier, int points, Gem bonusGem, Map<Gem, Integer> cost) {
        return new Card(id, tier, points, bonusGem, new HashMap<>(cost));
    }

    private static Map<Gem, Integer> gems(int red, int green, int blue, int white, int black, int gold) {
        Map<Gem, Integer> counts = new HashMap<>();
        counts.put(Gem.RED, red);
        counts.put(Gem.GREEN, green);
        counts.put(Gem.BLUE, blue);
        counts.put(Gem.WHITE, white);
        counts.put(Gem.BLACK, black);
        counts.put(Gem.GOLD, gold);
        return counts;
    }

    private static Map<Integer, List<Card>> emptyAvailableCards() {
        Map<Integer, List<Card>> available = new HashMap<>();
        available.put(1, new ArrayList<>());
        available.put(2, new ArrayList<>());
        available.put(3, new ArrayList<>());
        return available;
    }

    private static Map<Integer, List<Card>> availableCardsWithTierOne(Card tierOneCard) {
        Map<Integer, List<Card>> available = emptyAvailableCards();
        available.put(1, new ArrayList<>(List.of(tierOneCard)));
        return available;
    }

    private static Map<Integer, List<Card>> emptyDecks() {
        Map<Integer, List<Card>> decks = new HashMap<>();
        decks.put(1, new ArrayList<>());
        decks.put(2, new ArrayList<>());
        decks.put(3, new ArrayList<>());
        return decks;
    }

    private static Map<Integer, List<Card>> decksWithTierOne(Card... cards) {
        Map<Integer, List<Card>> decks = emptyDecks();
        decks.put(1, new ArrayList<>(java.util.Arrays.asList(cards)));
        return decks;
    }

    private static final class StubView implements IGameView {
        @Override public void displayGameState(Game game) { }
        @Override public void displayPlayerTurn(Player player) { }
        @Override public String displayMessage(String message) { return ""; }
        @Override public void displayNotification(String message) { }
        @Override public String displayError(String errorMessage) { return ""; }
        @Override public String promptForCommand(Player player, Game game) { return ""; }
        @Override public Move promptForMove(Player player, Game game, List<MenuOption> options) { return null; }
        @Override public Move promptForTokenDiscard(Player player, int excessCount) { return null; }
        @Override public void displayWinner(Player winner, Map<String, Integer> finalScores) { }
        @Override public void clearDisplay() { }
        @Override public void displayAvailableMoves(List<MenuOption> options, Game game) { }
        @Override public Noble promptForNobleChoice(Player player, List<Noble> nobles) { return nobles.get(0); }
        @Override public String promptForPlayerName(int playerNumber, int totalPlayers) { return "P" + playerNumber; }
        @Override public int promptForPlayerCount() { return 2; }
        @Override public void displayWelcomeMessage() { }
        @Override public String waitForEnter() { return ""; }
        @Override public void close() { }
    }
}
