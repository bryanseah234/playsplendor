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
    private final GameRenderer renderer;
    
    /**
     * Creates a new ConsoleView with default input handling.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.inputResolver = new InputResolver();
        this.renderer = new GameRenderer();
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
    public void displayMessage(final String message) {
        System.out.println(Colors.colorize(message, Colors.GREEN));
    }
    
    @Override
    public void displayError(final String errorMessage) {
        System.err.println(Colors.colorize("ERROR: " + errorMessage, Colors.RED));
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
    
    @Override
    public String promptForCommand(final Player player, final Game game) {
        displayAvailableMoves(player, game);
        return inputResolver.promptForString("Command > ", 1, 60);
    }

    @Override
    public Move promptForMove(final Player player, final Game game) {
        while (true) {
            final String command = promptForCommand(player, game).trim();
            if (command.isEmpty()) {
                displayError("Command cannot be empty");
                continue;
            }
            final String[] parts = command.split("\\s+");
            final String action = parts[0].toLowerCase();

            try {
                switch (action) {
                    case "help":
                        displayAvailableMoves(player, game);
                        continue;
                    case "take":
                        return parseTakeMove(parts);
                    case "buy":
                        return parseBuyMove(parts);
                    case "reserve":
                        return parseReserveMove(parts);
                    default:
                        displayError("Unknown command: " + action);
                }
            } catch (final IllegalArgumentException e) {
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
        System.out.println("\n" + Colors.colorize("--- COMMANDS ---", Colors.WHITE));
        System.out.println(Colors.colorize("take R G B", Colors.WHITE) + " | " + Colors.colorize("take 2 R", Colors.WHITE));
        System.out.println(Colors.colorize("buy 12", Colors.WHITE) + " | " + Colors.colorize("reserve 12", Colors.WHITE));
        System.out.println(Colors.colorize("buy reserved 12", Colors.WHITE) + " | " + Colors.colorize("help", Colors.WHITE));
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
        throw new IllegalArgumentException("Invalid reserve command format");
    }

    private Gem parseGem(final String token) {
        final String normalized = token.trim().toUpperCase();
        return switch (normalized) {
            case "W", "WHITE" -> Gem.WHITE;
            case "B", "BLUE" -> Gem.BLUE;
            case "G", "GREEN" -> Gem.GREEN;
            case "R", "RED" -> Gem.RED;
            case "K", "BLACK" -> Gem.BLACK;
            case "AU", "GOLD" -> Gem.GOLD;
            default -> throw new IllegalArgumentException("Unknown gem: " + token);
        };
    }
}
