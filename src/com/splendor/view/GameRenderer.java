package com.splendor.view;

import com.splendor.model.*;
import java.util.*;

/**
 * Handles the rendering of game components to the terminal.
 * Separates visualization logic from input handling.
 */
public class GameRenderer {

    private final Map<Integer, Integer> visibleCardMap;

    public GameRenderer(Map<Integer, Integer> visibleCardMap) {
        this.visibleCardMap = visibleCardMap;
    }

    /**
     * Clears the terminal display.
     * Uses ANSI escape codes for modern terminals and attempts system-specific
     * commands (cls/clear) for Windows/Unix compatibility.
     */
    public void clearDisplay() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback to ANSI if system command fails
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    /**
     * Renders the game header banner.
     */
    public void displayHeader() {
        System.out.println(Colors.colorize("╔══════════════════════════════════════════════════════════════╗", Colors.GOLD));
        System.out.println(Colors.colorize("║                           SPLENDOR                           ║", Colors.GOLD));
        System.out.println(Colors.colorize("╚══════════════════════════════════════════════════════════════╝", Colors.GOLD));
    }

    /**
     * Displays the complete game board including gem bank, nobles, and cards.
     * 
     * @param board The game board state
     * @param currentPlayer The player whose turn it is (for affordability checks)
     */
    public void displayBoard(final Board board, final Player currentPlayer) {
        visibleCardMap.clear();
        
        // Gem Bank Box
        System.out.println("\n" + Colors.colorize("┌─ GEM BANK ───────────────────────────────────────────────────┐", Colors.WHITE));
        System.out.print("│ ");
        for (Gem gem : Gem.values()) {
            String color = Colors.getGemColor(gem);
            System.out.printf("%s: %-2d  ", Colors.colorize("[" + gem.toString().substring(0, 3) + "]", color), board.getGemCount(gem));
        }
        System.out.println("│");
        System.out.println(Colors.colorize("└──────────────────────────────────────────────────────────────┘", Colors.WHITE));

        // Nobles
        displayAvailableNobles(board.getAvailableNobles());
        
        // Cards
        displayAvailableCards(board.getAvailableCards(), currentPlayer);
    }

    /**
     * Displays available cards for purchase, grouped by tier.
     * Cards are displayed in rows to fit standard terminal widths.
     * 
     * @param availableCards Map of card tiers to lists of available cards
     * @param currentPlayer The current player (to determine affordability highlighting)
     */
    private void displayAvailableCards(final Map<Integer, List<Card>> availableCards, final Player currentPlayer) {
        int displayIndexCounter = 1;
        
        for (int tier = 3; tier >= 1; tier--) {
            final List<Card> cards = availableCards.get(tier);
            if (cards != null && !cards.isEmpty()) {
                System.out.println("\n" + Colors.colorize("=== LEVEL " + tier + " ===", Colors.WHITE));
                
                // Group cards into chunks of 4 to prevent wrapping issues on smaller terminals
                for (int i = 0; i < cards.size(); i += 4) {
                    List<Card> rowCards = cards.subList(i, Math.min(i + 4, cards.size()));
                    
                    String[] lines = new String[5];
                    Arrays.fill(lines, "");
                    
                    for (Card card : rowCards) {
                        visibleCardMap.put(displayIndexCounter, card.getId());
                        boolean canAfford = canPlayerAfford(currentPlayer, card);
                        String[] cardLines = formatCardAscii(card, displayIndexCounter, canAfford).split("\n");
                        for (int j = 0; j < 5; j++) {
                            lines[j] += cardLines[j] + "  ";
                        }
                        displayIndexCounter++;
                    }
                    
                    for (String line : lines) {
                        System.out.println(line);
                    }
                    // Add a small spacer between rows if there are multiple rows for one tier
                    if (i + 4 < cards.size()) System.out.println();
                }
            }
        }
    }

    /**
     * Checks if a player can afford a specific card.
     * considers player's current tokens, permanent gem discounts, and gold (wild) tokens.
     * 
     * @param player The player attempting to purchase
     * @param card The card to purchase
     * @return true if the player has sufficient resources, false otherwise
     */
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

    public String formatCardAscii(Card card, int index, boolean canAfford) {
        String bonusColor = Colors.getGemColor(card.getBonusGem());
        String bonusChar = card.getBonusGem().toString().substring(0, 1);
        String points = card.getPoints() > 0 ? String.valueOf(card.getPoints()) : " ";
        
        StringBuilder costSb = new StringBuilder();
        card.getCost().forEach((g, c) -> 
            costSb.append(Colors.colorize(g.toString().substring(0, 1), Colors.getGemColor(g)))
                  .append(c).append(" "));
        
        String borderColor = canAfford ? Colors.WHITE : Colors.GRAY;
        
        return String.format(
            Colors.colorize("┌────────────┐", borderColor) + "\n" +
            Colors.colorize("│", borderColor) + " #%-2d   %s%s%s   " + Colors.colorize("│", borderColor) + "\n" +
            Colors.colorize("│", borderColor) + " Pts: %-2s    " + Colors.colorize("│", borderColor) + "\n" +
            Colors.colorize("│", borderColor) + " Cost:      " + Colors.colorize("│", borderColor) + "\n" +
            Colors.colorize("│", borderColor) + " %-18s " + Colors.colorize("│", borderColor) + "\n" +
            Colors.colorize("└────────────┘", borderColor),
            index, 
            bonusColor, "(" + bonusChar + ")", Colors.RESET,
            points,
            costSb.toString()
        );
    }

