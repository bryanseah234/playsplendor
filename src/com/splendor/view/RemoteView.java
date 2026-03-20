/**
 * Network-based implementation of the game view.
 * Provides remote client display and input handling for network gameplay.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.view;

import com.splendor.model.*;
import com.splendor.network.NetworkProtocol;
import com.splendor.util.GameLogger;
import com.splendor.util.GemParser;
import com.splendor.util.MoveParser;
import com.splendor.model.MenuOption;
import com.splendor.model.Player;
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

    @Override
    public void displayGameState(final Game game) {
        messageHandler.sendToClient(clientId, renderer.renderToString(game));
    }

    @Override
    public void displayPlayerTurn(final Player player) {
        messageHandler.sendToClient(clientId, "It's " + player.getName() + "'s turn.");
    }

    @Override
    public String displayMessage(final String message) {
        messageHandler.sendToClient(clientId, message);
        return "";
    }

    @Override
    public String displayError(final String errorMessage) {
        messageHandler.sendToClient(clientId, "ERROR: " + errorMessage);
        return "";
    }

    @Override
    public void displayNotification(final String message) {
        messageHandler.sendToClient(clientId, message);
    }

    @Override
    public String promptForCommand(final Player player, final Game game) {
        send("Command > ");
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        return response == null ? "" : response.trim();
    }

    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        // Show the board with menu, same as local play
        displayAvailableMoves(options, game);

        final int maxOption = options.stream().mapToInt(MenuOption::getNumber).max().orElse(0);

        while (true) {
            send("Select option (1-" + maxOption + "): ");
            final String input = waitForResponse(120000);
            if (input == null) {
                displayError("Timeout — no input received.");
                return MoveParser.createDefaultMove();
            }

            final int choice;
            try {
                choice = Integer.parseInt(input.trim());
            } catch (final NumberFormatException e) {
                displayError("Enter a number between 1 and " + maxOption);
                continue;
            }

            final MenuOption selected = options.stream()
                    .filter(o -> o.getNumber() == choice)
                    .findFirst().orElse(null);

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
                displayError(e.getMessage());
            }
        }
    }

    /** Sends follow-up prompts and builds a Move from the chosen MenuOption. */
    private Move buildMoveFromOption(final MenuOption option) {
        switch (option.getAction()) {
            case TAKE_THREE: {
                send("Available colors: " + option.getDetail() + "\nEnter 3 colors (e.g. R G B or RGB): ");
                final String response = waitForResponse(60000);
                final List<Gem> gems = response == null || response.trim().isEmpty()
                        ? List.of()
                        : GemParser.parseGemSelection(response);
                if (gems.size() != 3) throw new IllegalArgumentException("Enter exactly 3 colors.");
                final Map<Gem, Integer> selected = new HashMap<>();
                for (final Gem g : gems) selected.merge(g, 1, Integer::sum);
                return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
            }
            case TAKE_TWO: {
                send("Available colors: " + option.getDetail() + "\nEnter 1 color to take 2 of (e.g. R): ");
                final String response = waitForResponse(60000);
                final List<Gem> gems = response == null || response.trim().isEmpty()
                        ? List.of()
                        : GemParser.parseGemSelection(response);
                if (gems.size() != 1) throw new IllegalArgumentException("Enter exactly 1 color.");
                final Map<Gem, Integer> selected = new HashMap<>();
                selected.put(gems.get(0), 2);
                return new Move(MoveType.TAKE_TWO_SAME, selected);
            }
            case RESERVE_VISIBLE: {
                send("Visible card IDs: " + option.getDetail() + "\nEnter card ID to reserve: ");
                return new Move(MoveType.RESERVE_CARD, parseId(waitForResponse(60000)), false);
            }
            case RESERVE_DECK: {
                send("Available tiers: " + option.getDetail() + "\nEnter deck tier to reserve from: ");
                final int tier = parseId(waitForResponse(60000));
                return Move.reserveFromDeck(tier);
            }
            case BUY_VISIBLE: {
                send("Affordable card IDs: " + option.getDetail() + "\nEnter card ID to buy: ");
                return new Move(MoveType.BUY_CARD, parseId(waitForResponse(60000)), false);
            }
            case BUY_RESERVED: {
                send("Affordable reserved IDs: " + option.getDetail() + "\nEnter reserved card ID to buy: ");
                return new Move(MoveType.BUY_CARD, parseId(waitForResponse(60000)), true);
            }
            case EXIT_GAME:
                return new Move(MoveType.EXIT_GAME);
            default:
                throw new IllegalArgumentException("Unknown action: " + option.getAction());
        }
    }

    private int parseId(final String input) {
        if (input == null) throw new IllegalArgumentException("No input received.");
        try {
            return Integer.parseInt(input.trim());
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number.");
        }
    }

    /** Sends a plain message line to the client. */
    private void send(final String message) {
        messageHandler.sendToClient(clientId, message);
    }

    /** Waits for the next line from this client. */
    private String waitForResponse(final int timeoutMs) {
        return messageHandler.waitForClientResponse(clientId, timeoutMs);
    }

    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        send("You must discard " + excessCount + " tokens. Format: COLOR QUANTITY (e.g., R 1)");
        final String response = messageHandler.waitForClientResponse(clientId, 30000);

        if (response == null) {
            displayError("Timeout waiting for discard selection");
            return createDefaultDiscardMove(player, excessCount);
        }

        return MoveParser.parseDiscardMoveFromResponse(response);
    }

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

    @Override
    public void clearDisplay() {
        messageHandler.sendToClient(clientId, "\033[H\033[2J");
    }

    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        renderer.setMenuLines(renderer.buildMenuLines(options));
        messageHandler.sendToClient(clientId, renderer.renderToString(game));
    }

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

    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        send("Enter name for Player " + playerNumber + ": ");

        final String response = messageHandler.waitForClientResponse(clientId, 30000);

        if (response == null || response.trim().isEmpty()) {
            return "Player" + playerNumber;
        }

        return response.trim();
    }

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

    @Override
    public void displayWelcomeMessage() {
        send("Welcome to Splendor Network Game!");
    }

    @Override
    public String waitForEnter() {
        send("Press Enter to continue...");
        messageHandler.waitForClientResponse(clientId, 120000);
        return "";
    }

    @Override
    public void close() {
        send("Game ended. Disconnecting.");
        GameLogger.info("Remote view closed for client: " + clientId);
    }


    /**
     * Formats final scores for display.
     * 
     * @param finalScores Map of player names to scores
     * @return Formatted scores string
     */
    private String formatFinalScores(final Map<String, Integer> finalScores) {
        final StringBuilder scores = new StringBuilder();

        for (final Map.Entry<String, Integer> entry : finalScores.entrySet()) {
            if (scores.length() > 0) {
                scores.append(";");
            }
            scores.append(entry.getKey()).append(":").append(entry.getValue());
        }

        return scores.toString();
    }

    private String formatNobles(final List<Noble> nobles) {
        final StringBuilder sb = new StringBuilder();
        for (final Noble noble : nobles) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(noble.getId()).append(":").append(noble.getPoints()).append(":").append(noble.getRequirements());
        }
        return sb.toString();
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

    /**
     * Network message handler interface for communication.
     */
    public interface NetworkMessageHandler {
        void sendToClient(String clientId, String message);

        String waitForClientResponse(String clientId, int timeoutMs);
    }
}
