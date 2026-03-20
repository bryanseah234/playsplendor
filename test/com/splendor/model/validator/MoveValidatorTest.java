package com.splendor.model.validator;

import com.splendor.exception.InsufficientTokensException;
import com.splendor.exception.InvalidMoveException;
import com.splendor.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveValidatorTest {

    private static final int WINNING_POINTS = 15;
    private static final int MAX_TOKENS = 10;

    private MoveValidator validator;
    private Game game;
    private Player currentPlayer;

    @BeforeEach
    void setUp() {
        validator = new MoveValidator();
        game = buildTwoPlayerGameWithStandardBoard();
        currentPlayer = game.getCurrentPlayer();
    }

    @Test
    void validateTakeThreeDifferent_validThreeDifferentNonGoldAvailable() {
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 1, Gem.BLUE, 1, Gem.GREEN, 1));

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeThreeDifferent_invalidSelectingGoldGem() {
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 1, Gem.BLUE, 1, Gem.GOLD, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeThreeDifferent_invalidSelectingOnlyTwoGems() {
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 1, Gem.BLUE, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeThreeDifferent_invalidSelectingTwoOfSameType() {
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 2, Gem.BLUE, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeThreeDifferent_invalidGemNotAvailableInBank() {
        setBankGemCount(Gem.WHITE, 0);
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 1, Gem.BLUE, 1, Gem.GREEN, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeThreeDifferent_invalidMoreThanThreeTotalGems() {
        Move move = new Move(MoveType.TAKE_THREE_DIFFERENT, gems(Gem.WHITE, 2, Gem.BLUE, 1, Gem.GREEN, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeTwoSame_validGemWithAtLeastFourInBank() {
        Move move = new Move(MoveType.TAKE_TWO_SAME, gems(Gem.RED, 2));

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeTwoSame_invalidGemWithOnlyThreeInBank() {
        setBankGemCount(Gem.RED, 3);
        Move move = new Move(MoveType.TAKE_TWO_SAME, gems(Gem.RED, 2));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeTwoSame_invalidSelectingGold() {
        Move move = new Move(MoveType.TAKE_TWO_SAME, gems(Gem.GOLD, 2));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeTwoSame_invalidRequestingQuantityNotTwo() {
        Move move = new Move(MoveType.TAKE_TWO_SAME, gems(Gem.RED, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateTakeTwoSame_invalidSelectingTwoDifferentGemTypes() {
        Move move = new Move(MoveType.TAKE_TWO_SAME, gems(Gem.RED, 1, Gem.BLUE, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_validReserveVisibleCardWhenUnderLimit() {
        Card card = createCard(9001, 1, Gem.WHITE, gems(Gem.BLUE, 1));
        game.getBoard().addAvailableCard(1, card);
        Move move = new Move(MoveType.RESERVE_CARD, card.getId(), false);

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_validReserveFromDeck() {
        Move move = Move.reserveFromDeck(1);

        assertTrue(game.getBoard().getDeckSize(1) > 0, "Tier 1 deck should start non-empty");
        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_invalidPlayerAlreadyHasThreeReservedCards() {
        currentPlayer.addReservedCard(createCard(9101, 1, Gem.WHITE, gems(Gem.BLUE, 1)));
        currentPlayer.addReservedCard(createCard(9102, 1, Gem.WHITE, gems(Gem.BLUE, 1)));
        currentPlayer.addReservedCard(createCard(9103, 1, Gem.WHITE, gems(Gem.BLUE, 1)));
        Move move = Move.reserveFromDeck(1);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_invalidCardIdNotFoundOnBoard() {
        Move move = new Move(MoveType.RESERVE_CARD, 999999, false);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_invalidDeckTierEmpty() {
        int tier = 1;
        while (game.getBoard().getDeckSize(tier) > 0) {
            game.getBoard().drawBlindCard(tier);
        }
        Move move = Move.reserveFromDeck(tier);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_invalidDeckTierOutOfRangeZero() {
        Move move = Move.reserveFromDeck(0);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateReserveCard_invalidDeckTierOutOfRangeFour() {
        Move move = Move.reserveFromDeck(4);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_validExactTokensForCost() {
        Card card = createCard(9201, 1, Gem.BLUE, gems(Gem.WHITE, 2, Gem.RED, 1));
        game.getBoard().addAvailableCard(1, card);
        giveTokens(currentPlayer, gems(Gem.WHITE, 2, Gem.RED, 1));
        Move move = new Move(MoveType.BUY_CARD, card.getId(), false);

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_validUsingGoldForShortage() {
        Card card = createCard(9202, 1, Gem.GREEN, gems(Gem.WHITE, 2, Gem.RED, 1));
        game.getBoard().addAvailableCard(1, card);
        giveTokens(currentPlayer, gems(Gem.WHITE, 1, Gem.RED, 1, Gem.GOLD, 1));
        Move move = new Move(MoveType.BUY_CARD, card.getId(), false);

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_validWithDiscountsReducingCost() {
        Card discountCard = createCard(9203, 1, Gem.WHITE, gems());
        currentPlayer.addPurchasedCard(discountCard);

        Card target = createCard(9204, 1, Gem.RED, gems(Gem.WHITE, 1));
        game.getBoard().addAvailableCard(1, target);
        Move move = new Move(MoveType.BUY_CARD, target.getId(), false);

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_invalidCannotAffordCard() {
        Card card = createCard(9205, 1, Gem.BLACK, gems(Gem.BLUE, 3));
        game.getBoard().addAvailableCard(1, card);
        giveTokens(currentPlayer, gems(Gem.BLUE, 1));
        Move move = new Move(MoveType.BUY_CARD, card.getId(), false);

        assertThrows(InsufficientTokensException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_invalidCardIdNotFound() {
        Move move = new Move(MoveType.BUY_CARD, 888888, false);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_validBuyReservedCard() {
        Card reserved = createCard(9206, 1, Gem.GREEN, gems(Gem.BLACK, 2));
        currentPlayer.addReservedCard(reserved);
        giveTokens(currentPlayer, gems(Gem.BLACK, 2));
        Move move = new Move(MoveType.BUY_CARD, reserved.getId(), true);

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateBuyCard_invalidReservedCardIdNotInPlayerReservedList() {
        Card differentReserved = createCard(9207, 1, Gem.GREEN, gems(Gem.BLACK, 1));
        currentPlayer.addReservedCard(differentReserved);
        Move move = new Move(MoveType.BUY_CARD, 9208, true);

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateDiscardTokens_validDiscardExactExcessCount() {
        giveTokens(currentPlayer, gems(Gem.WHITE, 6, Gem.BLUE, 6));
        Move move = new Move(MoveType.DISCARD_TOKENS, gems(Gem.WHITE, 1, Gem.BLUE, 1));

        assertDoesNotThrow(() -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateDiscardTokens_invalidDiscardWrongNumberOfTokens() {
        giveTokens(currentPlayer, gems(Gem.WHITE, 6, Gem.BLUE, 6));
        Move move = new Move(MoveType.DISCARD_TOKENS, gems(Gem.WHITE, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateDiscardTokens_invalidPlayerDoesNotExceedTokenLimit() {
        giveTokens(currentPlayer, gems(Gem.WHITE, 5, Gem.BLUE, 5));
        Move move = new Move(MoveType.DISCARD_TOKENS, gems(Gem.WHITE, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void validateDiscardTokens_invalidDiscardMoreThanPlayerHas() {
        giveTokens(currentPlayer, gems(Gem.BLUE, 11));
        Move move = new Move(MoveType.DISCARD_TOKENS, gems(Gem.RED, 1));

        assertThrows(InvalidMoveException.class, () -> validator.validateMove(move, currentPlayer, game));
    }

    @Test
    void canPlayerAffordCard_discountsReduceCostToZero() {
        currentPlayer.addPurchasedCard(createCard(9301, 1, Gem.WHITE, gems()));
        currentPlayer.addPurchasedCard(createCard(9302, 1, Gem.WHITE, gems()));
        Card card = createCard(9303, 1, Gem.RED, gems(Gem.WHITE, 2));

        assertTrue(validator.canPlayerAffordCard(currentPlayer, card));
    }

    @Test
    void canPlayerAffordCard_goldCoversRemainingCost() {
        giveTokens(currentPlayer, gems(Gem.BLUE, 1, Gem.GOLD, 2));
        Card card = createCard(9304, 1, Gem.RED, gems(Gem.BLUE, 2, Gem.WHITE, 1));

        assertTrue(validator.canPlayerAffordCard(currentPlayer, card));
    }

    @Test
    void canPlayerAffordCard_combinationOfRegularDiscountsAndGold() {
        currentPlayer.addPurchasedCard(createCard(9305, 1, Gem.WHITE, gems()));
        giveTokens(currentPlayer, gems(Gem.WHITE, 1, Gem.BLUE, 1, Gem.GOLD, 1));
        Card card = createCard(9306, 1, Gem.RED, gems(Gem.WHITE, 3, Gem.BLUE, 1));

        assertTrue(validator.canPlayerAffordCard(currentPlayer, card));
    }

    @Test
    void canPlayerAffordCard_failureWhenEvenGoldNotEnough() {
        giveTokens(currentPlayer, gems(Gem.WHITE, 1, Gem.GOLD, 1));
        Card card = createCard(9307, 1, Gem.RED, gems(Gem.WHITE, 3, Gem.BLUE, 1));

        assertFalse(validator.canPlayerAffordCard(currentPlayer, card));
    }

    private Game buildTwoPlayerGameWithStandardBoard() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice"));
        players.add(new Player("Bob"));
        return new Game(players, WINNING_POINTS, MAX_TOKENS);
    }

    private void giveTokens(Player player, Map<Gem, Integer> tokens) {
        for (Map.Entry<Gem, Integer> entry : tokens.entrySet()) {
            player.addTokens(entry.getKey(), entry.getValue());
        }
    }

    private void setBankGemCount(Gem gem, int targetCount) {
        Board board = game.getBoard();
        int current = board.getGemCount(gem);
        if (current > targetCount) {
            board.removeGems(gems(gem, current - targetCount));
        } else if (targetCount > current) {
            board.addGems(gems(gem, targetCount - current));
        }
    }

    private Card createCard(int id, int tier, Gem bonusGem, Map<Gem, Integer> cost) {
        return new Card(id, tier, 0, bonusGem, cost);
    }

    private Map<Gem, Integer> gems(Object... entries) {
        Map<Gem, Integer> map = new EnumMap<>(Gem.class);
        for (int i = 0; i < entries.length; i += 2) {
            Gem gem = (Gem) entries[i];
            Integer qty = (Integer) entries[i + 1];
            map.put(gem, qty);
        }
        return map;
    }
}
