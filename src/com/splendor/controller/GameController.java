/**
 * Main game controller that orchestrates the game flow.
 * Coordinates between model and view to manage the complete game lifecycle.
 * 
 * @author Splendor Development Team
 * @version 1.0
 * // Edited by AI; implemented main game loop and exception handling
 */
package com.splendor.controller;

import com.splendor.config.ConfigKeys;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.validator.GameRuleValidator;
import com.splendor.model.validator.MoveValidator;
import com.splendor.view.IGameView;
import java.util.*;

/**
 * Main controller that orchestrates the game flow.
 * Manages game initialization, turn handling, and win condition checking.
 */
public class GameController {
    
    private final IGameView gameView;
    private final IConfigProvider configProvider;
    private final MoveValidator moveValidator;
    private final GameRuleValidator gameRuleValidator;
    private Game game;
    private List<Player> players;
    
    /**
     * Creates a new GameController with the specified view and configuration.
     * 
     * @param gameView Game view implementation
     * @param configProvider Configuration provider
     */
    public GameController(final IGameView gameView, final IConfigProvider configProvider) {
        this.gameView = Objects.requireNonNull(gameView, "Game view cannot be null");
        this.configProvider = Objects.requireNonNull(configProvider, "Config provider cannot be null");
        this.moveValidator = new MoveValidator();
        this.gameRuleValidator = new GameRuleValidator();
        this.players = new ArrayList<>();
    }
    
    /**
     * Initializes the game by setting up players and game state.
     * 
     * @throws SplendorException if initialization fails
     */
    public void initializeGame() throws SplendorException {
        try {
            gameView.displayWelcomeMessage();
            
            // Get configuration values
            final int winningPoints = configProvider.getIntProperty(ConfigKeys.WINNING_POINTS, 15);
            final int maxTokens = configProvider.getIntProperty(ConfigKeys.MAX_TOKENS, 10);
            
            // Get player information
            final int playerCount = gameView.promptForPlayerCount();
            
            // Validate game parameters
            gameRuleValidator.validateGameStart(playerCount, winningPoints, maxTokens);
            
            // Create players
            createPlayers(playerCount);
            
            // Create game
            game = new Game(players, winningPoints, maxTokens);
            
            gameView.displayMessage("Game initialized successfully!");
            
        } catch (final Exception e) {
            throw new SplendorException("Failed to initialize game: " + e.getMessage(), e);
        }
    }
    
    /**
     * Starts the main game loop.
     * 
     * @throws SplendorException if game execution fails
     */
    public void startGame() throws SplendorException {
        if (game == null) {
            throw new GameStateException("Game not initialized. Call initializeGame() first.");
        }
        
        try {
            gameView.displayMessage("Starting game...");
            
            // Main game loop
            while (!game.isGameFinished()) {
                processTurn();
            }
            
            // Display final results
            displayGameResults();
            
        } catch (final SplendorException e) {
            throw e; // Re-throw Splendor exceptions
        } catch (final Exception e) {
            throw new SplendorException("Game execution failed: " + e.getMessage(), e);
        } finally {
            gameView.close();
        }
    }
    
    /**
     * Processes a single turn for the current player.
     * 
     * @throws SplendorException if turn processing fails
     */
    private void processTurn() throws SplendorException {
        final Player currentPlayer = game.getCurrentPlayer();
        
        try {
            // Display current game state
            gameView.displayGameState(game);
            gameView.displayPlayerTurn(currentPlayer);
            
            // Get player move
            final Move move = getPlayerMove(currentPlayer);
            
            // Execute move
            executeMove(move, currentPlayer);
            
            // Check for noble visits after card purchases
            if (move.getMoveType() == MoveType.BUY_CARD) {
                checkNobleVisits(currentPlayer);
            }
            
            // Handle token limit
            handleTokenLimit(currentPlayer);
            
            // Advance to next player
            game.advanceToNextPlayer();
            
        } catch (final InvalidMoveException | InsufficientTokensException e) {
            // Display error and let player try again
            gameView.displayError(e.getMessage());
            // Don't advance to next player - let them try again
        }
    }
    
    /**
     * Gets a valid move from the current player.
     * 
     * @param player Current player
     * @return Valid move
     * @throws SplendorException if move acquisition fails
     */
    private Move getPlayerMove(final Player player) throws SplendorException {
        while (true) {
            try {
                final Move move = gameView.promptForMove(player, game);
                
                // Validate move
                moveValidator.validateMove(move, player, game);
                
                return move;
                
            } catch (final InvalidMoveException | InsufficientTokensException e) {
                gameView.displayError(e.getMessage());
                // Let player try again
            } catch (final Exception e) {
                throw new SplendorException("Failed to get player move: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Executes the specified move for the player.
     * 
     * @param move Move to execute
     * @param player Player executing the move
     * @throws SplendorException if move execution fails
     */
    private void executeMove(final Move move, final Player player) throws SplendorException {
        final TurnController turnController = new TurnController(game, gameView);
        turnController.executeMove(move, player);
    }
    
    /**
     * Checks if any nobles can visit the player after a card purchase.
     * 
     * @param player Player to check for noble visits
     * @throws SplendorException if noble assignment fails
     */
    private void checkNobleVisits(final Player player) throws SplendorException {
        final PlayerController playerController = new PlayerController(game, gameView);
        playerController.checkNobleVisits(player);
    }
    
    /**
     * Handles token limit enforcement for the player.
     * 
     * @param player Player to check for token limit
     * @throws SplendorException if token handling fails
     */
    private void handleTokenLimit(final Player player) throws SplendorException {
        if (player.getTotalTokenCount() > game.getMaxTokens()) {
            final int excessCount = player.getTotalTokenCount() - game.getMaxTokens();
            final Move discardMove = gameView.promptForTokenDiscard(player, excessCount);
            
            // Validate discard move
            moveValidator.validateMove(discardMove, player, game);
            
            // Execute discard
            final PlayerController playerController = new PlayerController(game, gameView);
            playerController.executeTokenDiscard(player, discardMove);
        }
    }
    
    /**
     * Creates players based on the specified count.
     * 
     * @param playerCount Number of players to create
     */
    private void createPlayers(final int playerCount) {
        players.clear();
        
        for (int i = 1; i <= playerCount; i++) {
            final String playerName = gameView.promptForPlayerName(i, playerCount);
            final Player player = new Player(playerName);
            players.add(player);
        }
    }
    
    /**
     * Displays the final game results.
     * 
     * @throws SplendorException if result display fails
     */
    private void displayGameResults() throws SplendorException {
        if (game.getWinner() == null) {
            throw new GameStateException("No winner determined for finished game");
        }
        
        // Build final scores map
        final Map<String, Integer> finalScores = new HashMap<>();
        for (final Player player : game.getPlayers()) {
            finalScores.put(player.getName(), player.getTotalPoints());
        }
        
        gameView.displayWinner(game.getWinner(), finalScores);
    }
    
    /**
     * Gets the current game state.
     * 
     * @return Current game or null if not initialized
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Gets the list of players.
     * 
     * @return List of players or empty list if not initialized
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}