/**
 * Interface for game view implementations.
 * Defines the contract for displaying game state and receiving user input.
 * 
 */
package com.splendor.view;

import com.splendor.model.Game;
import com.splendor.model.Move;
import com.splendor.model.Noble;
import com.splendor.model.Player;
import com.splendor.model.MenuOption;
import com.splendor.model.Player;
import java.util.List;
import java.util.Map;

/**
 * Interface for game view implementations.
 * Supports different view types (console, network, GUI) through a common
 * interface.
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
     * @return User input after displaying message
     */
    String displayMessage(String message);

    /**
     * Displays a notification message to the user without waiting for acknowledgement.
     * 
     * @param message Message to display
     */
    void displayNotification(String message);

    /**
     * Displays an error message to the user.
     * 
     * @param errorMessage Error message to display
     * @return User input after displaying error
     */
    String displayError(String errorMessage);

    /**
     * Prompts the current player for their command.
     * 
     * @param player Current player
     * @param game   Current game state (for validation/context)
     * @return Command string entered by the player
     */
    String promptForCommand(Player player, Game game);

    /**
     * Displays the menu, prompts the active player to choose an action, collects
     * any follow-up input (gem colors, card IDs, deck tier), and returns the
     * fully-constructed Move ready for validation and execution.
     *
     * @param player  The player whose turn it is.
     * @param game    The current game state (used for context and re-rendering).
     * @param options The ordered list of MenuOptions built by MenuBuilder.
     * @return A Move encapsulating the player's chosen action and parameters.
     */
    Move promptForMove(Player player, Game game, List<MenuOption> options);

    /**
     * Prompts the player to discard excess tokens.
     * 
     * @param player      Player with excess tokens
     * @param excessCount Number of tokens to discard
     * @return Move representing tokens to discard
     */
    Move promptForTokenDiscard(Player player, int excessCount);

    /**
     * Displays the game winner.
     * 
     * @param winner      Winning player
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
     * @param options The menu options to display
     * @param game Current game state
     */
    void displayAvailableMoves(List<MenuOption> options, Game game);

    /**
     * When a player qualifies for more than one noble at the end of their turn,
     * asks them to pick which noble tile to claim.
     *
     * @param player The player who qualifies for a noble visit.
     * @param nobles The subset of nobles the player can currently claim (size >= 1).
     * @return The Noble the player has chosen to take.
     */
    Noble promptForNobleChoice(Player player, List<Noble> nobles);

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
     * Waits for the user to press Enter before continuing.
     * Used to pause after messages so the user can read them.
     * @return User input (useful for catching undo commands)
     */
    String waitForEnter();

    /**
     * Closes the view and releases any resources.
     */
    void close();
}
