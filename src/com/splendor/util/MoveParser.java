package com.splendor.util;

import com.splendor.model.Gem;
import com.splendor.model.Move;
import com.splendor.model.MoveType;
import java.util.HashMap;
import java.util.Map;

public final class MoveParser {
    private MoveParser() {
    }

    public static Move parseMoveFromResponse(final String response) {
        if (response == null) {
            return createDefaultMove();
        }
        final String[] parts = response.split(":");
        if (parts.length < 3 || !parts[0].equalsIgnoreCase("MOVE")) {
            return createDefaultMove();
        }
        switch (parts[1].toUpperCase()) {
            case "TAKE_3":
                return parseTakeThreeMove(parts[2]);
            case "TAKE_2":
                return parseTakeTwoMove(parts[2]);
            case "BUY":
                return parseBuyMove(parts[2]);
            case "RESERVE":
                return parseReserveMove(parts[2]);
            default:
                return createDefaultMove();
        }
    }

    public static Move parseTakeThreeMove(final String gemCodes) {
        final Map<Gem, Integer> gems = new HashMap<>();
        for (final char c : gemCodes.toUpperCase().toCharArray()) {
            final Gem gem = GemParser.parseGemCode(c);
            if (gem == null) {
                return createDefaultMove();
            }
            gems.merge(gem, 1, Integer::sum);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
    }

    public static Move parseTakeTwoMove(final String gemCode) {
        if (gemCode.length() != 1) {
            return createDefaultMove();
        }
        final Gem gem = GemParser.parseGemCode(gemCode.toUpperCase().charAt(0));
        if (gem == null) {
            return createDefaultMove();
        }
        final Map<Gem, Integer> gems = new HashMap<>();
        gems.put(gem, 2);
        return new Move(MoveType.TAKE_TWO_SAME, gems);
    }

    public static Move parseBuyMove(final String param) {
        final boolean isReserved = param.length() > 1
                && param.toUpperCase().charAt(0) == 'R'
                && Character.isDigit(param.charAt(1));
        try {
            final int cardId = Integer.parseInt(isReserved ? param.substring(1) : param);
            return new Move(MoveType.BUY_CARD, cardId, isReserved);
        } catch (final NumberFormatException e) {
            return createDefaultMove();
        }
    }

    public static Move parseReserveMove(final String param) {
        if (param.length() > 1 && param.toUpperCase().charAt(0) == 'D') {
            try {
                return Move.reserveFromDeck(Integer.parseInt(param.substring(1)));
            } catch (final NumberFormatException e) {
                return createDefaultMove();
            }
        }
        try {
            return new Move(MoveType.RESERVE_CARD, Integer.parseInt(param), false);
        } catch (final NumberFormatException e) {
            return createDefaultMove();
        }
    }

    public static Move parseDiscardMoveFromResponse(final String response) {
        if (response != null) {
            final String[] parts = response.split(":");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase("DISCARD")) {
                return parseTakeThreeMove(parts[1]);
            }
        }
        return new Move(MoveType.DISCARD_TOKENS, new HashMap<>());
    }

    public static Move createDefaultMove() {
        return new Move(MoveType.TAKE_THREE_DIFFERENT);
    }
}
