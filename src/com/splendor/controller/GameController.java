package com.splendor.controller;

import com.splendor.config.ConfigKeys;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.*;
import com.splendor.model.*;
import com.splendor.model.BotStrategy;
import com.splendor.model.validator.GameRuleValidator;
import com.splendor.model.validator.MoveValidator;
import com.splendor.util.MoveFormatter;
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

    public GameController(final IGameView gameView, final IConfigProvider configProvider) {
        this.gameView = Objects.requireNonNull(gameView, "Game view cannot be null");
        this.configProvider = Objects.requireNonNull(configProvider, "Config provider cannot be null");
        this.moveValidator = new MoveValidator();
        this.gameRuleValidator = new GameRuleValidator();
        this.players = new ArrayList<>();
    }

    public void initializeGame() throws SplendorException {
        try {
            gameView.displayWelcomeMessage();
            final int winningPoints = configProvider.getIntProperty(ConfigKeys.WINNING_POINTS, 15);
            final int maxTokens = configProvider.getIntProperty(ConfigKeys.MAX_TOKENS, 10);
            final int playerCount = gameView.promptForPlayerCount();

            gameRuleValidator.validateGameStart(playerCount, winningPoints, maxTokens);
            createPlayers(playerCount);
            game = new Game(players, winningPoints, maxTokens);

            gameView.displayNotification("Game initialized successfully!");

        } catch (final Exception e) {
            throw new SplendorException("Failed to initialize game: " + e.getMessage(), e);
        }
    }

    public void startGame() throws SplendorException {
        if (game == null) {
            throw new GameStateException("Game not initialized. Call initializeGame() first.");
        }

        try {
            gameView.displayNotification("Starting game...");

            // for fun hahaha
            for (final Player p : game.getPlayers()) {
                if (p.getName().equalsIgnoreCase("bot yeow leong")) {
                    gameView.displayNotification("\n=======================================================");
                    gameView.displayNotification(" ERROR: OPPONENT INTELLIGENCE TOO HIGH.");
                    gameView.displayNotification(" " + p.getName() + " gives our group an instant A+!");
                    gameView.displayNotification(" All gems and nobles instantly fly into his hands.");
                    gameView.displayNotification(" " + p.getName().toUpperCase() + " WINS INSTANTLY! (Flawless Victory)");
                    gameView.displayNotification("=======================================================\n");
                    return; // This immediately exits the game without playing a single turn!
                }
            }
            // --- END EASTER EGG ---

            while (!game.isGameFinished()) {
                processTurn();
            }
            displayGameResults();
        } catch (final SplendorException e) {
            throw e; 
        } catch (final Exception e) {
            throw new SplendorException("Game execution failed: " + e.getMessage(), e);
        } finally {
            gameView.close();
        }
    }

    /**
     * Helper method to handle Undos. 
     * If reverting a turn lands on a bot, it keeps undoing until it's a human's turn.
     */
    private boolean performUndo() {
        boolean success = game.undo();
        if (success) {
            // Keep reverting as long as it is a bot's turn!
            while (game.getCurrentPlayer() instanceof ComputerPlayer) {
                if (!game.undo()) {
                    break;
                }
            }
        }
        return success;
    }

    private void processTurn() throws SplendorException {
        final Player currentPlayer = game.getCurrentPlayer();

        try {
            game.saveUndoState();
            gameView.displayGameState(game);
            gameView.displayPlayerTurn(currentPlayer);

            final Move move = getPlayerMove(currentPlayer);
            executeMove(move, currentPlayer);

            checkNobleVisits(currentPlayer);

            handleTokenLimit(currentPlayer);

            if (!(currentPlayer instanceof ComputerPlayer)) {
                // Human turn: Call the view method that includes the Undo prompt
                final String input = gameView.displayMessage("Move executed successfully!");
                if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                    if (performUndo()) { 
                        gameView.displayNotification("Move undone!");
                        return; 
                    } else {
                        gameView.displayError("Nothing to undo!");
                    }
                }
            } else {
                gameView.displayNotification(currentPlayer.getName() + " finished their turn. Press Enter to continue...");
                try { 
                    // Flush any garbage typed while the bot was thinking
                    while (System.in.available() > 0) { System.in.read(); }
                    // Now block and wait for a real Enter key
                    System.in.read(); 
                } catch (Exception e) {}
            }

            game.advanceToNextPlayer();
    
        } catch (final GameStateException e) {
            if ("UNDO_SIGNAL".equals(e.getMessage())) {
                gameView.displayNotification("Turn reverted!");
                return;
            }
            throw e;
        } catch (final InvalidMoveException | InsufficientTokensException e) {
            final String input = gameView.displayError(e.getMessage());
            if (!(currentPlayer instanceof ComputerPlayer) && input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                if (performUndo()) { 
                    gameView.displayNotification("Turn reverted!");
                    return;
                }
            }
        }
    }

    private Move getPlayerMove(final Player player) throws SplendorException {
        while (true) {
            try {
                final List<MenuOption> options = MenuBuilder.buildMenuOptions(player, game);
                
                if (player instanceof ComputerPlayer) {
                    gameView.displayAvailableMoves(options, game);
                    gameView.displayNotification(player.getName() + " is calculating a move...");
                    try { Thread.sleep(1500); } catch (InterruptedException e) {}
                    Move botMove = BotStrategy.chooseBotMove(player, game);
                    moveValidator.validateMove(botMove, player, game);
                    return botMove;
                }

                // Human logic
                final Move move = gameView.promptForMove(player, game, options);
                moveValidator.validateMove(move, player, game);
                return move;

            } catch (final InvalidMoveException | InsufficientTokensException e) {
                if (player instanceof ComputerPlayer) {
                    // If bot fails, force a basic reserve to prevent infinite loops
                    return Move.reserveFromDeck(1); 
                }
                
                final String input = gameView.displayError(e.getMessage());
                if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                    if (performUndo()) { // <-- Uses smart undo
                        throw new GameStateException("UNDO_SIGNAL"); 
                    }
                }
            } catch (final Exception e) {
                throw new SplendorException("Failed to get player move: " + e.getMessage(), e);
            }
        }
    }

    private void handleTokenLimit(final Player player) throws SplendorException {
        if (player.getTotalTokenCount() > game.getMaxTokens()) {
            final int excessCount = player.getTotalTokenCount() - game.getMaxTokens();
            final Move discardMove;

            if (player instanceof ComputerPlayer) {
                discardMove = BotStrategy.chooseBotDiscard(player, excessCount);
            } else {
                discardMove = gameView.promptForTokenDiscard(player, excessCount);
            }

            moveValidator.validateMove(discardMove, player, game);
            final PlayerController playerController = new PlayerController(game, gameView);
            playerController.executeTokenDiscard(player, discardMove);
            game.addRecentMove(MoveFormatter.formatMoveEntry(player, discardMove));
        }
    }

    private void createPlayers(final int playerCount) {
        players.clear();
        
        // Let the players know the secret to creating a CPU!
        gameView.displayNotification("\n--- PLAYER SETUP ---");
        gameView.displayNotification("Include 'bot' in a player's name (e.g., 'Bot1' or 'AngryBot') to make them a computer player!");
        
        for (int i = 1; i <= playerCount; i++) {
            final String playerName = gameView.promptForPlayerName(i, playerCount);
            
            // Type "Bot" as a player name to make them AI!
            if (playerName.toLowerCase().contains("bot")) {
                players.add(new ComputerPlayer(playerName));
            } else {
                players.add(new Player(playerName));
            }
        }
    }

    private void executeMove(final Move move, final Player player) throws SplendorException {
        final TurnController turnController = new TurnController(game, gameView);
        turnController.executeMove(move, player);
        game.addRecentMove(MoveFormatter.formatMoveEntry(player, move));
    }

    private void checkNobleVisits(final Player player) throws SplendorException {
        final PlayerController playerController = new PlayerController(game, gameView);
        playerController.checkNobleVisits(player);
    }

    private void displayGameResults() throws SplendorException {
        if (game.getWinner() == null) {
            throw new GameStateException("No winner determined for finished game");
        }
        final Map<String, Integer> finalScores = new HashMap<>();
        for (final Player player : game.getPlayers()) {
            finalScores.put(player.getName(), player.getTotalPoints());
        }
        gameView.displayWinner(game.getWinner(), finalScores);
    }

    public Game getGame() { return game; }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
}