    /**
     * Displays the list of available nobles.
     * 
     * @param nobles List of available noble tiles
     */
    private void displayAvailableNobles(final List<Noble> nobles) {
        if (!nobles.isEmpty()) {
            System.out.println("\n" + Colors.colorize("=== NOBLES ===", Colors.PURPLE));
            for (int i = 0; i < nobles.size(); i++) {
                final Noble noble = nobles.get(i);
                System.out.printf(" %d. [3 pts] Reqs: %s%n",
                    i + 1, noble.getId(), formatRequirements(noble.getRequirements()));
            }
        }
    }

    private String formatRequirements(Map<Gem, Integer> reqs) {
        StringBuilder sb = new StringBuilder();
        reqs.forEach((gem, count) -> 
            sb.append(Colors.colorize(gem.toString().substring(0,3), Colors.getGemColor(gem)))
              .append(":").append(count).append(" "));
        return sb.toString().trim();
    }

    public void displayPlayers(final List<Player> players) {
        System.out.println("\n" + Colors.colorize("--- OPPONENTS ---", Colors.WHITE));
        for (final Player player : players) {
            displayPlayerSummary(player);
        }
    }

    private void displayPlayerSummary(final Player player) {
        System.out.printf("%s: %d pts | Res: %d | ",
            Colors.colorize(player.getName(), Colors.CYAN), player.getTotalPoints(),
            player.getReservedCards().size());
            
        // Compact token display
        System.out.print("Tok: ");
        boolean hasTokens = false;
        for (Map.Entry<Gem, Integer> entry : player.getTokens().entrySet()) {
            if (entry.getValue() > 0) {
                hasTokens = true;
                System.out.printf("%s%d ", Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())), entry.getValue());
            }
        }
        if (!hasTokens) System.out.print("- ");
        
        // Compact bonus display
        System.out.print("| Bon: ");
        boolean hasBonuses = false;
        for (Map.Entry<Gem, Integer> entry : player.getGemDiscounts().entrySet()) {
            if (entry.getValue() > 0) {
                hasBonuses = true;
                System.out.printf("%s%d ", Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())), entry.getValue());
            }
        }
        if (!hasBonuses) System.out.print("-");
        System.out.println();
    }

    /**
     * Displays the status bar for the current player.
     * Shows detailed stats including points, specific discounts, and token inventory.
     * 
     * @param currentPlayer The player whose turn it is
     */
    public void displayStatus(final Player currentPlayer) {
        System.out.println("\n" + Colors.colorize("╔════════════════════ STATUS BAR ════════════════════╗", Colors.CYAN));
        System.out.printf("║ PLAYER: %-15s POINTS: %-17d ║%n", 
            currentPlayer.getName(), currentPlayer.getTotalPoints());
        
        System.out.print("║ DISCOUNTS: ");
        StringBuilder discSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getGemDiscounts().entrySet()) {
             if (entry.getValue() > 0) {
                 discSb.append(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                       .append(":")
                       .append(entry.getValue())
                       .append(" ");
             }
        }
        if (discSb.length() == 0) discSb.append("None");
        System.out.print(discSb.toString());
        System.out.println(Colors.colorize("", Colors.RESET)); 
        
        System.out.print("║ TOKENS:    ");
        StringBuilder tokSb = new StringBuilder();
        for (Map.Entry<Gem, Integer> entry : currentPlayer.getTokens().entrySet()) {
             if (entry.getValue() > 0) {
                 tokSb.append(Colors.colorize(entry.getKey().toString().substring(0, 1), Colors.getGemColor(entry.getKey())))
                      .append(":")
                      .append(entry.getValue())
                      .append(" ");
             }
        }
        if (tokSb.length() == 0) tokSb.append("None");
        System.out.println(tokSb.toString());
        System.out.println(Colors.colorize("╚════════════════════════════════════════════════════╝", Colors.CYAN));
    }

    public void displayPlayerTokens(final Player player) {
        player.getTokens().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .forEach(entry -> System.out.printf("%s: %d | ", 
                Colors.colorize(entry.getKey().toString(), Colors.getGemColor(entry.getKey())), 
                entry.getValue()));
        System.out.println();
    }
}
