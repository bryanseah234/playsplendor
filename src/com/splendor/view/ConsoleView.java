/**
 * Console-based implementation of the game view.
 * Provides text-based display and user input handling for console gameplay.
 * 
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
            if (currentPlayer != null && !currentPlayer.getReservedCards().isEmpty()) {
                System.out.println("Your reserved cards:");
                displayReservedCardDetails(currentPlayer);
            }

            // Find the highest option number to set the upper bound for input validation.
            int maxOption = 0;
            for (final MenuOption option : options) {
                if (option.getNumber() > maxOption) {
                    maxOption = option.getNumber();
                }
            }

            // Build a comma-separated list of available option numbers for the prompt text.
            final StringBuilder availableNumsBuilder = new StringBuilder();
            for (final MenuOption option : options) {
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

            final int choice = inputResolver.promptForInt(
                    "Select option (" + availableNums + "): ",
                    1,
                    maxOption,
                    () -> renderer.displayGameState(game));
            if (choice == -1) {
                displayNotification("Returning to menu...");
                continue;
            }

            // Find the MenuOption whose number matches what the player typed.
            MenuOption selected = null;
            for (final MenuOption option : options) {
                if (option.getNumber() == choice) {
                    selected = option;
                    break;
                }
            }
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

                final int existing = tokensToDiscard.getOrDefault(gem, 0);
                tokensToDiscard.put(gem, existing + qty);
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

        // Sort entries by score descending so the leaderboard reads highest-first.
        final List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(finalScores.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (final Map.Entry<String, Integer> entry : sortedEntries) {
            System.out.printf("  %-10s: %d points%n", entry.getKey(), entry.getValue());
        }

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

    /**
     * Collects input for a take-three-different-gems move.
     * Prints the available colors from the option detail, then reads a
     * space-separated or concatenated gem-code string (e.g., "R G B" or "RGB").
     * Throws IllegalArgumentException("BACK_TO_MENU") if the user enters Z/UNDO.
     *
     * @param option The TAKE_THREE MenuOption whose detail lists available colors.
     * @return A TAKE_THREE_DIFFERENT Move containing the three chosen gems.
     * @throws IllegalArgumentException if input is invalid or the user goes back.
     */
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
            final int existingCount = selected.getOrDefault(gem, 0);
            selected.put(gem, existingCount + 1);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
    }

    /**
     * Collects input for a take-two-same-gems move.
     * Prints the available colors from the option detail, then reads a single
     * gem-code string and wraps it as a quantity-2 entry.
     * Throws IllegalArgumentException("BACK_TO_MENU") if the user enters Z/UNDO.
     *
     * @param option The TAKE_TWO MenuOption whose detail lists eligible colors.
     * @return A TAKE_TWO_SAME Move with the chosen gem and quantity 2.
     * @throws IllegalArgumentException if input is invalid or the user goes back.
     */
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

    /**
     * Collects a card ID from the user for a reserve-visible-card move.
     * Prints the list of visible card IDs from the option detail, then reads
     * a numeric card ID. Throws IllegalArgumentException("BACK_TO_MENU") on Z/UNDO.
     *
     * @param option The RESERVE_VISIBLE MenuOption whose detail lists card IDs.
     * @return A RESERVE_CARD Move targeting the chosen face-up card.
     * @throws IllegalArgumentException if the user cancels back to the menu.
     */
    private Move promptReserveVisible(final MenuOption option) {
        System.out.println("Visible card IDs: " + option.getDetail());
        final int cardId = inputResolver.promptForInt("Card ID (Z to go back): ", 1, 9999);
        if (cardId == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        return new Move(MoveType.RESERVE_CARD, cardId, false);
    }

    /**
     * Collects a deck tier from the user for a blind deck-reserve move.
     * Parses the allowed tiers from the option detail (slash-separated, e.g., "1/2/3"),
     * prompts for a tier number, and validates the selection against the allowed list.
     * Throws IllegalArgumentException("BACK_TO_MENU") on Z/UNDO.
     *
     * @param option The RESERVE_DECK MenuOption whose detail lists non-empty tier numbers.
     * @return A RESERVE_CARD Move targeting the specified face-down deck.
     * @throws IllegalArgumentException if the tier is invalid or the user cancels.
     */
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

    /**
     * Collects a card ID from the user for a buy-visible-card move.
     * Prints the list of affordable card IDs from the option detail, then reads
     * a numeric card ID. Throws IllegalArgumentException("BACK_TO_MENU") on Z/UNDO.
     *
     * @param option The BUY_VISIBLE MenuOption whose detail lists affordable card IDs.
     * @return A BUY_CARD Move targeting the chosen face-up card.
     * @throws IllegalArgumentException if the user cancels back to the menu.
     */
    private Move promptBuyVisible(final MenuOption option) {
        System.out.println("Affordable IDs: " + option.getDetail());
        final int cardId = inputResolver.promptForInt("Card ID (Z to go back): ", 1, 9999);
        if (cardId == -1) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        return new Move(MoveType.BUY_CARD, cardId, false);
    }

    /**
     * Collects a reserved card ID for a buy-reserved-card move.
     * Calls displayReservedCardDetails to show a detailed table of the player's
     * reserved hand (including affordability status), then reads a numeric card ID.
     * Throws IllegalArgumentException("BACK_TO_MENU") on Z/UNDO.
     *
     * @param option The BUY_RESERVED MenuOption whose detail lists affordable reserved IDs.
     * @return A BUY_CARD Move with isReservedCard=true targeting the chosen card.
     * @throws IllegalArgumentException if the user cancels back to the menu.
     */
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

    /**
     * Prints a one-line summary for each reserved card in the player's hand.
     * Each line shows the card ID, prestige points, bonus gem color, gem cost,
     * and an ANSI-colored "[CAN BUY]" or "[NOT AFFORDABLE]" status tag so the
     * player can identify which reserved cards they can currently purchase.
     *
     * @param player The player whose reserved cards are displayed.
     */
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

    /**
     * Returns a short one- or two-character label for a gem color used in compact
     * card display lines (W, B, G, R, K, Au). Mirrors GemParser's format table.
     *
     * @param gem The gem type to abbreviate.
     * @return Short label string, or "" for unknown gem types.
     */
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
