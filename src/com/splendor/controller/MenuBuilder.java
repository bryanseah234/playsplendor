package com.splendor.controller;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.view.Colors;
import java.util.*;

public final class MenuBuilder {

    private static final MoveValidator moveValidator = new MoveValidator();

    private MenuBuilder() {}

    public static List<MenuOption> buildMenuOptions(final Player player, final Game game) {
        final Board board = game.getBoard();
        final List<MenuOption> options = new ArrayList<>();
        int index = 1;

        final List<Gem> threeDifferent = getAvailableDifferentGems(board);
        final boolean canTakeThree = threeDifferent.size() >= 3;
        options.add(new MenuOption(index++, MenuAction.TAKE_THREE, canTakeThree,
                "Take 3 different", formatColoredGemList(threeDifferent),
                canTakeThree ? "" : "Need 3 colors in bank"));

        final List<Gem> twoSame = getAvailableTwoSameGems(board);
        final boolean canTakeTwo = !twoSame.isEmpty();
        options.add(new MenuOption(index++, MenuAction.TAKE_TWO, canTakeTwo,
                "Take 2 same", formatColoredGemList(twoSame), canTakeTwo ? "" : "No color with 4+ tokens"));

        final boolean canReserve = player.canReserveCard();
        final boolean canReserveVisible = canReserve && hasVisibleCards(board);
        final boolean canReserveDeck = canReserve && hasAnyDeckCards(board);
        final List<Integer> visibleIds = getVisibleCardIds(board);
        options.add(new MenuOption(index++, MenuAction.RESERVE_VISIBLE, canReserveVisible,
                "Reserve visible card", canReserveVisible ? formatIdList(visibleIds, 8) : "None",
                canReserveVisible ? "" : reserveVisibleReason(player, board)));

        final List<Integer> availableTiers = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) availableTiers.add(tier);
        }
        final String deckInfo = availableTiers.isEmpty() ? "None" : formatIdList(availableTiers, 3).replace(", ", "/");
        options.add(new MenuOption(index++, MenuAction.RESERVE_DECK, canReserveDeck && !availableTiers.isEmpty(),
                "Reserve card from deck", deckInfo,
                canReserveDeck ? (availableTiers.isEmpty() ? "Decks are empty" : "") : reserveDeckReason(player, board)));

        final List<Integer> affordableVisible = getAffordableVisibleIds(player, board);
        final boolean canBuyVisible = !affordableVisible.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_VISIBLE, canBuyVisible,
                "Buy visible card", canBuyVisible ? formatIdList(affordableVisible, 8) : "None",
                canBuyVisible ? "" : buyVisibleReason(player, board)));

        final List<Integer> reservedIds = getReservedCardIds(player);
        final List<Integer> affordableReserved = getAffordableReservedIds(player);
        final boolean canBuyReserved = !affordableReserved.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_RESERVED, canBuyReserved,
                "Buy reserved card(s)", reservedIds.isEmpty() ? "None" : formatIdList(reservedIds, 8),
                canBuyReserved ? "" : buyReservedReason(player)));

        options.add(new MenuOption(index++, MenuAction.EXIT_GAME, !(player instanceof ComputerPlayer),
                "Exit Game", "-", ""));

        return options;
    }

    public static String formatColoredGemList(final List<Gem> gems) {
        if (gems.isEmpty()) return "None";
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)));
        }
        return joiner.toString();
    }

    public static String formatIdList(final List<Integer> ids, final int maxCount) {
        if (ids.isEmpty()) return "-";
        final StringJoiner joiner = new StringJoiner(", ");
        final int limit = Math.min(ids.size(), maxCount);
        for (int i = 0; i < limit; i++) joiner.add(String.valueOf(ids.get(i)));
        if (ids.size() > maxCount) joiner.add("...");
        return joiner.toString();
    }

    public static List<Gem> getAvailableDifferentGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) > 0) gems.add(gem);
        }
        return gems;
    }

    public static List<Gem> getAvailableTwoSameGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) >= 4) gems.add(gem);
        }
        return gems;
    }

    public static boolean hasVisibleCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) return true;
        }
        return false;
    }

    public static boolean hasAnyDeckCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) return true;
        }
        return false;
    }

    private static String reserveVisibleReason(final Player player, final Board board) {
        if (!player.canReserveCard()) return "Reserve limit reached (3)";
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Not available";
    }

    private static String reserveDeckReason(final Player player, final Board board) {
        if (!player.canReserveCard()) return "Reserve limit reached (3)";
        if (!hasAnyDeckCards(board)) return "Decks are empty";
        return "Not available";
    }

    private static String buyVisibleReason(final Player player, final Board board) {
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Need more tokens";
    }

    private static String buyReservedReason(final Player player) {
        if (player.getReservedCards().isEmpty()) return "No reserved cards";
        return "Need more tokens";
    }

    public static List<Integer> getAffordableVisibleIds(final Player player, final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
            }
        }
        return ids;
    }

    public static List<Integer> getAffordableReservedIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
        }
        return ids;
    }

    public static List<Integer> getVisibleCardIds(final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) ids.add(card.getId());
        }
        return ids;
    }

    public static List<Integer> getReservedCardIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) ids.add(card.getId());
        return ids;
    }

    private static String gemLabel(final Gem gem) {
        if (gem == Gem.WHITE) return "W";
        if (gem == Gem.BLUE) return "B";
        if (gem == Gem.GREEN) return "G";
        if (gem == Gem.RED) return "R";
        if (gem == Gem.BLACK) return "K";
        if (gem == Gem.GOLD) return "Au";
        return "";
    }
}
