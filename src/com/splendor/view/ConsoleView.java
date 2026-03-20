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

                Gem gem = parseGem(parts[0]);
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

    private Move parseTakeMove(final String[] parts) {
        if (parts.length == 3 && parts[1].equals("2")) {
            final Gem gem = parseGem(parts[2]);
            final Map<Gem, Integer> selected = new HashMap<>();
            selected.put(gem, 2);
            return new Move(MoveType.TAKE_TWO_SAME, selected);
        }
        if (parts.length == 4) {
            final Map<Gem, Integer> selected = new HashMap<>();
            for (int i = 1; i <= 3; i++) {
                final Gem gem = parseGem(parts[i]);
                selected.merge(gem, 1, Integer::sum);
            }
            return new Move(MoveType.TAKE_THREE_DIFFERENT, selected);
        }
        throw new IllegalArgumentException("Invalid take command format");
    }

    private Move parseBuyMove(final String[] parts) {
        if (parts.length == 2) {
            final int cardId = Integer.parseInt(parts[1]);
            return new Move(MoveType.BUY_CARD, cardId, false);
        }
        if (parts.length == 3 && parts[1].equals("reserved")) {
            final int cardId = Integer.parseInt(parts[2]);
            return new Move(MoveType.BUY_CARD, cardId, true);
        }
        throw new IllegalArgumentException("Invalid buy command format");
    }

    private Move parseReserveMove(final String[] parts) {
        if (parts.length == 2) {
            final int cardId = Integer.parseInt(parts[1]);
            return new Move(MoveType.RESERVE_CARD, cardId, false);
        }
        if (parts.length == 3 && parts[1].equalsIgnoreCase("deck")) {
            final int tier = Integer.parseInt(parts[2]);
            return Move.reserveFromDeck(tier);
        }
        throw new IllegalArgumentException("Invalid reserve command format");
    }

    private Gem parseGem(final String token) {
        final String normalized = token.trim().toUpperCase();
        if (normalized.equals("W") || normalized.equals("WHITE")) {
            return Gem.WHITE;
        }
        if (normalized.equals("B") || normalized.equals("BLUE")) {
            return Gem.BLUE;
        }
        if (normalized.equals("G") || normalized.equals("GREEN")) {
            return Gem.GREEN;
        }
        if (normalized.equals("R") || normalized.equals("RED")) {
            return Gem.RED;
        }
        if (normalized.equals("K") || normalized.equals("BLACK")) {
            return Gem.BLACK;
        }
        if (normalized.equals("AU") || normalized.equals("GOLD")) {
            return Gem.GOLD;
        }
        throw new IllegalArgumentException("Unknown gem: " + token);
    }

    /**
     * Parses user input into a list of gem selections.
     *
     * @param input Raw user input
     * @return Parsed gem list
     */
    private List<Gem> parseGemSelection(final String input) {
        final String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please enter at least one color");
        }
        final String normalized = trimmed.toUpperCase();
        final String spaced = normalized.replaceAll("[^A-Z]+", " ").trim();
        if (!spaced.isEmpty() && spaced.contains(" ")) {
            final String[] parts = spaced.split("\\s+");
            final List<Gem> gems = new ArrayList<>();
            for (final String part : parts) {
                gems.add(parseGem(part));
            }
            return gems;
        }
        final String compact = spaced.isEmpty() ? normalized.replaceAll("[^A-Z]+", "") : spaced;
        return parseGemSequence(compact);
    }

    /**
     * Parses a compact gem sequence (e.g., "RGB" or "AUW") into a gem list.
     *
     * @param input Compact gem sequence
     * @return Parsed gem list
     */
    private List<Gem> parseGemSequence(final String input) {
        final List<Gem> gems = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            if (i + 1 < input.length() && input.startsWith("AU", i)) {
                gems.add(Gem.GOLD);
                i += 2;
                continue;
            }
            final char c = input.charAt(i);
            gems.add(parseGem(String.valueOf(c)));
            i += 1;
        }
        return gems;
    }

    private String formatRequirements(final Map<Gem, Integer> requirements) {
        final List<String> parts = new ArrayList<>();
        for (final Map.Entry<Gem, Integer> entry : requirements.entrySet()) {
            parts.add(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey()))
                    + ":" + entry.getValue());
        }
        return parts.isEmpty() ? "None" : String.join(" ", parts);
    }

    private enum MenuAction {
        TAKE_THREE,
        TAKE_TWO,
        RESERVE_VISIBLE,
        RESERVE_DECK,
        BUY_VISIBLE,
        BUY_RESERVED
    }

    public List<String> buildMenuLines(final List<MenuOption> options) {
        final List<String> lines = new ArrayList<>();
        lines.add("Goal: 15 points");
        lines.add("Pick one action (or 'Z' to Undo)");
        for (final MenuOption option : options) {
            final String base = option.getNumber() + ") " + option.getLabel() + ": ";
            final String detail = option.getDetail();
            final String reason = option.isAvailable() || option.getReason().isBlank() ? "" : " (" + option.getReason() + ")";
            if (option.isAvailable()) {
                lines.add(base + detail + reason);
            } else {
                final String plain = detail.replaceAll("\\u001B\\[[0-9;]*m", "");
                lines.add(Colors.colorize(base + plain + reason, Colors.DIM));
            }
        }
        return lines;
    }

    private Move promptTakeThree(final MenuOption option) {
        System.out.println("Available colors: " + option.getDetail());
        final String input = inputResolver.promptForString("Pick 3 colors (Z to go back): ", 1, 30);
        if (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO")) {
            throw new IllegalArgumentException("BACK_TO_MENU");
        }
        final List<Gem> parsed = parseGemSelection(input);
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
        final List<Gem> parsed = parseGemSelection(input);
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

    private List<Integer> getVisibleCardIds(final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                ids.add(card.getId());
            }
        }
        return ids;
    }

    private void printSection(final String title, final List<String> lines, final String color) {
        System.out.println(Colors.colorize(title, color));
        if (lines.isEmpty()) {
            System.out.println(Colors.colorize("None", color));
            return;
        }
        for (final String line : lines) {
            System.out.println(line);
        }
    }

    private void addActionLine(final List<String> target, final String name, final String command, final String detail,
            final boolean available, final String reason) {
        String line = String.format("%-22s %-18s %s", name, command, detail);
        if (!available && reason != null && !reason.isBlank()) {
            line = line + " - " + reason;
        }
        target.add(available ? line : Colors.colorize(line, Colors.GRAY));
    }


    private String formatIdList(final List<Integer> ids, final int maxCount) {
        if (ids.isEmpty()) {
            return "-";
        }
        final StringJoiner joiner = new StringJoiner(", ");
        final int limit = Math.min(ids.size(), maxCount);
        for (int i = 0; i < limit; i++) {
            joiner.add(String.valueOf(ids.get(i)));
        }
        if (ids.size() > maxCount) {
            joiner.add("...");
        }
        return joiner.toString();
    }

    private String formatGemList(final List<Gem> gems) {
        if (gems.isEmpty()) {
            return "-";
        }
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(gemShort(gem));
        }
        return joiner.toString();
    }

    private String formatColoredGemList(final List<Gem> gems) {
        if (gems.isEmpty()) {
            return "None";
        }
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(Colors.colorize(gemShort(gem), Colors.getGemColor(gem)));
        }
        return joiner.toString();
    }

    private List<Integer> getReservedCardIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            ids.add(card.getId());
        }
        return ids;
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
