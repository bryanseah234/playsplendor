/**
 * Console-based implementation of the game view.
 * Provides text-based display and user input handling for console gameplay.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented ASCII visualization
 */
package com.splendor.view;

import com.splendor.model.*;
import com.splendor.util.InputResolver;
import java.util.*;

/**
 * Console-based implementation of IGameView.
 * Provides text-based display and user input handling.
 */
public class ConsoleView implements IGameView {
    
    private final Scanner scanner;
    private final InputResolver inputResolver;
    // Map display index (1, 2, 3...) to Card ID for the current turn
    private final Map<Integer, Integer> visibleCardMap; 
    
    /**
     * ANSI Color Constants for terminal output.
     */
    private static class Colors {
        static final String RESET = "\u001B[0m";
        static final String RED = "\u001B[31m";      // Red Gem
        static final String GREEN = "\u001B[32m";    // Green Gem
        static final String BLUE = "\u001B[34m";     // Blue Gem
        static final String WHITE = "\u001B[37m";    // White Gem
        static final String BLACK = "\u001B[90m";    // Black Gem (Dark Gray)
        static final String GOLD = "\u001B[33m";     // Gold Gem
        static final String CYAN = "\u001B[36m";     // Player Names / Info
        static final String PURPLE = "\u001B[35m";   // Nobles
        
        static String colorize(String text, String colorCode) {
            return colorCode + text + RESET;
        }

        static String getGemColor(Gem gem) {
            switch (gem) {
                case RED: return RED;
                case GREEN: return GREEN;
                case BLUE: return BLUE;
                case WHITE: return WHITE;
                case BLACK: return BLACK;
                case GOLD: return GOLD;
                default: return RESET;
            }
        }
    }
    
