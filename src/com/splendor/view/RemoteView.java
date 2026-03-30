/**
 * Network-based implementation of the game view.
 * Provides remote client display and input handling for network gameplay.
 * 
 */
package com.splendor.view;

import com.splendor.model.*;
import com.splendor.util.GameLogger;
import com.splendor.util.GemParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Network-based implementation of IGameView for remote clients.
 * Sends game state and receives moves via network protocol.
 */
public class RemoteView implements IGameView {

    private final String clientId;
    private final NetworkMessageHandler messageHandler;
    private final GameRenderer renderer;

    /**
     * Creates a new RemoteView for the specified client.
     * 
     * @param clientId       Client identifier
     * @param messageHandler Network message handler
     */
    public RemoteView(final String clientId, final NetworkMessageHandler messageHandler) {
        this.clientId = clientId;
        this.messageHandler = messageHandler;
        this.renderer = new GameRenderer();
    }

    /** Renders the full board state to a string and sends it to this client. */
    @Override
    public void displayGameState(final Game game) {
        messageHandler.sendToClient(clientId, renderer.renderToString(game));
    }

    /** Notifies this client whose turn it is. */
    @Override
    public void displayPlayerTurn(final Player player) {
        messageHandler.sendToClient(clientId, "It's " + player.getName() + "'s turn.");
    }

    /** Sends a success/info line to this client and waits for Enter, like ConsoleView. */
    @Override
    public String displayMessage(final String message) {
        messageHandler.sendToClient(clientId, Colors.colorize(message, Colors.GREEN));
        return waitForEnter();
    }

    /** Sends an error line to this client and waits for Enter, like ConsoleView. */
    @Override
    public String displayError(final String errorMessage) {
        sendErrorLine(errorMessage);
        return waitForEnter();
    }

    /** Sends a notification line to this client without waiting for a response. */
    @Override
    public void displayNotification(final String message) {
        messageHandler.sendToClient(clientId, Colors.colorize(message, Colors.GREEN));
    }

    /** Sends a red error line without waiting for input (used for broadcast-to-others). */
    void displayErrorNotification(final String errorMessage) {
        sendErrorLine(errorMessage);
    }

    /**
     * Sends a command prompt to the client and waits up to 30 seconds for a response.
     *
     * @param player The current player (unused here; kept for interface consistency).
     * @param game   The current game state (unused here; kept for interface consistency).
     * @return The trimmed response string, or "" if the client timed out.
     */
    @Override
    public String promptForCommand(final Player player, final Game game) {
        send("Command > ");
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        return response == null ? "" : response.trim();
    }

    /**
     * Sends the rendered board-plus-menu to the client, then loops prompting for a
     * valid menu selection and any required follow-up input (gem colors, card IDs, tier).
     * Returns a default move if the client times out with no response.
     *
     * @param player  The player whose turn it is.
     * @param game    The current game state (used for board rendering).
     * @param options The ordered list of MenuOptions to present.
     * @return A fully-constructed Move ready for validation.
     */
    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        // Show the board with menu, same as local play
        displayAvailableMoves(options, game);
        if (!player.getReservedCards().isEmpty()) {
            sendReservedCardDetails(player);
        }

        int maxOption = 0;
        final StringBuilder availableNumsBuilder = new StringBuilder();
        for (final MenuOption option : options) {
            if (option.getNumber() > maxOption) {
                maxOption = option.getNumber();
            }
            if (option.isAvailable()) {
                if (availableNumsBuilder.length() > 0) {
                    availableNumsBuilder.append(", ");
                }
                availableNumsBuilder.append(option.getNumber());
            }
        }
        final String availableNums = availableNumsBuilder.length() > 0
                ? availableNumsBuilder.toString()
                : "none";

