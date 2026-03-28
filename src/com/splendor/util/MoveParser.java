package com.splendor.util;

import com.splendor.model.Gem;
import com.splendor.model.Move;
import com.splendor.model.MoveType;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that converts raw network protocol strings into Move objects.
 *
 * Remote clients (connected via TCP) send moves as colon-delimited text commands
 * following the pattern defined in NetworkProtocol. This class is the single
 * translation layer between those strings and the Move domain objects consumed
 * by the rest of the game engine.
 *
 * Expected formats:
 *   MOVE:TAKE_3:RGB      -- take 3 different gems (one char per gem code)
 *   MOVE:TAKE_2:R        -- take 2 red gems
 *   MOVE:BUY:42          -- buy visible card with ID 42
 *   MOVE:BUY:R42         -- buy reserved card with ID 42 (R prefix signals reserved)
 *   MOVE:RESERVE:D2      -- reserve blind from tier-2 deck (D prefix signals deck)
 *   MOVE:RESERVE:42      -- reserve visible card with ID 42
 *   DISCARD:RGB          -- discard 3 tokens (reuses parseTakeThreeMove for gem codes)
 *
 * Any unrecognised or malformed input falls back to createDefaultMove().
 */
public final class MoveParser {

    /** Prevent instantiation of this utility class. */
    private MoveParser() {
    }

    /**
     * Parses a full network response string (e.g. "MOVE:TAKE_3:RGB") into a Move.
     * Returns a safe default move if the response is null, malformed, or uses an
     * unknown action keyword so that the game can always continue gracefully.
     *
     * @param response Raw string received from a remote client.
     * @return A Move representing the client's intended action.
     */
    public static Move parseMoveFromResponse(final String response) {
        if (response == null) {
            return createDefaultMove();
        }
        final String[] parts = response.split(":");
        // Minimum valid command: 3 segments ("MOVE", action, params).
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

    /**
     * Parses a compact gem-code string into a TAKE_THREE_DIFFERENT Move.
     * Each character of gemCodes must be a single-letter gem code (R, G, B, W, K).
     * Returns a default move if any character is unrecognised.
     *
     * @param gemCodes Concatenated gem codes, e.g. "RGB" for Red + Green + Blue.
     * @return A Move of type TAKE_THREE_DIFFERENT with the parsed gem counts.
     */
    public static Move parseTakeThreeMove(final String gemCodes) {
        final Map<Gem, Integer> gems = new HashMap<>();
        for (final char c : gemCodes.toUpperCase().toCharArray()) {
            final Gem gem = GemParser.parseGemCode(c);
            if (gem == null) {
                return createDefaultMove(); // unrecognised code → safe fallback
            }
            final int existingCount = gems.getOrDefault(gem, 0);
            gems.put(gem, existingCount + 1);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
    }

    /**
     * Parses a single gem-code character into a TAKE_TWO_SAME Move.
     * gemCode must be exactly one character long and map to a known gem.
     *
     * @param gemCode Single-character gem code, e.g. "R" for Red.
     * @return A Move of type TAKE_TWO_SAME taking 2 of the parsed gem.
     */
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

    /**
     * Parses a BUY command parameter into a BUY_CARD Move.
     * If the parameter starts with 'R' followed by a digit, it is treated as a
     * reserved-card purchase (e.g. "R42" → buy reserved card 42).
     * Otherwise it is treated as a visible-card purchase (e.g. "42" → buy card 42).
     *
     * @param param BUY command parameter string.
     * @return A Move of type BUY_CARD referencing the parsed card ID.
     */
    public static Move parseBuyMove(final String param) {
        // Detect "R<id>" pattern for reserved-card purchases.
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

    /**
     * Parses a RESERVE command parameter into a RESERVE_CARD Move.
     * Parameters starting with 'D' followed by a digit indicate a blind deck reserve
     * (e.g. "D2" → reserve from tier-2 deck). All others are treated as visible-card
     * reserves by card ID (e.g. "42" → reserve visible card 42).
     *
     * @param param RESERVE command parameter string.
     * @return A Move of type RESERVE_CARD targeting the parsed card or deck tier.
     */
    public static Move parseReserveMove(final String param) {
        if (param.length() > 1 && param.toUpperCase().charAt(0) == 'D') {
            // Deck-reserve: extract the tier number after the 'D' prefix.
            try {
                return Move.reserveFromDeck(Integer.parseInt(param.substring(1)));
            } catch (final NumberFormatException e) {
                return createDefaultMove();
            }
        }
        // Visible-card reserve: parse the raw card ID.
        try {
            return new Move(MoveType.RESERVE_CARD, Integer.parseInt(param), false);
        } catch (final NumberFormatException e) {
            return createDefaultMove();
        }
    }

    /**
     * Parses a DISCARD protocol response into a DISCARD_TOKENS Move.
     * Expected format: "DISCARD:<gem-codes>" (e.g. "DISCARD:RGK").
     * If the response is null or malformed, returns an empty discard move so that
     * the token-limit enforcement can still complete without crashing.
     *
     * @param response Raw DISCARD response string from the client.
     * @return A Move of type DISCARD_TOKENS containing the parsed gem counts.
     */
    public static Move parseDiscardMoveFromResponse(final String response) {
        if (response != null) {
            final String[] parts = response.split(":");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase("DISCARD")) {
                // Reuse parseTakeThreeMove to convert gem codes to a count map.
                return parseTakeThreeMove(parts[1]);
            }
        }
        // Return an empty discard so the caller always has a valid Move object.
        return new Move(MoveType.DISCARD_TOKENS, new HashMap<>());
    }

    /**
     * Creates a safe fallback Move used whenever parsing fails.
     * TAKE_THREE_DIFFERENT with an empty gem map is always a structurally valid
     * Move type, though it will likely be rejected by the validator. This prevents
     * null-pointer failures propagating through the rest of the game engine.
     *
     * @return A no-op TAKE_THREE_DIFFERENT move.
     */
    public static Move createDefaultMove() {
        return new Move(MoveType.TAKE_THREE_DIFFERENT);
    }
}
