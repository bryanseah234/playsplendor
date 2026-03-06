/**
 * Represents the complete game state including players, board, and game flow.
 * Central model class that coordinates all game components and enforces rules.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented core game state management
 */
package com.splendor.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete game state and coordinates all game components.
 * Manages players, the game board, turn order, and win conditions.
 */
public class Game {
    
    private final Deque<String> recentMoves;
    private final Deque<GameSnapshot> undoHistory;

    private final List<Player> players;
    private final Board board;
    private final int winningPoints;
    private final int maxTokens;
    private GameState currentState;
    private int currentPlayerIndex;
    private Player winner;
    private boolean isFinalRound;
    private int finalRoundPlayerIndex;
    
    
    private static class GameSnapshot {
        final List<PlayerSnapshot> playerSnapshots;
        final BoardSnapshot boardSnapshot;
        final int currentPlayerIndex;
        final GameState currentState;
        final boolean isFinalRound;
        final int finalRoundPlayerIndex;

        final List<String> recentMoves;

        GameSnapshot(Game game) {
            this.playerSnapshots = new ArrayList<>();
            for (Player p : game.getPlayers()) {
                this.playerSnapshots.add(new PlayerSnapshot(p));
            }
            this.boardSnapshot = new BoardSnapshot(game.getBoard());
            this.currentPlayerIndex = game.currentPlayerIndex;
            this.currentState = new GameState(game.currentState.getPhase());
            this.isFinalRound = game.isFinalRound;
            this.finalRoundPlayerIndex = game.finalRoundPlayerIndex;
            this.recentMoves = new ArrayList<>(game.recentMoves);
        }
    }

    private static class PlayerSnapshot {
        final String name;
        final Map<Gem, Integer> tokens;
        final List<Card> purchasedCards;
        final List<Card> reservedCards;
        final List<Noble> nobles;

        PlayerSnapshot(Player p) {
            this.name = p.getName();
            this.tokens = new HashMap<>(p.getTokens());
            this.purchasedCards = new ArrayList<>(p.getPurchasedCards());
            this.reservedCards = new ArrayList<>(p.getReservedCards());
            this.nobles = new ArrayList<>(p.getNobles());
        }

        void restore(Player p) {
            p.setName(name);
            p.setTokens(tokens);
            p.setPurchasedCards(purchasedCards);
            p.setReservedCards(reservedCards);
            p.setNobles(nobles);
        }
    }

    private static class BoardSnapshot {
        final Map<Gem, Integer> gemBank;
        final Map<Integer, List<Card>> cardDecks;
        final Map<Integer, List<Card>> availableCards;
        final List<Noble> availableNobles;

        BoardSnapshot(Board b) {
            this.gemBank = new HashMap<>(b.getGemBank());
            this.cardDecks = b.copyDecks(); // Need to add this to Board
            this.availableCards = new HashMap<>();
            for (int t = 1; t <= 3; t++) {
                this.availableCards.put(t, new ArrayList<>(b.getAvailableCards(t)));
            }
            this.availableNobles = new ArrayList<>(b.getAvailableNobles());
        }

        void restore(Board b) {
            b.setGemBank(gemBank);
            b.restoreDecks(cardDecks);
            b.setAvailableCards(availableCards);
            b.setAvailableNobles(availableNobles);
        }
    }
    
    /**
     * Creates a new game with the specified players and configuration.
     * 
     * @param players List of players in the game
     * @param winningPoints Points required to win
     * @param maxTokens Maximum tokens allowed per player
     */
    public Game(final List<Player> players, final int winningPoints, final int maxTokens) {
        this.players = new ArrayList<>(players);
        this.board = new Board(players.size());
        this.winningPoints = winningPoints;
        this.maxTokens = maxTokens;
        this.currentState = GameState.ONGOING;
        this.currentPlayerIndex = 0;
        this.winner = null;
        this.isFinalRound = false;
        this.finalRoundPlayerIndex = -1;
        this.recentMoves = new ArrayDeque<>();
        this.undoHistory = new ArrayDeque<>();
    }

    public void saveUndoState() {
        undoHistory.push(new GameSnapshot(this));
        if (undoHistory.size() > 10) {
            undoHistory.removeLast();
        }
    }

