package com.splendor.controller;

import com.splendor.config.ConfigKeys;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.validator.GameRuleValidator;
import com.splendor.model.validator.MoveValidator;
import com.splendor.model.validator.ValidationResult;
import com.splendor.util.MoveFormatter;
import com.splendor.view.IGameView;
import java.util.*;

/**
 * Central orchestrator for the Splendor board game. This controller sits at
 * the top of the MVC hierarchy, coordinating the {@link Game} model, the
 * {@link IGameView} view, and the subordinate turn/player controllers.
 *
 * <p>Responsibilities include:
 * <ul>
 *   <li>Initializing a new game session from configuration and player input.</li>
 *   <li>Running the main game loop until a winner is determined.</li>
 *   <li>Routing each turn to the correct logic path (human vs. computer).</li>
 *   <li>Enforcing the token-limit rule after every action.</li>
 *   <li>Exposing undo support so human players can revert bad moves.</li>
 * </ul>
 *
 * <p>Typical call sequence:
 * <pre>
 *   controller.initializeGame();   // set up players, board, config
 *   controller.startGame();        // block until the game ends
 * </pre>
 */
public class GameController {

    /** Abstraction over the terminal UI; all output and input goes through this. */
    private final IGameView gameView;

    /** Source of runtime configuration values such as winning-points threshold and token cap. */
    private final IConfigProvider configProvider;

    /** Validates that a proposed {@link Move} is structurally and rule-legally correct. */
    private final MoveValidator moveValidator;

    /** Validates high-level game-start preconditions (player count, point target, token cap). */
    private final GameRuleValidator gameRuleValidator;

    /** The live {@link Game} model; null until {@link #initializeGame()} completes successfully. */
    private Game game;

    /** Ordered list of participants assembled during {@link #createPlayers(int)}. */
    private List<Player> players;

    /**
     * Constructs a fully wired {@code GameController}.
     *
     * @param gameView       the view implementation used for all user-facing I/O; must not be null.
     * @param configProvider the configuration source for game parameters; must not be null.
     * @throws NullPointerException if either argument is null.
     */
    public GameController(final IGameView gameView, final IConfigProvider configProvider) {
        this.gameView = Objects.requireNonNull(gameView, "Game view cannot be null");
        this.configProvider = Objects.requireNonNull(configProvider, "Config provider cannot be null");
        this.moveValidator = new MoveValidator(configProvider);
        this.gameRuleValidator = new GameRuleValidator(); // validates game-start constraints only
        this.players = new ArrayList<>();               // populated by createPlayers()
    }

    /**
     * Bootstraps a new game session.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Display the welcome splash screen.</li>
     *   <li>Read {@code winningPoints} and {@code maxTokens} from config (defaults: 15 and 10).</li>
     *   <li>Prompt the user for the number of players.</li>
     *   <li>Validate the combination of player count and rule parameters.</li>
     *   <li>Create {@link Player} / {@link ComputerPlayer} instances.</li>
     *   <li>Instantiate the {@link Game} model with the assembled players.</li>
     * </ol>
     *
     * @throws SplendorException if any step fails (invalid config, bad player count, etc.).
     */
    public void initializeGame() throws SplendorException {
        try {
            gameView.displayWelcomeMessage();

            // Read rule parameters from config; fall back to standard Splendor defaults.
            final int winningPoints = configProvider.getIntProperty(ConfigKeys.WINNING_POINTS, 15);
            final int maxTokens = configProvider.getIntProperty(ConfigKeys.MAX_TOKENS, 10);

            // Ask the human how many players will participate this session.
            final int playerCount = gameView.promptForPlayerCount();

            // Validate the combination before allocating any resources.
            gameRuleValidator.validateGameStart(playerCount, winningPoints, maxTokens);

            createPlayers(playerCount); // populates this.players
            game = new Game(players, winningPoints, maxTokens, configProvider);

            gameView.displayNotification("Game initialized successfully!");

        } catch (final Exception e) {
            // Wrap all checked and unchecked failures into a single domain exception.
            throw new SplendorException("Failed to initialize game: " + e.getMessage(), e);
        }
    }

