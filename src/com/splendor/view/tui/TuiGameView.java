package com.splendor.view.tui;

import com.williamcallahan.tui4j.compat.bubbletea.Program;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.view.IGameView;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * IGameView adapter that bridges the synchronous GameController call pattern
 * to the asynchronous TUI4J event loop. Runs TUI4J Program on a daemon thread;
 * each IGameView method posts a TuiRequest and blocks until the TUI collects
 * a response from the user.
 */
public class TuiGameView implements IGameView {

    private final BlockingQueue<TuiRequest> requestQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private final MoveValidator moveValidator = new MoveValidator();
    private Thread tuiThread;

    public TuiGameView() {
        RequestMessage.setRequestQueue(requestQueue);

        final TuiAppModel model = new TuiAppModel(requestQueue, responseQueue);
        tuiThread = new Thread(() -> {
            try {
                new Program(model).withAltScreen().run();
            } catch (final Exception e) {
                System.err.println("TUI4J crashed: " + e.getMessage());
            }
        }, "tui4j-event-loop");
        tuiThread.setDaemon(true);
        tuiThread.start();
    }

    private String postAndWait(final TuiRequest request) {
        requestQueue.offer(request);
        try {
            return responseQueue.take();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private void postFireAndForget(final TuiRequest request) {
        requestQueue.offer(request);
        try {
            responseQueue.take();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void displayWelcomeMessage() {
        postFireAndForget(TuiRequest.welcome());
    }

    @Override
    public int promptForPlayerCount() {
        final String response = postAndWait(TuiRequest.playerCount());
        try {
            final int count = Integer.parseInt(response.trim());
            if (count >= 2 && count <= 4) return count;
        } catch (final NumberFormatException ignored) {}
        return 2;
    }

    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        final String prompt = "Enter name for Player " + playerNumber + " of " + totalPlayers + ": ";
        final String response = postAndWait(TuiRequest.playerName(prompt));
        return response.isEmpty() ? "Player" + playerNumber : response;
    }

    @Override
    public void displayGameState(final Game game) {
        postFireAndForget(TuiRequest.gameState(game));
    }

    @Override
    public void displayPlayerTurn(final Player player) {
        postFireAndForget(TuiRequest.playerTurn(player));
    }

    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        postFireAndForget(TuiRequest.availableMoves(options, game));
    }

    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        while (true) {
            final String response = postAndWait(TuiRequest.promptMove(player, game, options));

            if ("Z".equalsIgnoreCase(response) || "UNDO".equalsIgnoreCase(response)) {
                throw new IllegalArgumentException("BACK_TO_MENU");
            }
            if ("EXIT".equalsIgnoreCase(response)) {
                return new Move(MoveType.EXIT_GAME);
            }

            try {
                final int choice = Integer.parseInt(response.trim());
                final MenuOption selected = options.stream()
                        .filter(o -> o.getNumber() == choice)
                        .findFirst().orElse(null);
                if (selected == null || !selected.isAvailable()) continue;

                return buildMoveFromOption(selected);
            } catch (final NumberFormatException ignored) {
            } catch (final IllegalArgumentException e) {
                if ("BACK_TO_MENU".equals(e.getMessage())) continue;
                postFireAndForget(TuiRequest.error(e.getMessage()));
            }
        }
    }

    private Move buildMoveFromOption(final MenuOption option) {
        switch (option.getAction()) {
            case TAKE_THREE: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Available: " + option.getDetail() + "\nPick 3 colors (e.g. R G B): "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                final List<Gem> gems = parseGemSelection(input);
                if (gems.size() != 3) throw new IllegalArgumentException("Enter exactly 3 colors");
                final Map<Gem, Integer> selected = new HashMap<>();
                for (final Gem g : gems) selected.merge(g, 1, Integer::sum);
                return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
            }
            case TAKE_TWO: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Available: " + option.getDetail() + "\nPick 1 color to take 2: "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                final List<Gem> gems = parseGemSelection(input);
                if (gems.size() != 1) throw new IllegalArgumentException("Enter exactly 1 color");
                final Map<Gem, Integer> selected = new HashMap<>();
                selected.put(gems.get(0), 2);
                return new Move(MoveType.TAKE_TWO_SAME, selected);
            }
            case RESERVE_VISIBLE: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Visible IDs: " + option.getDetail() + "\nCard ID to reserve: "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                return new Move(MoveType.RESERVE_CARD, Integer.parseInt(input.trim()), false);
            }
            case RESERVE_DECK: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Available tiers: " + option.getDetail() + "\nDeck tier: "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                return Move.reserveFromDeck(Integer.parseInt(input.trim()));
            }
            case BUY_VISIBLE: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Affordable IDs: " + option.getDetail() + "\nCard ID to buy: "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                return new Move(MoveType.BUY_CARD, Integer.parseInt(input.trim()), false);
            }
            case BUY_RESERVED: {
                final String input = postAndWait(TuiRequest.subInput(
                        "Reserved IDs: " + option.getDetail() + "\nReserved card ID to buy: "));
                if (isBack(input)) throw new IllegalArgumentException("BACK_TO_MENU");
                return new Move(MoveType.BUY_CARD, Integer.parseInt(input.trim()), true);
            }
            case EXIT_GAME:
                return new Move(MoveType.EXIT_GAME);
            default:
                throw new IllegalArgumentException("Unknown action: " + option.getAction());
        }
    }

    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        final Map<Gem, Integer> tokensToDiscard = new HashMap<>();
        int remaining = excessCount;

        while (remaining > 0) {
            final String input = postAndWait(TuiRequest.tokenDiscard(player, remaining));
            try {
                final String[] parts = input.trim().toUpperCase().split("\\s+");
                if (parts.length != 2) throw new IllegalArgumentException("Format: COLOR QUANTITY");
                final Gem gem = parseGem(parts[0]);
                final int qty = Integer.parseInt(parts[1]);
                if (qty <= 0 || qty > remaining) throw new IllegalArgumentException("Invalid quantity");
                if (player.getTokenCount(gem) < qty) throw new IllegalArgumentException("Not enough tokens");
                tokensToDiscard.merge(gem, qty, Integer::sum);
                remaining -= qty;
            } catch (final Exception e) {
                postFireAndForget(TuiRequest.error("Invalid: " + e.getMessage()));
            }
        }
        return new Move(MoveType.DISCARD_TOKENS, tokensToDiscard);
    }

    @Override
    public Noble promptForNobleChoice(final Player player, final List<Noble> nobles) {
        final String response = postAndWait(TuiRequest.nobleChoice(player, nobles));
        try {
            final int choice = Integer.parseInt(response.trim());
            if (choice >= 1 && choice <= nobles.size()) return nobles.get(choice - 1);
        } catch (final NumberFormatException ignored) {}
        return nobles.get(0);
    }

    @Override
    public String displayMessage(final String message) {
        return postAndWait(TuiRequest.message(message));
    }

    @Override
    public void displayNotification(final String message) {
        postFireAndForget(TuiRequest.notification(message));
    }

    @Override
    public String displayError(final String errorMessage) {
        return postAndWait(TuiRequest.error(errorMessage));
    }

    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        final StringBuilder sb = new StringBuilder();
        sb.append("WINNER: ").append(winner.getName())
          .append(" with ").append(winner.getTotalPoints()).append(" points!\n\n");
        sb.append("Final Scores:\n");
        finalScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sb.append(String.format("  %-10s: %d points%n", e.getKey(), e.getValue())));
        postAndWait(TuiRequest.winner(sb.toString()));
    }

    @Override
    public void clearDisplay() {
    }

    @Override
    public String promptForCommand(final Player player, final Game game) {
        return postAndWait(TuiRequest.subInput("Command > "));
    }

    @Override
    public String waitForEnter() {
        return postAndWait(TuiRequest.waitForEnter(""));
    }

    @Override
    public void close() {
        requestQueue.offer(TuiRequest.close());
        if (tuiThread != null) {
            try {
                tuiThread.join(3000);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isBack(final String input) {
        return "Z".equalsIgnoreCase(input) || "UNDO".equalsIgnoreCase(input);
    }

    private Gem parseGem(final String token) {
        return switch (token.trim().toUpperCase()) {
            case "W", "WHITE" -> Gem.WHITE;
            case "B", "BLUE" -> Gem.BLUE;
            case "G", "GREEN" -> Gem.GREEN;
            case "R", "RED" -> Gem.RED;
            case "K", "BLACK" -> Gem.BLACK;
            case "AU", "GOLD" -> Gem.GOLD;
            default -> throw new IllegalArgumentException("Unknown gem: " + token);
        };
    }

    private List<Gem> parseGemSelection(final String input) {
        if (input == null || input.trim().isEmpty()) return List.of();
        final List<Gem> gems = new ArrayList<>();
        final String upper = input.trim().toUpperCase().replaceAll("[^A-Z]+", " ").trim();
        final String[] parts = upper.contains(" ") ? upper.split("\\s+") : new String[]{upper};
        for (final String part : parts) {
            int i = 0;
            while (i < part.length()) {
                if (i + 1 < part.length() && part.startsWith("AU", i)) {
                    gems.add(Gem.GOLD);
                    i += 2;
                } else {
                    gems.add(parseGem(String.valueOf(part.charAt(i))));
                    i++;
                }
            }
        }
        return gems;
    }
}