    /**
     * Creates a new ConsoleView with default input handling.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.inputResolver = new InputResolver();
        this.visibleCardMap = new HashMap<>();
    }
    
    @Override
    public void displayGameState(final Game game) {
        clearDisplay();
        displayHeader();
        displayBoard(game.getBoard());
        displayPlayers(game.getPlayers());
        displayCurrentPlayer(game.getCurrentPlayer());
    }
    
    @Override
    public void displayPlayerTurn(final Player player) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(Colors.colorize(player.getName() + "'s Turn", Colors.CYAN));
        System.out.println("=".repeat(50));
    }
    
    @Override
    public void displayMessage(final String message) {
        System.out.println(Colors.colorize(message, Colors.GREEN));
    }
    
    @Override
    public void displayError(final String errorMessage) {
        System.err.println(Colors.colorize("ERROR: " + errorMessage, Colors.RED));
    }
    
    @Override
    public Move promptForMove(final Player player, final Game game) {
        // Pre-calculate availability to prevent invalid selection
        boolean canTake3 = false;
        boolean canTake2 = false;
        boolean canReserve = false;
        boolean canBuy = false;
        boolean canBuyReserved = false;

        if (game != null) {
            Board b = game.getBoard();
            long pilesWithGems = b.getGemBank().entrySet().stream()
                .filter(e -> e.getKey() != Gem.GOLD && e.getValue() > 0).count();
            canTake3 = pilesWithGems >= 3 && player.getTotalTokenCount() <= 7;
            canTake2 = b.getGemBank().values().stream().anyMatch(count -> count >= 4) && player.getTotalTokenCount() <= 8;
            canReserve = player.getReservedCards().size() < 3;
            canBuy = true; // Simplified check
            canBuyReserved = !player.getReservedCards().isEmpty();
        }

        displayAvailableMoves(player, game);
        
        while (true) {
            try {
                System.out.print("\nEnter your move (number): ");
                final String input = scanner.nextLine().trim();
                
                switch (input) {
                    case "1":
                        if (!canTake3) throw new IllegalArgumentException("Move unavailable.");
                        return promptForTake3Different();
                    case "2":
                        if (!canTake2) throw new IllegalArgumentException("Move unavailable.");
                        return promptForTake2Same();
                    case "3":
                        if (!canReserve) throw new IllegalArgumentException("Move unavailable.");
                        return promptForReserveCard();
                    case "4":
                        if (!canBuy) throw new IllegalArgumentException("Move unavailable.");
                        return promptForBuyCard(false, player);
                    case "5":
                        if (!canBuyReserved) throw new IllegalArgumentException("Move unavailable.");
                        return promptForBuyCard(true, player);
                    case "6":
                        return new Move(MoveType.DISCARD_TOKENS);
                    default:
                         if (input.contains(" ")) {
                              return parseMoveInput(input);
                         }
                         System.out.println(Colors.colorize("Invalid selection. Please enter 1-6.", Colors.RED));
                }
            } catch (final IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    // ... (Keep existing methods) ...
    // Deleted duplicate methods to fix compilation error.


    // ... (Keep existing promptForTokenDiscard) ...

    
    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        System.out.println("\nYou have too many tokens! You must discard " + excessCount + " tokens.");
        displayPlayerTokens(player);
        
        final Map<Gem, Integer> tokensToDiscard = new HashMap<>();
        int remainingToDiscard = excessCount;
        
        while (remainingToDiscard > 0) {
            try {
                System.out.print("Enter gem type and quantity to discard (e.g., 'RED 2'): ");
                final String input = scanner.nextLine().trim();
                final String[] parts = input.split(" ");
                
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid format. Use: GEM_TYPE QUANTITY");
                }
                
                final Gem gem = parseGemType(parts[0]);
                final int quantity = Integer.parseInt(parts[1]);
                
                if (quantity <= 0 || quantity > remainingToDiscard) {
                    throw new IllegalArgumentException("Invalid quantity. Must be between 1 and " + remainingToDiscard);
                }
                
                if (player.getTokenCount(gem) < quantity) {
                    throw new IllegalArgumentException("You don't have enough " + gem + " tokens");
                }
                
                tokensToDiscard.merge(gem, quantity, Integer::sum);
                remainingToDiscard -= quantity;
                
                if (remainingToDiscard > 0) {
                    System.out.println("You still need to discard " + remainingToDiscard + " tokens.");
                }
                
            } catch (final Exception e) {
                displayError(e.getMessage());
            }
        }
        
        return new Move(MoveType.DISCARD_TOKENS, tokensToDiscard);
    }
    
    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("GAME OVER!");
        System.out.println("=".repeat(50));
        System.out.println("WINNER: " + winner.getName() + " with " + winner.getTotalPoints() + " points!");
        System.out.println("\nFinal Scores:");
        
        finalScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> System.out.printf("  %s: %d points%n", entry.getKey(), entry.getValue()));
        
        System.out.println("=".repeat(50));
    }
    
    @Override
    public void clearDisplay() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    @Override
    public void displayAvailableMoves(final Player player, final Game game) {
        // Validation Logic (Simulated)
        boolean canTake3 = false;
        boolean canTake2 = false;
        boolean canReserve = false;
        boolean canBuy = false;
        boolean canBuyReserved = false;

        if (game != null) {
            Board b = game.getBoard();
            // Check Take 3: Need >=3 piles with >0 gems
            long pilesWithGems = b.getGemBank().entrySet().stream()
                .filter(e -> e.getKey() != Gem.GOLD && e.getValue() > 0)
                .count();
            canTake3 = pilesWithGems >= 3 && player.getTotalTokenCount() <= 7; // Approx check

            // Check Take 2: Need >=1 pile with >=4 gems
            canTake2 = b.getGemBank().values().stream().anyMatch(count -> count >= 4) && player.getTotalTokenCount() <= 8;

            // Check Reserve: Hand < 3 and (cards available or decks not empty)
            canReserve = player.getReservedCards().size() < 3; // Simplified

            // Check Buy: Can afford any visible card?
            // This is complex, but let's just check if board has cards. 
            // Real validation happens in Controller. Here we just enable the menu item if it's plausible.
            canBuy = true; 
            canBuyReserved = !player.getReservedCards().isEmpty();
        }

        System.out.println("\nAvailable Moves:");
        printOption(1, "Take 3 different gems", canTake3);
        printOption(2, "Take 2 same gems", canTake2);
        printOption(3, "Reserve a card", canReserve);
        printOption(4, "Buy a card", canBuy);
        printOption(5, "Buy a reserved card", canBuyReserved);
        
        if (player.getTotalTokenCount() > 10) {
            System.out.println(Colors.colorize("6. Discard tokens (REQUIRED - limit exceeded)", Colors.RED));
        }
    }

    private void printOption(int num, String text, boolean enabled) {
        if (enabled) {
            System.out.println(num + ". " + text);
        } else {
            // Dark Gray for disabled
            System.out.println(Colors.colorize(num + ". " + text + " (Unavailable)", Colors.BLACK));
        }
    }
    
    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        while (true) {
            try {
                System.out.print("Enter name for Player " + playerNumber + ": ");
                final String name = scanner.nextLine().trim();
                
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Player name cannot be empty");
                }
                
                if (name.length() > 20) {
                    throw new IllegalArgumentException("Player name too long (max 20 characters)");
                }
                
                return name;
            } catch (final IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }
    
    @Override
    public int promptForPlayerCount() {
        while (true) {
            try {
                System.out.print("Enter number of players (2-4): ");
                final String input = scanner.nextLine().trim();
                final int count = Integer.parseInt(input);
                
                if (count < 2 || count > 4) {
                    throw new IllegalArgumentException("Player count must be between 2 and 4");
                }
                
                return count;
            } catch (final NumberFormatException e) {
                displayError("Please enter a valid number");
            } catch (final IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }
    
    @Override
    public void displayWelcomeMessage() {
        System.out.println("Welcome to Splendor!");
        System.out.println("A game of gem collecting and card development.");
        System.out.println("May the best merchant win!\n");
    }
    
    @Override
    public void close() {
        scanner.close();
    }
    
    /**
     * Displays the game header.
     */
    private void displayHeader() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                SPLENDOR                        ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
    