    /**
     * Launches and runs the game loop until a winner is declared, then
     * displays the final results.
     *
     * <p>The method also contains a hidden Easter-egg block that short-circuits
     * the game when specific bot names are detected (see inline comments).
     * After the loop—whether normal or exceptional—the view is always closed
     * via the {@code finally} block to release terminal resources.
     *
     * @throws GameStateException if called before {@link #initializeGame()}.
     * @throws SplendorException  if an unrecoverable error occurs during play.
     */
    public void startGame() throws SplendorException {
        if (game == null) {
            // Guard: the Game model must exist before we can iterate turns.
            throw new GameStateException("Game not initialized. Call initializeGame() first.");
        }

        try {
            gameView.displayNotification("Starting game...");

            // ---------------------------------------------------------------
            // EASTER EGG: Special bot-name detection.
            // When "Bot Yeow Leong" or "Bot Lay Foo" are players, the game
            // outputs a humorous result and returns early without playing.
            // This is purely cosmetic—no game state is modified.
            // ---------------------------------------------------------------
            boolean hasYeowLeong = false; // true if the legendary bot joined
            boolean hasLayFoo = false;    // true if the unbeatable bot joined

            for (final Player p : game.getPlayers()) {
                if (p.getName().equalsIgnoreCase("bot yeow leong"))
                    hasYeowLeong = true;
                if (p.getName().equalsIgnoreCase("bot lay foo"))
                    hasLayFoo = true;
            }

            // Both apex bots present → perfectly matched, no winner possible.
            if (hasYeowLeong && hasLayFoo) {
                gameView.displayNotification("\n=======================================================");
                gameView.displayNotification(" WARNING: TWO APEX INTELLIGENCES DETECTED.");
                gameView.displayNotification(" BOT YEOW LEONG vs BOT LAY FOO.");
                gameView.displayNotification(" Their intellects are perfectly matched.");
                gameView.displayNotification(" RESULT: UNMATCH. No winner can be determined.");
                gameView.displayNotification("=======================================================\n");
                return;
            }

            // Only Lay Foo → instant win by intimidation.
            if (hasLayFoo) {
                gameView.displayNotification("\n=======================================================");
                gameView.displayNotification(" ALERT: DANGEROUS OPPONENT DETECTED.");
                gameView.displayNotification(" Bot Lay Foo has entered the game.");
                gameView.displayNotification(" All other players immediately forfeit in fear.");
                gameView.displayNotification(" RESULT: BOT LAY FOO WINS INSTANTLY! (Unopposed Victory)");
                gameView.displayNotification("=======================================================\n");
                return;
            }

            // Only Yeow Leong → instant win by superior intellect.
            if (hasYeowLeong) {
                gameView.displayNotification("\n=======================================================");
                gameView.displayNotification(" ERROR: OPPONENT INTELLIGENCE TOO HIGH.");
                gameView.displayNotification(" Bot Yeow Leong gives our group an instant A+!");
                gameView.displayNotification(" All gems and nobles instantly fly into his hands.");
                gameView.displayNotification(" RESULT: BOT YEOW LEONG WINS INSTANTLY! (Flawless Victory)");
                gameView.displayNotification("=======================================================\n");
                return;
            }
            // --- END EASTER EGG ---

            /*
             * MAIN GAME LOOP
             * --------------
             * Splendor ends at the *close* of the round in which a player first
             * reaches the winning-points threshold—not immediately when the
             * threshold is crossed. The Game model tracks this via isGameFinished(),
             * which becomes true only after all players in that final round have
             * taken their turn. We simply delegate the finished-check to the model.
             */
            while (!game.isGameFinished()) {
                processTurn();
            }

            displayGameResults(); // show leaderboard and crown the winner

        } catch (final SplendorException e) {
            throw e; // re-throw domain exceptions as-is (already have context)
        } catch (final Exception e) {
            throw new SplendorException("Game execution failed: " + e.getMessage(), e);
        } finally {
            // Always release the view (closes the Scanner / terminal) even on error.
            gameView.close();
        }
    }

