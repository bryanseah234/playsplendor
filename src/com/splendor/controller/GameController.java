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
import com.splendor.view.Colors;
import com.splendor.view.IGameView;
import java.util.*;

/**
 * Main controller that orchestrates the game flow.
 * Manages game initialization, turn handling, and win condition checking.
 */
public class GameController {

    private static final List<Gem> GEM_ORDER = List.of(
            Gem.WHITE, Gem.BLUE, Gem.GREEN, Gem.RED, Gem.BLACK, Gem.GOLD);
    private final IGameView gameView;
    private final IConfigProvider configProvider;
    private final MoveValidator moveValidator;
    private final GameRuleValidator gameRuleValidator;
    private Game game;
    private List<Player> players;

    /**
     * Creates a new GameController with the specified view and configuration.
     * 
     * @param gameView       Game view implementation
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

            gameView.displayNotification("Game initialized successfully!");

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
            gameView.displayNotification("Starting game...");

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
            // Save game state before anything else for undo
            game.saveUndoState();

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

            // Give user chance to undo before advancing
            final String input = gameView.displayMessage("Move executed successfully!");
            if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                if (game.undo()) {
                    gameView.displayNotification("Move undone!");
                    return; // Re-process same turn
                } else {
                    gameView.displayError("Nothing to undo!");
                }
            }

            // Advance to next player
            game.advanceToNextPlayer();
    

        } catch (final GameStateException e) {
            if ("UNDO_SIGNAL".equals(e.getMessage())) {
                gameView.displayNotification("Turn reverted!");
                return;
            }
            throw e;
        } catch (final InvalidMoveException | InsufficientTokensException e) {
            // Display error and let player try again
            final String input = gameView.displayError(e.getMessage());
            if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                if (game.undo()) {
                    gameView.displayNotification("Turn reverted!");
                    return;
                }
            }
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
                final List<MenuOption> options = buildMenuOptions(player, game);
                final Move move = gameView.promptForMove(player, game, options);

                // Validate move
                moveValidator.validateMove(move, player, game);

                return move;

            } catch (final InvalidMoveException | InsufficientTokensException e) {
                final String input = gameView.displayError(e.getMessage());
                if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
                    if (game.undo()) {
                        throw new GameStateException("UNDO_SIGNAL"); // Custom skip
                    }
                }
                // Let player try again
            } catch (final Exception e) {
                throw new SplendorException("Failed to get player move: " + e.getMessage(), e);
            }
        }
    }

    private List<MenuOption> buildMenuOptions(final Player player, final Game game) {
        final Board board = game.getBoard();
        final List<MenuOption> options = new ArrayList<>();
        int index = 1;

        final List<Gem> threeDifferent = getAvailableDifferentGems(board);
        final boolean canTakeThree = threeDifferent.size() >= 3;
        options.add(new MenuOption(index++, MenuAction.TAKE_THREE, canTakeThree,
                "Take 3 different", formatColoredGemList(threeDifferent),
                canTakeThree ? "" : "Need 3 colors in bank"));

        final List<Gem> twoSame = getAvailableTwoSameGems(board);
        final boolean canTakeTwo = !twoSame.isEmpty();
        options.add(new MenuOption(index++, MenuAction.TAKE_TWO, canTakeTwo,
                "Take 2 same", formatColoredGemList(twoSame), canTakeTwo ? "" : "No color with 4+ tokens"));

        final boolean canReserve = player.canReserveCard();
        final boolean canReserveVisible = canReserve && hasVisibleCards(board);
        final boolean canReserveDeck = canReserve && hasAnyDeckCards(board);
        final List<Integer> visibleIds = getVisibleCardIds(board);
        options.add(new MenuOption(index++, MenuAction.RESERVE_VISIBLE, canReserveVisible,
                "Reserve visible card", canReserveVisible ? formatIdList(visibleIds, 8) : "None",
                canReserveVisible ? "" : reserveVisibleReason(player, board)));
        final List<Integer> availableTiers = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) {
                availableTiers.add(tier);
            }
        }
        final String deckInfo = availableTiers.isEmpty() ? "None" : formatIdList(availableTiers, 3).replace(", ", "/");
        options.add(new MenuOption(index++, MenuAction.RESERVE_DECK, canReserveDeck && !availableTiers.isEmpty(),
                "Reserve card from deck", deckInfo,
                canReserveDeck ? (availableTiers.isEmpty() ? "Decks are empty" : "") : reserveDeckReason(player, board)));

        final List<Integer> affordableVisible = getAffordableVisibleIds(player, board);
        final boolean canBuyVisible = !affordableVisible.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_VISIBLE, canBuyVisible,
                "Buy visible card", canBuyVisible ? formatIdList(affordableVisible, 8) : "None",
                canBuyVisible ? "" : buyVisibleReason(player, board)));

        final List<Integer> reservedIds = getReservedCardIds(player);
        final List<Integer> affordableReserved = getAffordableReservedIds(player);
        final boolean canBuyReserved = !affordableReserved.isEmpty();
        options.add(new MenuOption(index++, MenuAction.BUY_RESERVED, canBuyReserved,
                "Buy reserved card(s)", reservedIds.isEmpty() ? "None" : formatIdList(reservedIds, 8),
                canBuyReserved ? "" : buyReservedReason(player)));

        return options;
    }

    private String formatColoredGemList(final List<Gem> gems) {
        if (gems.isEmpty()) {
            return "None";
        }
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)));
        }
        return joiner.toString();
    }

    private String formatIdList(final List<Integer> ids, final int maxCount) {
        if (ids.isEmpty()) {
            return "-";
        }
        final StringJoiner joiner = new StringJoiner(", ");
        final int limit = Math.min(ids.size(), maxCount);
        for (int i = 0; i < limit; i++) {
            joiner.add(String.valueOf(ids.get(i)));
        }
        if (ids.size() > maxCount) {
            joiner.add("...");
        }
        return joiner.toString();
    }

    private List<Gem> getAvailableDifferentGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) > 0) {
                gems.add(gem);
            }
        }
        return gems;
    }

    private List<Gem> getAvailableTwoSameGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) >= 4) {
                gems.add(gem);
            }
        }
        return gems;
    }

    private boolean hasVisibleCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyDeckCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) {
                return true;
            }
        }
        return false;
    }

    private String reserveVisibleReason(final Player player, final Board board) {
        if (!player.canReserveCard()) {
            return "Reserve limit reached (3)";
        }
        if (!hasVisibleCards(board)) {
            return "No visible cards";
        }
        return "Not available";
    }

    private String reserveDeckReason(final Player player, final Board board) {
        if (!player.canReserveCard()) {
            return "Reserve limit reached (3)";
        }
        if (!hasAnyDeckCards(board)) {
            return "Decks are empty";
        }
        return "Not available";
    }

    private String buyVisibleReason(final Player player, final Board board) {
        if (!hasVisibleCards(board)) {
            return "No visible cards";
        }
        return "Need more tokens";
    }

    private String buyReservedReason(final Player player) {
        if (player.getReservedCards().isEmpty()) {
            return "No reserved cards";
        }
        return "Need more tokens";
    }

    private List<Integer> getAffordableVisibleIds(final Player player, final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) {
                    ids.add(card.getId());
                }
            }
        }
        return ids;
    }

    private List<Integer> getAffordableReservedIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) {
                ids.add(card.getId());
            }
        }
        return ids;
    }

    private List<Integer> getVisibleCardIds(final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                ids.add(card.getId());
            }
        }
        return ids;
    }

    private List<Integer> getReservedCardIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            ids.add(card.getId());
        }
        return ids;
    }

    /**
     * Executes the specified move for the player.
     * 
     * @param move   Move to execute
     * @param player Player executing the move
     * @throws SplendorException if move execution fails
     */
    private void executeMove(final Move move, final Player player) throws SplendorException {
        final TurnController turnController = new TurnController(game, gameView);
        turnController.executeMove(move, player);
        game.addRecentMove(formatMoveEntry(player, move));
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
            game.addRecentMove(formatMoveEntry(player, discardMove));
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

    private String formatMoveEntry(final Player player, final Move move) {
        final StringBuilder sb = new StringBuilder();
        sb.append(player.getName()).append(": ").append(move.getMoveType().getDisplayName());
        if (move.hasGemSelection()) {
            sb.append(" ").append(formatGemCounts(move.getSelectedGems()));
        }
        if (move.hasCardSelection()) {
            sb.append(" Card ").append(move.getCardId());
            if (move.isReservedCard()) {
                sb.append(" (Res)");
            }
        } else if (move.hasDeckSelection()) {
            sb.append(" Deck ").append(move.getDeckTier());
        }
        return sb.toString();
    }

    private String formatGemCounts(final Map<Gem, Integer> counts) {
        final StringBuilder sb = new StringBuilder();
        for (final Gem gem : GEM_ORDER) {
            final int count = counts.getOrDefault(gem, 0);
            if (count > 0) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem))).append(count);
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private String gemLabel(final Gem gem) {
        if (gem == Gem.WHITE) {
            return "W";
        }
        if (gem == Gem.BLUE) {
            return "B";
        }
        if (gem == Gem.GREEN) {
            return "G";
        }
        if (gem == Gem.RED) {
            return "R";
        }
        if (gem == Gem.BLACK) {
            return "K";
        }
        if (gem == Gem.GOLD) {
            return "Au";
        }
        return "";
    }
}
