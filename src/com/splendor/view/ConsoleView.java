/**
 * Console-based implementation of the game view.
 * Provides text-based display and user input handling for console gameplay.
 * 
 * @author Splendor Development Team
 * @version 2.0
 * // Edited by AI; implemented Tabular ASCII board, Status Bar, and robust input handling
 */
package com.splendor.view;

import com.splendor.model.*;
import com.splendor.model.validator.MoveValidator;
import com.splendor.util.GemParser;
import com.splendor.util.InputResolver;
import java.util.*;

/**
 * Console-based implementation of IGameView.
 * Provides text-based display and user input handling.
 */
public class ConsoleView implements IGameView {

    private final Scanner scanner;
    private final InputResolver inputResolver;
    private final GameRenderer renderer;
    private final MoveValidator moveValidator;
    private Player currentPlayer;

    /**
     * Creates a new ConsoleView with default input handling.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.inputResolver = new InputResolver();
        this.renderer = new GameRenderer();
        this.moveValidator = new MoveValidator();
        this.currentPlayer = null;
    }

    @Override
    public void displayGameState(final Game game) {
        renderer.displayGameState(game);
    }

    @Override
    public void displayPlayerTurn(final Player player) {
        // Handled by displayStatus now
    }

    @Override
    public String displayMessage(final String message) {
        System.out.println(Colors.colorize(message, Colors.GREEN));
        return waitForEnter();
    }

    @Override
    public void displayNotification(final String message) {
        System.out.println(Colors.colorize(message, Colors.GREEN));
    }

    @Override
    public String displayError(final String errorMessage) {
        System.out.println(Colors.colorize("ERROR: " + errorMessage, Colors.RED));
        return waitForEnter();
    }

    @Override
    public String waitForEnter() {
        System.out.print("Press Enter to continue... ");
        try {
            return scanner.nextLine();
        } catch (final java.util.NoSuchElementException e) {
            return "";
        }
    }

    @Override
    public String promptForCommand(final Player player, final Game game) {
        renderer.displayGameState(game);
        return inputResolver.promptForString("Command > ", 1, 60);
    }

    @Override
    public Move promptForMove(final Player player, final Game game, final List<MenuOption> options) {
        this.currentPlayer = player;
        while (true) {
            displayAvailableMoves(options, game);
            final int maxOption = options.stream().mapToInt(MenuOption::getNumber).max().orElse(0);
            final String availableNums = options.stream()
                    .filter(MenuOption::isAvailable)
                    .map(o -> String.valueOf(o.getNumber()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
            final int choice = inputResolver.promptForInt(
                    "Select option (" + availableNums + "): ",
                    1,
                    maxOption,
                    () -> renderer.displayGameState(game));
            if (choice == -1) {
                displayNotification("Returning to menu...");
                continue;
            }
            final MenuOption selected = options.stream()
                    .filter(option -> option.getNumber() == choice)
                    .findFirst()
                    .orElse(null);
            if (selected == null) {
                displayError("Invalid selection");
                continue;
            }
            if (!selected.isAvailable()) {
                displayError("Option unavailable: " + selected.getReason());
                continue;
            }
            try {
                switch (selected.getAction()) {
                    case TAKE_THREE:
                        return promptTakeThree(selected);
                    case TAKE_TWO:
                        return promptTakeTwo(selected);
                    case RESERVE_VISIBLE:
                        return promptReserveVisible(selected);
                    case RESERVE_DECK:
                        return promptReserveDeck(selected);
                    case BUY_VISIBLE:
                        return promptBuyVisible(selected);
                    case BUY_RESERVED:
                        return promptBuyReserved(selected);
                    case EXIT_GAME:
                        return new Move(MoveType.EXIT_GAME);
                    default:
                        throw new IllegalArgumentException("Unknown menu action: " + selected.getAction());
                }
            } catch (final IllegalArgumentException e) {
                if ("BACK_TO_MENU".equals(e.getMessage())) {
                    displayNotification("Returning to menu...");
                    continue;
                }
                displayError(e.getMessage());
            }
        }
    }

    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        System.out.println("\n" + Colors.colorize("!!! TOKEN LIMIT EXCEEDED !!!", Colors.RED));
        System.out.println("You must discard " + excessCount + " tokens.");
        renderer.displayPlayerTokens(player);

        final Map<Gem, Integer> tokensToDiscard = new HashMap<>();
        int remainingToDiscard = excessCount;

        while (remainingToDiscard > 0) {
            System.out.println("Remaining to discard: " + remainingToDiscard);
            System.out.println("Format: COLOR QUANTITY (e.g., R 1 or Red 1)");
            String input = inputResolver.promptForString("> ", 3, 20).toUpperCase();

            try {
                String[] parts = input.split("\\s+");
                if (parts.length != 2)
                    throw new IllegalArgumentException("Invalid format.");

                Gem gem = GemParser.parseGem(parts[0]);
                int qty = Integer.parseInt(parts[1]);

                if (qty <= 0)
                    throw new IllegalArgumentException("Quantity must be positive.");
                if (qty > remainingToDiscard)
                    throw new IllegalArgumentException("Quantity exceeds required discard.");
                if (player.getTokenCount(gem) < qty)
                    throw new IllegalArgumentException("Not enough tokens of that type.");

                tokensToDiscard.merge(gem, qty, Integer::sum);
                remainingToDiscard -= qty;

            } catch (Exception e) {
                System.out.println(Colors.colorize("Invalid input: " + e.getMessage(), Colors.RED));
            }
        }

        return new Move(MoveType.DISCARD_TOKENS, tokensToDiscard);
    }

    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(Colors.colorize("                   GAME OVER", Colors.GOLD));
        System.out.println("=".repeat(50));
        System.out.println("WINNER: " + Colors.colorize(winner.getName(), Colors.CYAN) + " with "
                + winner.getTotalPoints() + " points!");
        System.out.println("\nFinal Scores:");

        finalScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("  %-10s: %d points%n", entry.getKey(), entry.getValue()));

        System.out.println("=".repeat(50));
    }

    @Override
    public void clearDisplay() {
        renderer.clearDisplay();
    }

    @Override
    public void displayAvailableMoves(final List<MenuOption> options, final Game game) {
        renderer.setMenuLines(renderer.buildMenuLines(options));
        renderer.displayGameState(game);
    }

    @Override
    public Noble promptForNobleChoice(final Player player, final List<Noble> nobles) {
        System.out.println("\n" + Colors.colorize(player.getName() + " can claim a noble:", Colors.PURPLE));
        for (int i = 0; i < nobles.size(); i++) {
            final Noble noble = nobles.get(i);
            final String reqs = formatRequirements(noble.getRequirements());
            System.out.println(
                    String.format("%d) Noble %d - %d pts - %s", i + 1, noble.getId(), noble.getPoints(), reqs));
        }
        final int choice = inputResolver.promptForInt("Choose noble (1-" + nobles.size() + "): ", 1, nobles.size());
        return nobles.get(choice - 1);
    }

    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        return inputResolver.promptForString("Enter name for Player " + playerNumber + ": ", 1, 20);
    }

    @Override
    public int promptForPlayerCount() {
        return inputResolver.promptForInt("Enter number of players (2-4): ", 2, 4);
    }

    @Override
    public void displayWelcomeMessage() {
        System.out.println(Colors.colorize("Welcome to Splendor!", Colors.GOLD));
    }

    @Override
    public void close() {
        inputResolver.close();
        scanner.close();
    }

    private String formatRequirements(final Map<Gem, Integer> requirements) {
        final List<String> parts = new ArrayList<>();
        for (final Map.Entry<Gem, Integer> entry : requirements.entrySet()) {
            parts.add(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey()))
                    + ":" + entry.getValue());
        }
        return parts.isEmpty() ? "None" : String.join(" ", parts);
    }

    private Move promptTakeThree(final MenuOption option) {
        System.out.println("Available colors: " + option.getDetail());
        final String input = inputResolver.promptForString("Pick 3 colors (Z to go back): ", 1, 30);
        if (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO")) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        final List<Gem> parsed = GemParser.parseGemSelection(input);
        if (parsed.size() != 3) {
            throw new IllegalArgumentException("Please enter exactly 3 colors");
        }
        final Map<Gem, Integer> selected = new HashMap<>();
        for (final Gem gem : parsed) {
            selected.merge(gem, 1, Integer::sum);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
    }

    private Move promptTakeTwo(final MenuOption option) {
        System.out.println("Available colors: " + option.getDetail());
        final String input = inputResolver.promptForString("Pick 1 color (Z to go back): ", 1, 10);
        if (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO")) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        final List<Gem> parsed = GemParser.parseGemSelection(input);
        if (parsed.size() != 1) {
            throw new IllegalArgumentException("Please enter exactly 1 color");
        }
        final Map<Gem, Integer> selected = new HashMap<>();
        selected.put(parsed.get(0), 2);
        return new Move(MoveType.TAKE_TWO_SAME, selected);
    }

    private Move promptReserveVisible(final MenuOption option) {
        System.out.println("Visible card IDs: " + option.getDetail());
        final int cardId = inputResolver.promptForInt("Card ID (Z to go back): ", 1, 9999);
        if (cardId == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        return new Move(MoveType.RESERVE_CARD, cardId, false);
    }

    private Move promptReserveDeck(final MenuOption option) {
        System.out.println("Available tiers: " + option.getDetail());
        final String[] allowed = option.getDetail().split("/");
        final List<Integer> allowedTiers = new ArrayList<>();
        for (String s : allowed) {
            allowedTiers.add(Integer.parseInt(s.trim()));
        }

        final int tier = inputResolver.promptForInt("Tier (Z to go back): ", 1, 3);
        if (tier == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        if (!allowedTiers.contains(tier)) {
            throw new IllegalArgumentException("Tier " + tier + " has no more cards!");
        }
        return Move.reserveFromDeck(tier);
    }

    private Move promptBuyVisible(final MenuOption option) {
        System.out.println("Affordable IDs: " + option.getDetail());
        final int cardId = inputResolver.promptForInt("Card ID (Z to go back): ", 1, 9999);
        if (cardId == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        return new Move(MoveType.BUY_CARD, cardId, false);
    }

    private Move promptBuyReserved(final MenuOption option) {
        System.out.println("Your reserved cards:");
        if (currentPlayer != null) {
            displayReservedCardDetails(currentPlayer);
        }
        System.out.println("Affordable reserved IDs: " + option.getDetail());
        final int cardId = inputResolver.promptForInt("Card ID (Z to go back): ", 1, 9999);
        if (cardId == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        return new Move(MoveType.BUY_CARD, cardId, true);
    }

    private void displayReservedCardDetails(final Player player) {
        final List<Card> reserved = player.getReservedCards();
        if (reserved.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (final Card card : reserved) {
            final boolean affordable = moveValidator.canPlayerAffordCard(player, card);
            final String status = affordable
                    ? Colors.colorize("[CAN BUY]", Colors.GREEN)
                    : Colors.colorize("[NOT AFFORDABLE]", Colors.RED);
            final String bonus = card.getBonusGem() == null ? "-"
                    : Colors.colorize(gemShort(card.getBonusGem()), Colors.getGemColor(card.getBonusGem()));
            final List<String> costParts = new ArrayList<>();
            for (final Map.Entry<Gem, Integer> entry : card.getCost().entrySet()) {
                if (entry.getValue() > 0) {
                    costParts.add(Colors.colorize(gemShort(entry.getKey()), Colors.getGemColor(entry.getKey()))
                            + ":" + entry.getValue());
                }
            }
            final String costDisplay = costParts.isEmpty() ? "Free" : String.join(" ", costParts);
            System.out.printf("  ID:%d | Pts:%d | Bonus:%s | Cost: %s %s%n",
                    card.getId(), card.getPoints(), bonus, costDisplay, status);
        }
    }

    private String gemShort(final Gem gem) {
        if (gem == Gem.WHITE) {
            return "W";
        }
        if (gem == Gem.BLUE) {
            return "B";
        }
        if (gem == Gem.GREEN) {
            return "G";
        }
        if (gem == Gem.RED) {
            return "R";
        }
        if (gem == Gem.BLACK) {
            return "K";
        }
        if (gem == Gem.GOLD) {
            return "Au";
        }
        return "";
    }
}