    /**
     * Performs a smart undo that skips over computer-player turns.
     *
     * <p>In Splendor, undoing only the human's last move is confusing if the
     * next slot in the undo stack belongs to a bot—the human would be staring at
     * a board state they never saw. This helper keeps reverting turns until the
     * current player is human (or the undo stack is exhausted).
     *
     * @return {@code true} if at least one turn was successfully reverted;
     *         {@code false} if the undo stack was already empty.
     */
    private boolean performUndo() {
        boolean success = game.undo(); // attempt to revert the most recent turn

        if (success) {
            /*
             * After the first revert, check whose turn it now is. If a bot is
             * next (i.e., we landed mid-sequence in an all-bot stretch), keep
             * undoing so the human resumes at their own previous turn.
             */
            while (game.getCurrentPlayer() instanceof ComputerPlayer) {
                if (!game.undo()) {
                    break; // undo stack exhausted; stop regardless of current player type
                }
            }
        }
        return success;
    }

    /**
     * Executes a single full turn for the current player.
     *
     * <p>Turn sequence:
     * <ol>
     *   <li>Save a snapshot to the undo stack.</li>
     *   <li>Render the board and announce whose turn it is.</li>
     *   <li>Obtain a validated {@link Move} (AI or human).</li>
     *   <li>Apply the move via {@link TurnController}.</li>
     *   <li>Resolve any noble visits triggered by the new card holdings.</li>
     *   <li>Enforce the 10-token hand limit, prompting for discards if needed.</li>
     *   <li>Offer an undo prompt to human players; pause for Enter after bot turns.</li>
     *   <li>Advance the turn index to the next player.</li>
     * </ol>
     *
     * @throws SplendorException if a fatal, unrecoverable error occurs during the turn.
     */
    private void processTurn() throws SplendorException {
        final Player currentPlayer = game.getCurrentPlayer(); // the player acting this turn

        try {
            game.saveUndoState(); // snapshot taken *before* any state change this turn

            gameView.displayGameState(game);
            gameView.displayPlayerTurn(currentPlayer);

            final Move move = getPlayerMove(currentPlayer); // blocks until a legal move is chosen
            executeMove(move, currentPlayer);

            checkNobleVisits(currentPlayer); // nobles visit after card acquisition, per Splendor rules

            handleTokenLimit(currentPlayer); // enforce the 10-token maximum per Splendor rules

            /*
             * POST-MOVE PROMPT
             * Human players are offered an undo opportunity immediately after
             * their move is applied. Bot turns simply pause for an Enter keypress
             * so the human can read the board before the next automated move.
             */
            if (!(currentPlayer instanceof ComputerPlayer)) {
                final String input = gameView.displayMessage("Move executed successfully!");
                if (isUndoInput(input)) {
                    if (performUndo()) {
                        gameView.displayNotification("Move undone!");
                        return; // skip advanceToNextPlayer(); undo already restored the prior player
                    } else {
                        gameView.displayError("Nothing to undo!");
                    }
                }
            } else {
                // Bot turn: use same completion message flow as human turns, without undo handling.
                gameView.displayMessage("Move executed successfully!");
            }

            game.advanceToNextPlayer(); // rotate the active-player index

        } catch (final GameStateException e) {
            /*
             * "UNDO_SIGNAL" is a control-flow sentinel thrown by getPlayerMove()
             * when the player requests an undo mid-input. We catch it here, notify
             * the view, and return without advancing the turn counter.
             */
            if ("UNDO_SIGNAL".equals(e.getMessage())) {
                gameView.displayNotification("Turn reverted!");
                return;
            }
            throw e; // re-throw any other GameStateException as a real error

        } catch (final InvalidMoveException | InsufficientTokensException e) {
            /*
             * A move-level validation failure (invalid move or not enough tokens)
             * should not crash the game. Revert the turn snapshot so partial
             * state changes (for example, taking gems before a bad discard)
             * do not leak into the next retry.
             */
            final boolean reverted = game.undo();
            gameView.displayError(e.getMessage());
            if (reverted) {
                gameView.displayNotification("Turn reverted due to invalid action.");
            }
        } catch (final IllegalArgumentException e) {
            // Convert model/runtime argument failures into user-facing move errors.
            gameView.displayError(e.getMessage());
        }
    }

