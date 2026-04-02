package com.splendor.controller;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.view.Colors;
import java.util.*;

/**
 * Stateless factory that constructs the ordered list of MenuOption objects
 * presented to a player at the start of their turn.
 *
 * MenuBuilder centralises all "can the player take this action right now?"
 * logic, keeping it out of both the view (which only renders) and the model
 * (which only stores state). Each option carries a pre-computed availability
 * flag and a human-readable reason string so the view can display greyed-out
 * entries with an explanation without re-querying the model itself.
 *
 * The returned list is always in the same fixed order:
 *   1) Take 3 different gems
 *   2) Take 2 same gems
 *   3) Reserve visible card
 *   4) Reserve from deck
 *   5) Buy visible card
 *   6) Buy reserved card
 *   7) Exit game
 */
public final class MenuBuilder {

    /** Prevent instantiation of this utility class. */
    private MenuBuilder() {}

    /**
     * Builds the complete ordered list of MenuOptions for the given player's turn.
     *
     * Each option's availability is determined by querying the current board and
     * player state. Computer players are never offered the Exit Game option.
     *
     * @param player        The player whose turn it is.
     * @param game          The current game state, used to inspect the board and rules.
     * @param moveValidator Validator used to check card affordability.
     * @return An ordered, non-null list of MenuOption objects ready for the view to render.
     */
    public static List<MenuOption> buildMenuOptions(final Player player, final Game game,
            final MoveValidator moveValidator) {
        final Board board = game.getBoard();
        final List<MenuOption> options = new ArrayList<>();
        int index = 1; // 1-based numbering matches what the player types

        // --- Option 1: Take 3 different gems ---
        final List<Gem> threeDifferent = getAvailableDifferentGems(board); // gems with at least 1 token
        final boolean canTakeThree = threeDifferent.size() >= 3;           // need at least 3 colours available
        options.add(new MenuOption(index++, MenuAction.TAKE_THREE, canTakeThree,
                "Take 3 different", formatColoredGemList(threeDifferent),
                canTakeThree ? "" : "Need 3 colors in bank"));

        // --- Option 2: Take 2 same gems ---
        final List<Gem> twoSame = getAvailableTwoSameGems(board); // gems with >= 4 tokens in bank
        final boolean canTakeTwo = !twoSame.isEmpty();
        options.add(new MenuOption(index++, MenuAction.TAKE_TWO, canTakeTwo,
                "Take 2 same", formatColoredGemList(twoSame), canTakeTwo ? "" : "No color with 4+ tokens"));

        // --- Options 3 & 4: Reserve (visible or from deck) ---
        final boolean canReserve = player.canReserveCard(moveValidator.getMaxReservedCards());
        final boolean canReserveVisible = canReserve && hasVisibleCards(board);
        final boolean canReserveDeck = canReserve && hasAnyDeckCards(board);
        final List<Integer> visibleIds = getVisibleCardIds(board);
        options.add(new MenuOption(index++, MenuAction.RESERVE_VISIBLE, canReserveVisible,
                "Reserve visible card", canReserveVisible ? formatIdList(visibleIds, 8) : "None",
                canReserveVisible ? "" : reserveVisibleReason(player, board, moveValidator)));

        // Build the tier list for deck reserves (e.g. "1/2/3").
        final List<Integer> availableTiers = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) {
                availableTiers.add(tier);
            }
        }

        // Format tier list as slash-separated (e.g. "1/2/3") for the detail column.
        final String deckInfo;
        if (availableTiers.isEmpty()) {
            deckInfo = "None";
        } else {
            final String commaSeparated = formatIdList(availableTiers, 3);
            deckInfo = commaSeparated.replace(", ", "/");
        }

        // Determine availability and reason separately for clarity.
        final boolean canReserveDeckAndHasCards = canReserveDeck && !availableTiers.isEmpty();
        final String reserveDeckUnavailableReason;
        if (!canReserveDeck) {
            reserveDeckUnavailableReason = reserveDeckReason(player, board, moveValidator);
        } else if (availableTiers.isEmpty()) {
            reserveDeckUnavailableReason = "Decks are empty";
        } else {
            reserveDeckUnavailableReason = "";
        }
        options.add(new MenuOption(index++, MenuAction.RESERVE_DECK, canReserveDeckAndHasCards,
                "Reserve card from deck", deckInfo, reserveDeckUnavailableReason));

        // --- Options 5 & 6: Buy (visible or reserved) ---
        final List<Integer> affordableVisible = getAffordableVisibleIds(player, board, moveValidator);
        final boolean canBuyVisible = !affordableVisible.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_VISIBLE, canBuyVisible,
                "Buy visible card", canBuyVisible ? formatIdList(affordableVisible, 8) : "None",
                canBuyVisible ? "" : buyVisibleReason(player, board)));

        final List<Integer> reservedIds = getReservedCardIds(player);
        final List<Integer> affordableReserved = getAffordableReservedIds(player, moveValidator);
        final boolean canBuyReserved = !affordableReserved.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_RESERVED, canBuyReserved,
                "Buy reserved card(s)", reservedIds.isEmpty() ? "None" : formatIdList(reservedIds, 8),
                canBuyReserved ? "" : buyReservedReason(player)));

        // --- Option 7: Exit Game (human players only) ---
        options.add(new MenuOption(index++, MenuAction.EXIT_GAME, !(player instanceof ComputerPlayer),
                "Exit Game", "", ""));

        return options;
    }

    /**
     * Formats a list of gems as a space-separated, ANSI-coloured abbreviation string.
     * Returns "None" for an empty list.
     *
     * @param gems The gem types to format.
     * @return Coloured gem label string, e.g. "W B R", or "None".
     */
    public static String formatColoredGemList(final List<Gem> gems) {
        if (gems.isEmpty()) return "None";
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(Colors.colorize(gem.getShortCode(), Colors.getGemColor(gem)));
        }
        return joiner.toString();
    }

    /**
     * Formats up to maxCount IDs from a list as a comma-separated string,
     * appending "..." if the list has more entries than the limit.
     *
     * @param ids      List of integer IDs to format.
     * @param maxCount Maximum number of IDs to show before truncating.
     * @return Comma-separated string (e.g. "1, 5, 12") or "-" for an empty list.
     */
    public static String formatIdList(final List<Integer> ids, final int maxCount) {
        if (ids.isEmpty()) return "-";
        final StringJoiner joiner = new StringJoiner(", ");
        final int limit = Math.min(ids.size(), maxCount);
        for (int i = 0; i < limit; i++) joiner.add(String.valueOf(ids.get(i)));
        if (ids.size() > maxCount) joiner.add("...");
        return joiner.toString();
    }

    /**
     * Returns a list of all non-gold gem types that have at least one token
     * remaining in the bank. Used to populate the "Take 3 different" option.
     *
     * @param board The current game board.
     * @return List of gem types with a positive bank count.
     */
    public static List<Gem> getAvailableDifferentGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.displayOrderNoGold()) {
            if (board.getGemCount(gem) > 0) gems.add(gem);
        }
        return gems;
    }

    /**
     * Returns a list of non-gold gem types that have at least 4 tokens in the bank,
     * which is the minimum required to take 2 of the same colour.
     *
     * @param board The current game board.
     * @return List of gem types eligible for the "Take 2 same" action.
     */
    public static List<Gem> getAvailableTwoSameGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.displayOrderNoGold()) {
            if (board.getGemCount(gem) >= 4) gems.add(gem);
        }
        return gems;
    }

    /**
     * Returns true if any tier has at least one face-up card available on the board.
     *
     * @param board The current game board.
     * @return true if any visible card can be reserved.
     */
    public static boolean hasVisibleCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) return true;
        }
        return false;
    }

    /**
     * Returns true if any tier deck still has cards that can be drawn blind.
     *
     * @param board The current game board.
     * @return true if at least one deck-reserve is possible.
     */
    public static boolean hasAnyDeckCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) return true;
        }
        return false;
    }

    /**
     * Produces a human-readable explanation for why reserving a visible card is unavailable.
     *
     * @param player        The player attempting to reserve.
     * @param board         The current board.
     * @param moveValidator Validator used to read reserve-limit configuration.
     * @return Reason string shown in the greyed-out menu entry.
     */
    private static String reserveVisibleReason(final Player player, final Board board,
            final MoveValidator moveValidator) {
        if (!player.canReserveCard(moveValidator.getMaxReservedCards()))
            return "Reserve limit reached (" + moveValidator.getMaxReservedCards() + ")";
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Not available";
    }

    /**
     * Produces a human-readable explanation for why reserving from a deck is unavailable.
     *
     * @param player        The player attempting to reserve.
     * @param board         The current board.
     * @param moveValidator Validator used to read reserve-limit configuration.
     * @return Reason string shown in the greyed-out menu entry.
     */
    private static String reserveDeckReason(final Player player, final Board board,
            final MoveValidator moveValidator) {
        if (!player.canReserveCard(moveValidator.getMaxReservedCards()))
            return "Reserve limit reached (" + moveValidator.getMaxReservedCards() + ")";
        if (!hasAnyDeckCards(board)) return "Decks are empty";
        return "Not available";
    }

    /**
     * Produces a human-readable explanation for why buying a visible card is unavailable.
     *
     * @param player The player attempting to buy.
     * @param board  The current board.
     * @return Reason string shown in the greyed-out menu entry.
     */
    private static String buyVisibleReason(final Player player, final Board board) {
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Need more tokens";
    }

    /**
     * Produces a human-readable explanation for why buying a reserved card is unavailable.
     *
     * @param player The player attempting to buy.
     * @return Reason string shown in the greyed-out menu entry.
     */
    private static String buyReservedReason(final Player player) {
        if (player.getReservedCards().isEmpty()) return "No reserved cards";
        return "Need more tokens";
    }

    /**
     * Returns the IDs of all visible cards across all tiers that the given player
     * can currently afford (considering tokens and card-bonus discounts).
     *
     * @param player The player whose resources are checked.
     * @param board  The current game board.
     * @param moveValidator Validator used to check card affordability.
     * @return List of affordable visible card IDs.
     */
    public static List<Integer> getAffordableVisibleIds(final Player player, final Board board,
            final MoveValidator moveValidator) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
            }
        }
        return ids;
    }

    /**
     * Returns the IDs of all reserved cards in the player's hand that they can
     * currently afford.
     *
     * @param player        The player whose reserved cards are checked.
     * @param moveValidator Validator used to check card affordability.
     * @return List of affordable reserved card IDs.
     */
    public static List<Integer> getAffordableReservedIds(final Player player,
            final MoveValidator moveValidator) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
        }
        return ids;
    }

    /**
     * Returns the IDs of all face-up cards currently available on the board,
     * regardless of affordability. Used to populate the reserve-visible detail string.
     *
     * @param board The current game board.
     * @return List of all visible card IDs across all tiers.
     */
    public static List<Integer> getVisibleCardIds(final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) ids.add(card.getId());
        }
        return ids;
    }

    /**
     * Returns the IDs of all cards currently in the player's reserve hand.
     * Used to populate the buy-reserved detail string.
     *
     * @param player The player whose reserved cards are queried.
     * @return List of reserved card IDs.
     */
    public static List<Integer> getReservedCardIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) ids.add(card.getId());
        return ids;
    }
}