    /**
     * Displays the current board state.
     * 
     * @param board Game board
     */
    private void displayBoard(final Board board) {
        visibleCardMap.clear(); // Reset map for this frame
        System.out.println("\n--- Board State ---");
        displayGemBank(board.getGemBank());
        displayAvailableCards(board.getAvailableCards());
        displayAvailableNobles(board.getAvailableNobles());
    }
    
    /**
     * Displays the gem bank state.
     * 
     * @param gemBank Current gem counts
     */
    private void displayGemBank(final Map<Gem, Integer> gemBank) {
        System.out.print("Gems Available: ");
        gemBank.entrySet().stream()
            .filter(entry -> entry.getKey() != Gem.GOLD)
            .forEach(entry -> System.out.printf("%s: %d | ", 
                Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())), 
                entry.getValue()));
        System.out.printf("%s: %d%n", Colors.colorize("GOLD", Colors.GOLD), gemBank.get(Gem.GOLD));
    }
    
    /**
     * Displays available cards for each tier in a grid/row layout.
     * 
     * @param availableCards Map of tier to available cards
     */
    private void displayAvailableCards(final Map<Integer, List<Card>> availableCards) {
        int displayIndexCounter = 1;
        
        for (int tier = 3; tier >= 1; tier--) {
            final List<Card> cards = availableCards.get(tier);
            if (cards != null && !cards.isEmpty()) {
                System.out.println("\n[Level " + tier + " Cards]");
                
                // Prepare lines for side-by-side display
                String[] lines = new String[5]; // 5 lines per card
                Arrays.fill(lines, "");
                
                for (Card card : cards) {
                    visibleCardMap.put(displayIndexCounter, card.getId());
                    String[] cardLines = formatCardAscii(card, displayIndexCounter).split("\n");
                    for (int j = 0; j < 5; j++) {
                        lines[j] += cardLines[j] + "  "; // Add spacing
                    }
                    displayIndexCounter++;
                }
                
                // Print the assembled lines
                for (String line : lines) {
                    System.out.println(line);
                }
            }
        }
    }

    private String formatCardAscii(Card card, int index) {
        String bonusColor = Colors.getGemColor(card.getBonusGem());
        String bonusChar = card.getBonusGem().toString().substring(0, 1);
        String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : " ";
        
        // Format cost string compactly
        StringBuilder costSb = new StringBuilder();
        card.getCost().forEach((g, c) -> 
            costSb.append(Colors.colorize(g.toString().substring(0, 1), Colors.getGemColor(g)))
                  .append(c).append(" "));
        
        // Ensure cost string padding is calculated correctly without ANSI codes for width
        // This is tricky in Java console. We'll use a fixed width layout.
        
        //  ┌────────────┐
        //  │ #1   (R)   │
        //  │ Pts: 4     │
        //  │ Cost:      │
        //  │ R3 G2 B1   │
        //  └────────────┘
        
        return String.format(
            "┌────────────┐\n" +
            "│ #%-2d   %s%s%s   │\n" +
            "│ Pts: %-2s    │\n" +
            "│ Cost:      │\n" +
            "│ %-18s │\n" + // Wide padding to account for invisible ANSI codes
            "└────────────┘",
            index, 
            bonusColor, "(" + bonusChar + ")", Colors.RESET,
            points,
            costSb.toString()
        );
    }
    
    /**
     * Displays available nobles.
     * 
     * @param nobles Available nobles
     */
    private void displayAvailableNobles(final List<Noble> nobles) {
        if (!nobles.isEmpty()) {
            System.out.println(Colors.colorize("\n[Available Nobles]", Colors.PURPLE));
            for (int i = 0; i < nobles.size(); i++) {
                final Noble noble = nobles.get(i);
                System.out.printf("%d. Noble %d [%d pts] Requirements: %s%n",
                    i + 1, noble.getId(), noble.getPoints(), formatRequirements(noble.getRequirements()));
            }
        }
    }

    private String formatRequirements(Map<Gem, Integer> reqs) {
        StringBuilder sb = new StringBuilder();
        reqs.forEach((gem, count) -> 
            sb.append(Colors.colorize(gem.toString(), Colors.getGemColor(gem)))
              .append(":").append(count).append(" "));
        return sb.toString().trim();
    }
    
    /**
     * Displays all players and their current state.
     * 
     * @param players List of players
     */
    private void displayPlayers(final List<Player> players) {
        System.out.println("\n--- Players ---");
        for (final Player player : players) {
            displayPlayerSummary(player);
        }
    }
    
    /**
     * Displays the current player with emphasis.
     * 
     * @param currentPlayer Current player
     */
    private void displayCurrentPlayer(final Player currentPlayer) {
        System.out.println("\n>>> CURRENT TURN: " + Colors.colorize(currentPlayer.getName(), Colors.CYAN) + " <<<");
        displayPlayerDetails(currentPlayer);
    }
    
    /**
     * Displays a brief player summary.
     * 
     * @param player Player to display
     */
    private void displayPlayerSummary(final Player player) {
        System.out.printf("%s: %d pts | Tokens: %d | Cards: %d | Reserved: %d | Nobles: %d%n",
            Colors.colorize(player.getName(), Colors.CYAN), player.getTotalPoints(), player.getTotalTokenCount(),
            player.getPurchasedCards().size(), player.getReservedCards().size(),
            player.getNobles().size());
    }
    
    /**
     * Displays detailed player information.
     * 
     * @param player Player to display
     */
    private void displayPlayerDetails(final Player player) {
        System.out.println("Points: " + player.getTotalPoints());
        displayPlayerTokens(player);
        displayPlayerDiscounts(player);
        
        if (!player.getReservedCards().isEmpty()) {
            System.out.println("Reserved Cards:");
            for (int i = 0; i < player.getReservedCards().size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, formatCard(player.getReservedCards().get(i)));
            }
        }
    }
    
    /**
     * Displays player's token inventory.
     * 
     * @param player Player whose tokens to display
     */
    private void displayPlayerTokens(final Player player) {
        System.out.print("Tokens: ");
        player.getTokens().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .forEach(entry -> System.out.printf("%s: %d | ", 
                Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())), 
                entry.getValue()));
        System.out.println();
    }
    
    /**
     * Displays player's gem discounts from purchased cards.
     * 
     * @param player Player whose discounts to display
     */
    private void displayPlayerDiscounts(final Player player) {
        System.out.print("Discounts: ");
        player.getGemDiscounts().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .forEach(entry -> System.out.printf("%s: %d | ", 
                Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())), 
                entry.getValue()));
        System.out.println();
    }
    
    /**
     * Formats a card for display (Used for Reserved Cards / Text Fallback).
     * 
     * @param card Card to format
     * @return Formatted card string
     */
    private String formatCard(final Card card) {
        return String.format("[%d pts, Bonus: %s, Cost: %s]",
            card.getPoints(), 
            Colors.colorize(card.getBonusGem().toString(), Colors.getGemColor(card.getBonusGem())), 
            formatRequirements(card.getCost()));
    }
    
    /**
     * Parses move input from user.
     * 
     * @param input User input
     * @return Parsed move
     * @throws IllegalArgumentException if input is invalid
     */
    private Move parseMoveInput(final String input) throws IllegalArgumentException {
        // Fallback for command-line style input (e.g. "2 RED")
        String[] parts = input.split("\\s+");
        String command = parts[0];
        
        switch (command) {
            case "1": // Take 3 diff: 1 RED GREEN BLUE
                 if (parts.length != 4) throw new IllegalArgumentException("Usage: 1 GEM1 GEM2 GEM3");
                 Map<Gem, Integer> gems = new HashMap<>();
                 gems.put(parseGemType(parts[1]), 1);
                 gems.put(parseGemType(parts[2]), 1);
                 gems.put(parseGemType(parts[3]), 1);
                 return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
            case "2": // Take 2 same: 2 RED
                 if (parts.length != 2) throw new IllegalArgumentException("Usage: 2 GEM");
                 Map<Gem, Integer> twoGems = new HashMap<>();
                 twoGems.put(parseGemType(parts[1]), 2);
                 return new Move(MoveType.TAKE_TWO_SAME, twoGems);
            case "3": // Reserve: 3 <DisplayIndex>
                 if (parts.length != 2) throw new IllegalArgumentException("Usage: 3 CARD_NUM");
                 int rIdx = Integer.parseInt(parts[1]);
                 if (!visibleCardMap.containsKey(rIdx)) throw new IllegalArgumentException("Invalid card number");
                 return new Move(MoveType.RESERVE_CARD, visibleCardMap.get(rIdx), false);
            case "4": // Buy: 4 <DisplayIndex>
                 if (parts.length != 2) throw new IllegalArgumentException("Usage: 4 CARD_NUM");
                 int bIdx = Integer.parseInt(parts[1]);
                 if (!visibleCardMap.containsKey(bIdx)) throw new IllegalArgumentException("Invalid card number");
                 return new Move(MoveType.BUY_CARD, visibleCardMap.get(bIdx), false);
            default:
                throw new IllegalArgumentException("Unknown command. Use interactive mode by typing just the number.");
        }
    }
    
    private Move promptForTake3Different() {
        System.out.println("Select 3 different gems (e.g. RED GREEN BLUE):");
        System.out.print("> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("You must select exactly 3 gems.");
        }
        
        Map<Gem, Integer> gems = new HashMap<>();
        for (String p : parts) {
            Gem g = parseGemType(p);
            if (g == Gem.GOLD) throw new IllegalArgumentException("Cannot take GOLD tokens directly.");
            if (gems.containsKey(g)) throw new IllegalArgumentException("Gems must be different.");
            gems.put(g, 1);
        }
        return new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
    }

    private Move promptForTake2Same() {
        System.out.println("Select gem color (e.g. RED):");
        System.out.print("> ");
        String input = scanner.nextLine().trim();
        Gem g = parseGemType(input);
        if (g == Gem.GOLD) throw new IllegalArgumentException("Cannot take GOLD tokens directly.");
        
        Map<Gem, Integer> gems = new HashMap<>();
        gems.put(g, 2);
        return new Move(MoveType.TAKE_TWO_SAME, gems);
    }

    private Move promptForReserveCard() {
        System.out.println("Enter the Card Number to reserve (see board):");
        System.out.print("> ");
        int displayIndex = Integer.parseInt(scanner.nextLine().trim());
        
        if (!visibleCardMap.containsKey(displayIndex)) {
            throw new IllegalArgumentException("Invalid card number.");
        }
        return new Move(MoveType.RESERVE_CARD, visibleCardMap.get(displayIndex), false);
    }

    private Move promptForBuyCard(boolean isReserved, Player player) {
         if (isReserved) {
              System.out.println("Select a reserved card to buy:");
              List<Card> reserved = player.getReservedCards();
              for (int i = 0; i < reserved.size(); i++) {
                  // Re-use ASCII format for detail
                  System.out.println(formatCardAscii(reserved.get(i), i + 1));
              }
              System.out.print("> Enter Index (1-" + reserved.size() + "): ");
              
              int index = Integer.parseInt(scanner.nextLine().trim());
              if (index < 1 || index > reserved.size()) {
                  throw new IllegalArgumentException("Invalid index.");
              }
              return new Move(MoveType.BUY_CARD, index - 1, true);
         } else {
              System.out.println("Enter the Card Number to buy (see board):");
              System.out.print("> ");
              int displayIndex = Integer.parseInt(scanner.nextLine().trim());
              if (!visibleCardMap.containsKey(displayIndex)) {
                  throw new IllegalArgumentException("Invalid card number.");
              }
              return new Move(MoveType.BUY_CARD, visibleCardMap.get(displayIndex), false);
         }
    }
    
    /**
     * Parses gem type from string.
     * 
     * @param gemString Gem type string
     * @return Parsed gem
     * @throws IllegalArgumentException if gem type is invalid
     */
    private Gem parseGemType(final String gemString) throws IllegalArgumentException {
        try {
            return Gem.valueOf(gemString.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gem type: " + gemString);
        }
    }
}