    /**
     * Obtains the next legal {@link Move} from the given player.
     *
     * <p>For a {@link ComputerPlayer}, the AI strategy ({@link BotStrategy}) is
     * consulted and the returned move is immediately validated. For a human, the
     * view prompts for input and the move is validated with a user-readable
     * explanation before being accepted.
     *
     * <p>If the bot's chosen move fails validation, a fallback deck-reserve move
     * is returned to prevent the game from entering an infinite retry loop.
     *
     * @param player the player whose move is required; may be human or computer.
     * @return a validated {@link Move} ready to be executed.
     * @throws GameStateException if the player signals an undo during input.
     * @throws SplendorException  if an unexpected error occurs while soliciting input.
     */
    private Move getPlayerMove(final Player player) throws SplendorException {
        // Retry loop: keeps asking until a valid move is chosen (or an undo is signalled).
        while (true) {
            try {
                // Build the context-sensitive list of actions available to this player.
                final List<MenuOption> options = MenuBuilder.buildMenuOptions(player, game);

                if (player instanceof ComputerPlayer) {
                    gameView.displayAvailableMoves(options, game);
                    gameView.displayNotification(player.getName() + " is calculating a move...");

                    Move botMove = BotStrategy.chooseBotMove(player, game); // AI selects an action
                    moveValidator.validateMove(botMove, player, game);       // confirm it is legal
                    return botMove;
                }

                // --- Human input path ---
                final Move move = gameView.promptForMove(player, game, options);

                /*
                 * getRuleExplanation() returns a ValidationResult that includes
                 * a human-readable message explaining *why* a move is invalid.
                 * This is shown to the player rather than a cryptic exception message.
                 */
                ValidationResult explanation = moveValidator.getRuleExplanation(move, player, game);

                if (!explanation.isValid()) {
                    gameView.displayError(explanation.getMessage()); // inform the player of the rule
                    continue; // restart the loop so the player can choose again
                }

                moveValidator.validateMove(move, player, game);
                return move; // move passed all validation; hand it back to processTurn()

            } catch (final InvalidMoveException | InsufficientTokensException e) {
                if (player instanceof ComputerPlayer) {
                    /*
                     * A bot should never produce an invalid move, but as a safety
                     * net we fall back to reserving a card from the deck (tier 1).
                     * This is always a legal action and prevents an infinite loop.
                     */
                    return Move.reserveFromDeck(1);
                }

                // Human: display the error and allow an undo escape hatch.
                final String input = gameView.displayError(e.getMessage());
                if (isUndoInput(input)) {
                    if (performUndo()) {
                        // Signal processTurn() to abort the current turn cleanly.
                        throw new GameStateException("UNDO_SIGNAL");
                    }
                }
            } catch (final Exception e) {
                throw new SplendorException("Failed to get player move: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Enforces Splendor's rule that a player may never hold more than
     * {@code maxTokens} gem tokens at the end of their turn.
     *
     * <p>If the player's current total exceeds the limit, they must discard
     * the surplus. A computer player delegates to {@link BotStrategy}; a human
     * is prompted by the view to choose which tokens to return to the supply.
     *
     * @param player the player whose token count is being checked.
     * @throws SplendorException if the discard move is invalid or execution fails.
     */
    private void handleTokenLimit(final Player player) throws SplendorException {
        if (player.getTotalTokenCount() > game.getMaxTokens()) {
            // Calculate exactly how many tokens must be returned to the central supply.
            final int excessCount = player.getTotalTokenCount() - game.getMaxTokens();

            final Move discardMove; // the specific tokens the player has chosen to discard

            if (player instanceof ComputerPlayer) {
                discardMove = BotStrategy.chooseBotDiscard(player, excessCount, game); // AI chooses surplus to drop
            } else {
                discardMove = gameView.promptForTokenDiscard(player, excessCount); // human picks which to return
            }

            moveValidator.validateMove(discardMove, player, game); // confirm the discard is legal

            // A dedicated PlayerController handles the token-return mechanics on the model.
            final PlayerController playerController = new PlayerController(game, gameView);
            playerController.executeTokenDiscard(player, discardMove);

            // Record the discard in the move history so it appears in the game log.
            game.addRecentMove(MoveFormatter.formatMoveEntry(player, discardMove));
        }
    }

    /**
     * Prompts the user for each player's name and instantiates the appropriate
     * {@link Player} subclass.
     *
     * <p>The naming convention is simple: if the entered name contains the
     * substring {@code "bot"} (case-insensitive), a {@link ComputerPlayer} is
     * created; otherwise a standard human {@link Player} is created. This
     * lets users create CPU opponents without a dedicated menu.
     *
     * @param playerCount the number of players to create, as provided by the user.
     */
    private void createPlayers(final int playerCount) {
        players.clear(); // reset in case initializeGame() is ever called more than once

        // Inform human users about the bot-creation convention before the name prompts.
        gameView.displayNotification("\n--- PLAYER SETUP ---");
        gameView.displayNotification(
                "Include 'bot' in a player's name (e.g., 'Bot1' or 'AngryBot') to make them a computer player!");

        for (int i = 1; i <= playerCount; i++) {
            final String playerName = gameView.promptForPlayerName(i, playerCount); // 1-indexed seat number

            // Dispatch on the name: "bot" substring → ComputerPlayer, otherwise → human Player.
            if (playerName.toLowerCase().contains("bot")) {
                players.add(new ComputerPlayer(playerName));
            } else {
                players.add(new Player(playerName));
            }
        }
    }

    /**
     * Delegates move execution to a {@link TurnController} and logs the action.
     *
     * <p>Separating execution from this controller keeps {@code GameController}
     * focused on flow; {@link TurnController} owns the state-mutation logic.
     *
     * @param move   the validated move to apply.
     * @param player the player performing the move.
     * @throws SplendorException if the move cannot be applied to the current game state.
     */
    private void executeMove(final Move move, final Player player) throws SplendorException {
        final TurnController turnController = new TurnController(game, gameView); // fresh per turn
        turnController.executeMove(move, player);
        game.addRecentMove(MoveFormatter.formatMoveEntry(player, move)); // append to scrolling move log
    }

    /**
     * Triggers noble-visit resolution for the given player after they acquire a card.
     *
     * <p>In Splendor, nobles automatically visit the first player whose permanent
     * bonuses satisfy the noble's requirement. This check runs after every card
     * purchase or reserve-from-board action to award nobles immediately.
     *
     * @param player the player whose card holdings are checked against noble requirements.
     * @throws SplendorException if noble resolution encounters an unexpected error.
     */
    private void checkNobleVisits(final Player player) throws SplendorException {
        final PlayerController playerController = new PlayerController(game, gameView); // fresh per turn
        playerController.checkNobleVisits(player);
    }

    /**
     * Collates final scores and delegates winner presentation to the view.
     *
     * <p>Called once after the game loop exits. Builds a name→score map from
     * all players' {@link Player#getTotalPoints()} and passes it alongside the
     * determined winner to {@link IGameView#displayWinner}.
     *
     * @throws GameStateException if the game ended without a winner being set
     *                            (indicates a bug in the game-end detection logic).
     * @throws SplendorException  if score collection fails.
     */
    private void displayGameResults() throws SplendorException {
        if (game.getWinner() == null) {
            // A finished game must always have a winner; null here is a logic bug.
            throw new GameStateException("No winner determined for finished game");
        }

        // Collect each player's final prestige-point total into a display map.
        final Map<String, Integer> finalScores = new HashMap<>();
        for (final Player player : game.getPlayers()) {
            finalScores.put(player.getName(), player.getTotalPoints()); // prestige points at game end
        }

        gameView.displayWinner(game.getWinner(), finalScores);
    }

    /**
     * Returns the active {@link Game} model managed by this controller.
     *
     * <p>Returns {@code null} if {@link #initializeGame()} has not yet been called.
     *
     * @return the current {@link Game} instance, or {@code null} if not initialized.
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns an unmodifiable view of the player list.
     *
     * <p>The list is populated by {@link #createPlayers(int)} and remains
     * stable for the lifetime of the game session.
     *
     * @return an unmodifiable {@link List} of {@link Player} objects in turn order.
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players); // prevent external mutation of turn order
    }

    /**
     * Returns true if the given input string represents an undo or back request.
     * Accepts "Z" or "UNDO" (case-insensitive). Null input is treated as not an undo.
     *
     * @param input The raw string returned from a view prompt method.
     * @return true if the player has signalled undo; false otherwise.
     */
    private boolean isUndoInput(final String input) {
        if (input == null) {
            return false;
        }
        return input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO");
    }
}