        while (true) {
            send("Select option (" + availableNums + "): ");
            final String input = waitForResponse(120000);
            if (input == null) {
                displayError("Timeout — no input received.");
                return new Move(MoveType.TAKE_THREE_DIFFERENT);
            }

            final int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (final NumberFormatException e) {
                displayError("Enter a number between 1 and " + maxOption);
                continue;
            }

            MenuOption selected = null;
            for (final MenuOption option : options) {
                if (option.getNumber() == choice) {
                    selected = option;
                    break;
                }
            }

            if (selected == null) {
                displayError("Invalid selection. Choose 1-" + maxOption);
                continue;
            }
            if (!selected.isAvailable()) {
                displayError("Option unavailable: " + selected.getReason());
                continue;
            }

            try {
                return buildMoveFromOption(selected);
            } catch (final IllegalArgumentException e) {
                if ("BACK_TO_MENU".equals(e.getMessage())) {
                    displayNotification("Returning to menu...");
                    continue;
                }
                displayError(e.getMessage());
            }
        }
    }

    /**
     * Sends the appropriate follow-up prompt for the chosen menu option and
     * constructs the resulting Move from the client's response.
     *
     * Each branch handles one MenuAction:
     *   TAKE_THREE    — reads 3 gem color codes and builds a TAKE_THREE_DIFFERENT move.
     *   TAKE_TWO      — reads 1 gem color code and builds a TAKE_TWO_SAME move.
     *   RESERVE_VISIBLE — reads a card ID and builds a RESERVE_CARD move (face-up card).
     *   RESERVE_DECK  — reads a tier number and builds a RESERVE_CARD deck move.
     *   BUY_VISIBLE   — reads a card ID and builds a BUY_CARD move (face-up card).
     *   BUY_RESERVED  — reads a card ID and builds a BUY_CARD move (reserved card).
     *   EXIT_GAME     — returns an EXIT_GAME move immediately.
     *
     * @param option The MenuOption the player selected, driving the prompt text and move type.
     * @return A fully-constructed Move ready for validation.
     * @throws IllegalArgumentException if the client response is invalid.
     */
    private Move buildMoveFromOption(final MenuOption option) {
        switch (option.getAction()) {
            case TAKE_THREE: {
                send("Available colors: " + option.getDetail());
                send("Pick 3 colors (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                final List<Gem> gems = response == null || response.trim().isEmpty()
                        ? List.of()
                        : GemParser.parseGemSelection(response);
                if (gems.size() != 3) throw new IllegalArgumentException("Enter exactly 3 colors.");
                final Map<Gem, Integer> selected = new HashMap<>();
                for (final Gem g : gems) {
                    final int existingCount = selected.getOrDefault(g, 0);
                    selected.put(g, existingCount + 1);
                }
                return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
            }
            case TAKE_TWO: {
                send("Available colors: " + option.getDetail());
                send("Pick 1 color (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                final List<Gem> gems = response == null || response.trim().isEmpty()
                        ? List.of()
                        : GemParser.parseGemSelection(response);
                if (gems.size() != 1) throw new IllegalArgumentException("Enter exactly 1 color.");
                final Map<Gem, Integer> selected = new HashMap<>();
                selected.put(gems.get(0), 2);
                return new Move(MoveType.TAKE_TWO_SAME, selected);
            }
            case RESERVE_VISIBLE: {
                send("Visible card IDs: " + option.getDetail());
                send("Card ID (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                return new Move(MoveType.RESERVE_CARD, parseId(response), false);
            }
            case RESERVE_DECK: {
                send("Available tiers: " + option.getDetail());
                send("Tier (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                final int tier = parseId(response);
                return Move.reserveFromDeck(tier);
            }
            case BUY_VISIBLE: {
                send("Affordable card IDs: " + option.getDetail());
                send("Card ID (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                return new Move(MoveType.BUY_CARD, parseId(response), false);
            }
            case BUY_RESERVED: {
                send("Affordable reserved IDs: " + option.getDetail());
                send("Card ID (Z to go back): ");
                final String response = waitForResponse(60000);
                if (isBackToMenuInput(response)) {
                    throw new IllegalArgumentException("BACK_TO_MENU");
                }
                return new Move(MoveType.BUY_CARD, parseId(response), true);
            }
            case EXIT_GAME:
                return new Move(MoveType.EXIT_GAME);
            default:
                throw new IllegalArgumentException("Unknown action: " + option.getAction());
        }
    }

    /** Returns true if the client requested to return to the previous menu. */
    private boolean isBackToMenuInput(final String input) {
        if (input == null) {
            return false;
        }
        final String trimmed = input.trim();
        return trimmed.equalsIgnoreCase("Z") || trimmed.equalsIgnoreCase("UNDO");
    }

    /**
     * Parses a raw client response string as an integer ID (card ID or tier number).
     *
     * @param input The raw string received from the client; may be null if the
     *              client timed out.
     * @return The parsed integer.
     * @throws IllegalArgumentException if input is null or cannot be parsed as an integer.
     */
    private int parseId(final String input) {
        if (input == null) throw new IllegalArgumentException("No input received.");
        try {
            return Integer.parseInt(input.trim());
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number.");
        }
    }

    /**
     * Sends a plain text message to this client via the network message handler.
     * Convenience wrapper so callers do not need to pass clientId repeatedly.
     *
     * @param message The text to transmit to the client.
     */
    private void send(final String message) {
        messageHandler.sendToClient(clientId, message);
    }

    /** Sends a red ERROR-prefixed line without prompting for Enter. */
    private void sendErrorLine(final String errorMessage) {
        messageHandler.sendToClient(clientId, Colors.colorize("ERROR: " + errorMessage, Colors.RED));
    }

    /**
     * Blocks until the client sends a response line or the timeout expires.
     * Convenience wrapper so callers do not need to pass clientId repeatedly.
     *
     * @param timeoutMs Maximum milliseconds to wait before returning null.
     * @return The client's response string, or null if the timeout was reached.
     */
    private String waitForResponse(final int timeoutMs) {
        return messageHandler.waitForClientResponse(clientId, timeoutMs);
    }

    /**
     * Sends a discard instruction to the client and parses the response into a
     * DISCARD_TOKENS move. Falls back to a gold-first default discard if the
     * client does not respond within 30 seconds.
     *
     * @param player      The player who must discard tokens.
     * @param excessCount The number of tokens to discard to reach the 10-token limit.
     * @return A DISCARD_TOKENS Move specifying which tokens to return to the bank.
     */
    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        send("You must discard " + excessCount + " tokens. Format: COLOR QUANTITY (e.g., R 1)");
        final String response = messageHandler.waitForClientResponse(clientId, 30000);

        if (response == null) {
            displayError("Timeout waiting for discard selection");
            return createDefaultDiscardMove(player, excessCount);
        }

        return parseDiscardMove(response);
    }

    /**
     * Parses discard input from remote clients.
     *
     * Supported formats:
     * 1) Legacy protocol: DISCARD:R B
     * 2) Prompt format: R 1 B 1
     */
    private Move parseDiscardMove(final String rawResponse) {
        String payload = rawResponse == null ? "" : rawResponse.trim();
        final int separatorIndex = payload.indexOf(':');
        if (separatorIndex > 0 && payload.substring(0, separatorIndex).equalsIgnoreCase("DISCARD")) {
            payload = payload.substring(separatorIndex + 1).trim();
        }

        if (payload.isEmpty()) {
            return new Move(MoveType.DISCARD_TOKENS, new HashMap<>());
        }

        final String normalized = payload.replace(",", " ").trim();
        final String[] tokens = normalized.split("\\s+");
        final Map<Gem, Integer> gemMap = new HashMap<>();

        boolean parsedAsPairs = tokens.length >= 2 && tokens.length % 2 == 0;
        if (parsedAsPairs) {
            for (int i = 0; i < tokens.length; i += 2) {
                try {
                    final Gem gem = GemParser.parseGem(tokens[i]);
                    final int quantity = Integer.parseInt(tokens[i + 1]);
                    if (quantity <= 0) {
                        parsedAsPairs = false;
                        break;
                    }
                    final int existing = gemMap.getOrDefault(gem, 0);
                    gemMap.put(gem, existing + quantity);
                } catch (final RuntimeException e) {
                    parsedAsPairs = false;
                    break;
                }
            }
        }

        if (parsedAsPairs && !gemMap.isEmpty()) {
            return new Move(MoveType.DISCARD_TOKENS, gemMap);
        }

        // Backward compatibility for plain gem lists (e.g., "R B").
        final List<Gem> gems = GemParser.parseGemSelection(payload);
        for (final Gem gem : gems) {
            gemMap.put(gem, gemMap.getOrDefault(gem, 0) + 1);
        }
        return new Move(MoveType.DISCARD_TOKENS, gemMap);
    }

    /**
     * Sends a formatted game-over banner to this client including the winner's name,
     * their point total, and the full ranked final-scores table.
     *
     * @param winner      The player who won the game.
     * @param finalScores Map of every player's name to their final prestige point total.
     */
    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(50)).append("\n");
        sb.append("                   GAME OVER\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("WINNER: ").append(winner.getName()).append(" with ").append(winner.getTotalPoints()).append(" points!\n");
        sb.append("\nFinal Scores:\n");
        for (final Map.Entry<String, Integer> entry : finalScores.entrySet()) {
            sb.append(String.format("  %-10s: %d points%n", entry.getKey(), entry.getValue()));
        }
        sb.append("=".repeat(50));
        messageHandler.sendToClient(clientId, sb.toString());
    }

    /** Sends ANSI escape codes to move the cursor home and erase the screen on the client terminal. */
    @Override
    public void clearDisplay() {
        messageHandler.sendToClient(clientId, "\033[H\033[2J");
    }

    /**
     * Injects the built menu lines into the renderer and sends the full board-plus-menu
     * rendering to this client, mirroring what a local console player would see.
     *
     * @param options The current menu options to embed in the board display.
     * @param game    The current game state used to render the board.
     */
    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        renderer.setMenuLines(renderer.buildMenuLines(options));
        messageHandler.sendToClient(clientId, renderer.renderToString(game));
    }

    /**
     * Sends a numbered noble list to the client and waits up to 30 seconds for a choice.
     * Defaults to the first noble if the response is missing, out of range, or unparseable.
     *
     * @param player The player who qualifies for a noble visit.
     * @param nobles The subset of nobles the player can claim (size >= 1).
     * @return The chosen Noble, or nobles.get(0) as a safe fallback.
     */
    @Override
    public Noble promptForNobleChoice(final Player player, final List<Noble> nobles) {
        final StringBuilder sb = new StringBuilder();
        sb.append(player.getName()).append(" can claim a noble:\n");
        for (int i = 0; i < nobles.size(); i++) {
            final Noble noble = nobles.get(i);
            sb.append(String.format("%d) Noble %d - %d pts - %s%n",
                    i + 1, noble.getId(), noble.getPoints(), noble.getRequirements()));
        }
        sb.append("Choose noble (1-").append(nobles.size()).append("): ");
        send(sb.toString());
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        try {
            final int choice = Integer.parseInt(response.trim());
            if (choice >= 1 && choice <= nobles.size()) {
                return nobles.get(choice - 1);
            }
        } catch (final Exception e) {
            return nobles.get(0);
        }
        return nobles.get(0);
    }

    /**
     * Prompts this client to enter their player name and waits up to 30 seconds.
     * Returns a default name ("Player{N}") if the response is null or blank.
     *
     * @param playerNumber 1-based player number shown in the prompt.
     * @param totalPlayers Total number of players in the game (for context in the prompt).
     * @return The trimmed name entered by the client, or "Player{playerNumber}" as fallback.
     */
    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        send("Enter name for Player " + playerNumber + ": ");

        final String response = messageHandler.waitForClientResponse(clientId, 30000);

        if (response == null || response.trim().isEmpty()) {
            return "Player" + playerNumber;
        }

        return response.trim();
    }

    /**
     * Prompts this client to enter the desired player count and waits up to 30 seconds.
     * Returns 2 (minimum valid count) if the response is missing or not a valid integer.
     *
     * @return Player count entered by the client (2-4), or 2 as a safe fallback.
     */
    @Override
    public int promptForPlayerCount() {
        send("Enter number of players (2-4): ");

        final String response = messageHandler.waitForClientResponse(clientId, 30000);

        try {
            return Integer.parseInt(response.trim());
        } catch (final NumberFormatException e) {
            displayError("Invalid player count received, defaulting to 2");
            return 2;
        }
    }

    /** Sends a welcome banner to this client at the start of the network game session. */
    @Override
    public void displayWelcomeMessage() {
        send("Welcome to Splendor Network Game!");
    }

    /**
     * Sends a continue prompt and blocks up to 2 minutes for a response,
     * giving the remote player time to read the preceding output before the game advances.
     *
     * @return Trimmed client input ("" on plain Enter or timeout). Callers may
     *         still interpret non-empty input such as "Z" when needed.
     */
    @Override
    public String waitForEnter() {
        send("Press Enter to continue...");
        final String response = messageHandler.waitForClientResponse(clientId, 120000);
        return response == null ? "" : response.trim();
    }

    /** Sends a disconnect notice to the client and logs the closure server-side. */
    @Override
    public void close() {
        send("Game ended. Disconnecting.");
        GameLogger.info("Remote view closed for client: " + clientId);
    }


    /**
     * Creates a default discard move when client doesn't respond.
     * 
     * @param player      Player with excess tokens
     * @param excessCount Number of tokens to discard
     * @return Default discard move
     */
    private Move createDefaultDiscardMove(final Player player, final int excessCount) {
        // Default to discarding gold tokens first
        final Map<com.splendor.model.Gem, Integer> discard = new HashMap<>();
        discard.put(com.splendor.model.Gem.GOLD,
                Math.min(player.getTokenCount(com.splendor.model.Gem.GOLD), excessCount));
        return new Move(MoveType.DISCARD_TOKENS, discard);
    }

    /** Sends a concise reserved-card list for the active player. */
    private void sendReservedCardDetails(final Player player) {
        send("Your reserved cards:");
        for (final Card card : player.getReservedCards()) {
            send(String.format("  ID:%d | Pts:%d | Bonus:%s | Cost:%s",
                    card.getId(),
                    card.getPoints(),
                    card.getBonusGem() == null ? "-" : card.getBonusGem(),
                    card.getCost()));
        }
    }

}
