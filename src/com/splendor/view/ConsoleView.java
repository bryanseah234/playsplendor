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
import com.splendor.util.InputResolver;
import java.util.*;

/**
 * Console-based implementation of IGameView.
 * Provides text-based display and user input handling.
 */
public class ConsoleView implements IGameView {
    
    private final Scanner scanner;
    private final InputResolver inputResolver;
    private final Map<Integer, Integer> visibleCardMap; 
    private final GameRenderer renderer;
    
    /**
     * Creates a new ConsoleView with default input handling.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.inputResolver = new InputResolver();
        this.visibleCardMap = new HashMap<>();
        this.renderer = new GameRenderer(this.visibleCardMap);
    }
    
    @Override
    public void displayGameState(final Game game) {
        renderer.clearDisplay();
        renderer.displayHeader();
        renderer.displayBoard(game.getBoard(), game.getCurrentPlayer());
        renderer.displayPlayers(game.getPlayers());
        renderer.displayStatus(game.getCurrentPlayer());
    }
    
    @Override
    public void displayPlayerTurn(final Player player) {
        // Handled by displayStatus now
    }
    
    @Override
    public void displayMessage(final String message) {
        System.out.println(Colors.colorize(message, Colors.GREEN));
    }
    
    @Override
    public void displayError(final String errorMessage) {
        System.err.println(Colors.colorize("ERROR: " + errorMessage, Colors.RED));
        // Small pause so user can see the error
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
    }
    
    @Override
    public Move promptForMove(final Player player, final Game game) {
        // Pre-calculate availability
        boolean canTake3 = false;
        boolean canTake2 = false;
        boolean canReserve = false;
        boolean canBuy = false;
        boolean canBuyReserved = false;

        if (game != null) {
            Board b = game.getBoard();
            long pilesWithGems = b.getGemBank().entrySet().stream()
                .filter(e -> e.getKey() != Gem.GOLD && e.getValue() > 0).count();
            
            // Rule: Cannot take tokens if already at 10 (though game allows taking then discarding, 
            // usually UI restricts taking if it's futile, but let's stick to standard rules: 
            // you CAN take and then discard. However, user requested: "If a player has 10 tokens, the 'Take Tokens' option should be grayed out")
            boolean tokenLimitReached = player.getTotalTokenCount() >= 10;
            
            canTake3 = !tokenLimitReached && pilesWithGems >= 3;
            canTake2 = !tokenLimitReached && b.getGemBank().values().stream().anyMatch(count -> count >= 4);
            canReserve = player.getReservedCards().size() < 3;
            canBuy = true; // Always show, validated later
            canBuyReserved = !player.getReservedCards().isEmpty();
        }

        displayAvailableMoves(canTake3, canTake2, canReserve, canBuy, canBuyReserved, player);
        
        while (true) {
            int choice = inputResolver.promptForInt("\nEnter your move (number): ", 1, 6);
            
            try {
                switch (choice) {
                    case 1:
                        if (!canTake3) throw new IllegalArgumentException("Move unavailable.");
                        return promptForTake3Different();
                    case 2:
                        if (!canTake2) throw new IllegalArgumentException("Move unavailable.");
                        return promptForTake2Same();
                    case 3:
                        if (!canReserve) throw new IllegalArgumentException("Move unavailable.");
                        return promptForReserveCard();
                    case 4:
                        if (!canBuy) throw new IllegalArgumentException("Move unavailable.");
                        return promptForBuyCard(false, player);
                    case 5:
                        if (!canBuyReserved) throw new IllegalArgumentException("Move unavailable.");
                        return promptForBuyCard(true, player);
                    case 6:
                        // Only useful if over limit, but usually triggered automatically
                        return new Move(MoveType.DISCARD_TOKENS);
                }
            } catch (IllegalArgumentException e) {
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
            System.out.println("Format: GEM_NAME QUANTITY (e.g., RED 1)");
            String input = inputResolver.promptForString("> ", 3, 20).toUpperCase();
            
            try {
                String[] parts = input.split("\\s+");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid format.");
                
                Gem gem = Gem.valueOf(parts[0]);
                int qty = Integer.parseInt(parts[1]);
                
                if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive.");
                if (qty > remainingToDiscard) throw new IllegalArgumentException("Quantity exceeds required discard.");
                if (player.getTokenCount(gem) < qty) throw new IllegalArgumentException("Not enough tokens of that type.");
                
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
        System.out.println("WINNER: " + Colors.colorize(winner.getName(), Colors.CYAN) + " with " + winner.getTotalPoints() + " points!");
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
    public void displayAvailableMoves(Player player, Game game) {
        // Not used directly, replaced by overloaded method
    }
    
    private void displayAvailableMoves(boolean canTake3, boolean canTake2, boolean canReserve, boolean canBuy, boolean canBuyReserved, Player player) {
        System.out.println("\n" + Colors.colorize("--- AVAILABLE ACTIONS ---", Colors.WHITE));
        printOption(1, "Take 3 different gems", canTake3);
        printOption(2, "Take 2 same gems", canTake2);
        printOption(3, "Reserve a card", canReserve);
        printOption(4, "Buy a card", canBuy);
        printOption(5, "Buy a reserved card", canBuyReserved);
        
        if (player.getTotalTokenCount() > 10) {
            System.out.println(Colors.colorize("6. Discard tokens (REQUIRED)", Colors.RED));
        }
    }

    private void printOption(int num, String text, boolean enabled) {
        if (enabled) {
            System.out.println(Colors.colorize(num + ". " + text, Colors.WHITE));
        } else {
            System.out.println(Colors.colorize(num + ". " + text + " (Unavailable)", Colors.GRAY));
        }
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
    
    private Move promptForTake3Different() {
        System.out.println("Select 3 different gems (e.g. RED GREEN BLUE):");
        String input = inputResolver.promptForString("> ", 5, 50);
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
        String input = inputResolver.promptForString("> ", 3, 10);
        Gem g = parseGemType(input);
        if (g == Gem.GOLD) throw new IllegalArgumentException("Cannot take GOLD tokens directly.");
        
        Map<Gem, Integer> gems = new HashMap<>();
        gems.put(g, 2);
        return new Move(MoveType.TAKE_TWO_SAME, gems);
    }

    private Move promptForReserveCard() {
        int displayIndex = inputResolver.promptForInt("Enter the Card Number to reserve: ", 1, 20); // Arbitrary max
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
                  boolean canAfford = canPlayerAfford(player, reserved.get(i));
                  System.out.println(renderer.formatCardAscii(reserved.get(i), i + 1, canAfford));
              }
              int index = inputResolver.promptForInt("Enter Index (1-" + reserved.size() + "): ", 1, reserved.size());
              return new Move(MoveType.BUY_CARD, index - 1, true);
         } else {
              int displayIndex = inputResolver.promptForInt("Enter the Card Number to buy: ", 1, 20);
              if (!visibleCardMap.containsKey(displayIndex)) {
                  throw new IllegalArgumentException("Invalid card number.");
              }
              return new Move(MoveType.BUY_CARD, visibleCardMap.get(displayIndex), false);
         }
    }
    
    private boolean canPlayerAfford(Player player, Card card) {
        Map<Gem, Integer> discounts = player.getGemDiscounts();
        Map<Gem, Integer> tokens = player.getTokens();
        int goldTokens = tokens.getOrDefault(Gem.GOLD, 0);
        
        for (Map.Entry<Gem, Integer> costEntry : card.getCost().entrySet()) {
            Gem gem = costEntry.getKey();
            int cost = costEntry.getValue();
            int discount = discounts.getOrDefault(gem, 0);
            int available = tokens.getOrDefault(gem, 0);
            
            int needed = Math.max(0, cost - discount);
            int remaining = Math.max(0, needed - available);
            
            if (remaining > 0) {
                if (goldTokens >= remaining) {
                    goldTokens -= remaining;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private Gem parseGemType(final String gemString) {
        try {
            return Gem.valueOf(gemString.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gem type: " + gemString);
        }
    }
}
