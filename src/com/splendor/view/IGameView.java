/**
 * Interface for game view implementations.
 * Defines the contract for displaying game state and receiving user input.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.view;

import com.splendor.model.Game;
import com.splendor.model.Move;
import com.splendor.model.Player;
import java.util.Map;

/**
 * Interface for game view implementations.
 * Supports different view types (console, network, GUI) through a common interface.
 */
public interface IGameView {
    
    /**
     * Displays the current game state including board and player information.
     * 
     * @param game Current game state
     */
    void displayGameState(Game game);
    
    /**
     * Displays the current player's turn information.
     * 
     * @param player Current player
     */
    void displayPlayerTurn(Player player);
    
    /**
     * Displays a message to the user.
     * 
     * @param message Message to display
     */
    void displayMessage(String message);
    
    /**
     * Displays an error message to the user.
     * 
     * @param errorMessage Error message to display
     */
    void displayError(String errorMessage);
    
    /**
     * Prompts the current player for their command.
     * 
     * @param player Current player
     * @param game Current game state (for validation/context)
     * @return Command string entered by the player
     */
    String promptForCommand(Player player, Game game);

    Move promptForMove(Player player, Game game);
    
    /**
     * Prompts the player to discard excess tokens.
     * 
     * @param player Player with excess tokens
     * @param excessCount Number of tokens to discard
     * @return Move representing tokens to discard
     */
    Move promptForTokenDiscard(Player player, int excessCount);
    
    /**
     * Displays the game winner.
     * 
     * @param winner Winning player
     * @param finalScores Map of player names to final scores
     */
    void displayWinner(Player winner, Map<String, Integer> finalScores);
    
    /**
     * Clears the display (useful for console views).
     */
    void clearDisplay();
    
    /**
     * Displays available moves for the current player.
     * 
     * @param player Current player
     * @param game Current game state
     */
    void displayAvailableMoves(Player player, Game game);
    
    /**
     * Prompts for player names during game setup.
     * 
     * @param playerNumber Player number (1-based)
     * @param totalPlayers Total number of players
     * @return Player name entered by user
     */
    String promptForPlayerName(int playerNumber, int totalPlayers);
    
    /**
     * Prompts for the number of players.
     * 
     * @return Number of players selected
     */
    int promptForPlayerCount();
    
    /**
     * Displays a welcome message and game instructions.
     */
    void displayWelcomeMessage();
    
    /**
     * Closes the view and releases any resources.
     */
    void close();
}