    public boolean undo() {
        if (undoHistory.isEmpty()) {
            return false;
        }
        GameSnapshot snapshot = undoHistory.pop();
        for (int i = 0; i < players.size(); i++) {
            snapshot.playerSnapshots.get(i).restore(players.get(i));
        }
        snapshot.boardSnapshot.restore(board);
        this.currentPlayerIndex = snapshot.currentPlayerIndex;
        this.currentState = snapshot.currentState;
        this.isFinalRound = snapshot.isFinalRound;
        this.finalRoundPlayerIndex = snapshot.finalRoundPlayerIndex;
        this.winner = null;
        this.recentMoves.clear();
        this.recentMoves.addAll(snapshot.recentMoves);
        return true;
    }
    
    /**
     * Gets the list of players in the game.
     * 
     * @return Unmodifiable list of players
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    /**
     * Gets the current player (player whose turn it is).
     * 
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Gets the game board.
     * 
     * @return Game board
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Gets the current game state.
     * 
     * @return Current game state
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the winning points threshold.
     * 
     * @return Points required to win
     */
    public int getWinningPoints() {
        return winningPoints;
    }
    
    /**
     * Gets the maximum tokens allowed per player.
     * 
     * @return Maximum token count
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    public void addRecentMove(final String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        recentMoves.addLast(entry);
        while (recentMoves.size() > 5) {
            recentMoves.removeFirst();
        }
    }

    public List<String> getRecentMoves() {
        return new ArrayList<>(recentMoves);
    }
    
    /**
     * Gets the current winner, if any.
     * 
     * @return Winner or null if no winner yet
     */
    public Player getWinner() {
        return winner;
    }
    
    /**
     * Checks if the game is in the final round.
     * 
     * @return true if in final round, false otherwise
     */
    public boolean isFinalRound() {
        return isFinalRound;
    }
    
    /**
     * Checks if the game is finished.
     * 
     * @return true if game is finished, false otherwise
     */
    public boolean isGameFinished() {
        return currentState.isFinished();
    }
    
    /**
     * Advances to the next player's turn.
     * Handles final round logic and game state transitions.
     */
    public void advanceToNextPlayer() {
        if (currentState.isFinished()) {
            return;
        }

        if (!isFinalRound && hasPlayerReachedWinningScore()) {
            startFinalRound();
        }

        final int nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (isFinalRound && nextPlayerIndex == finalRoundPlayerIndex) {
            currentState = GameState.FINISHED;
            determineWinner();
            return;
        }

        currentPlayerIndex = nextPlayerIndex;
    }
    
    /**
     * Checks if any player has reached the winning score.
     * 
     * @return true if a player has won, false otherwise
     */
    public boolean hasPlayerReachedWinningScore() {
        for (final Player player : players) {
            if (player.getTotalPoints() >= winningPoints) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Starts the final round after a player reaches winning score.
     */
    private void startFinalRound() {
        isFinalRound = true;
        finalRoundPlayerIndex = currentPlayerIndex;
        currentState = GameState.FINAL_ROUND;
    }
    
    /**
     * Determines the winner based on points and tie-breaker rules.
     */
    private void determineWinner() {
        Player highestScorer = null;
        int highestScore = -1;
        int fewestCards = Integer.MAX_VALUE;
        
        // Find player(s) with highest score
        for (final Player player : players) {
            final int score = player.getTotalPoints();
            if (score > highestScore) {
                highestScore = score;
                highestScorer = player;
                fewestCards = player.getPurchasedCards().size();
            } else if (score == highestScore) {
                // Tie-breaker: fewest purchased cards
                final int playerCards = player.getPurchasedCards().size();
                if (playerCards < fewestCards) {
                    highestScorer = player;
                    fewestCards = playerCards;
                }
            }
        }
        
        winner = highestScorer;
    }
    
    /**
     * Gets a summary of the current game state.
     * 
     * @return Map of game state information
     */
    public Map<String, Object> getGameStateSummary() {
        final Map<String, Object> summary = new HashMap<>();
        
        summary.put("currentState", currentState);
        summary.put("currentPlayer", getCurrentPlayer().getName());
        summary.put("winningPoints", winningPoints);
        summary.put("isFinalRound", isFinalRound);
        
        if (winner != null) {
            summary.put("winner", winner.getName());
        }
        
        return summary;
    }
    
    /**
     * Returns a string representation of the game state.
     * 
     * @return Game summary
     */
    @Override
    public String toString() {
        return String.format("Game [State: %s, Players: %d, Current: %s, Winner: %s]",
                           currentState, players.size(), 
                           getCurrentPlayer().getName(),
                           winner != null ? winner.getName() : "None");
    }
}
