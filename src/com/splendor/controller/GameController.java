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
                // Bot turn: prompt via the view so it works both locally AND over the network
                gameView.displayMessage(currentPlayer.getName() + " finished their turn.");
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
            if (input != null && (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO"))) {
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
                final List<MenuOption> options = buildMenuOptions(player, game);
                
                // --- NEW CPU BOT LOGIC ---
                if (player instanceof ComputerPlayer) {
                    gameView.displayNotification(player.getName() + " is calculating a move...");
                    try { Thread.sleep(1500); } catch (InterruptedException e) {}

                    Move botMove = null;

                    // 1. Try to buy a visible card first
                    final List<Integer> affordableVisible = getAffordableVisibleIds(player, game.getBoard());
                    if (!affordableVisible.isEmpty()) {
                        botMove = new Move(MoveType.BUY_CARD, affordableVisible.get(0), false);
                    } 
                    // 2. Try to buy a reserved card next
                    else if (!getAffordableReservedIds(player).isEmpty()) {
                        botMove = new Move(MoveType.BUY_CARD, getAffordableReservedIds(player).get(0), true);
                    } 
                    // 3. Try to take 3 different gems
                    else if (getAvailableDifferentGems(game.getBoard()).size() >= 3) {
                        final List<Gem> diffGems = getAvailableDifferentGems(game.getBoard());
                        final Map<Gem, Integer> gems = new HashMap<>();
                        gems.put(diffGems.get(0), 1);
                        gems.put(diffGems.get(1), 1);
                        gems.put(diffGems.get(2), 1);
                        botMove = new Move(MoveType.TAKE_THREE_DIFFERENT, gems);
                    } 
                    // 4. Try to take 2 of the same gem
                    else if (!getAvailableTwoSameGems(game.getBoard()).isEmpty()) {
                        final List<Gem> sameGems = getAvailableTwoSameGems(game.getBoard());
                        final Map<Gem, Integer> gems = new HashMap<>();
                        gems.put(sameGems.get(0), 2);
                        botMove = new Move(MoveType.TAKE_TWO_SAME, gems);
                    } 
                    // 5. Fallback: Reserve from deck
                    else {
                        botMove = Move.reserveFromDeck(1);
                    }

                    moveValidator.validateMove(botMove, player, game);
                    return botMove;
                }
                // --- END BOT LOGIC ---

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

            // --- NEW CPU BOT LOGIC ---
            if (player instanceof ComputerPlayer) {
                final Map<Gem, Integer> discardMap = new HashMap<>();
                int leftToDiscard = excessCount;
                for (final Gem gem : Gem.values()) {
                    final int count = player.getTokenCount(gem);
                    if (count > 0 && leftToDiscard > 0) {
                        final int toDiscard = Math.min(count, leftToDiscard);
                        discardMap.put(gem, toDiscard);
                        leftToDiscard -= toDiscard;
                    }
                }
                discardMove = new Move(MoveType.DISCARD_TOKENS, discardMap);
            } else {
                discardMove = gameView.promptForTokenDiscard(player, excessCount);
            }
            // --- END BOT LOGIC ---

            moveValidator.validateMove(discardMove, player, game);
            final PlayerController playerController = new PlayerController(game, gameView);
            playerController.executeTokenDiscard(player, discardMove);
            game.addRecentMove(formatMoveEntry(player, discardMove));
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
        if (gems.isEmpty()) return "None";
        final StringJoiner joiner = new StringJoiner(" ");
        for (final Gem gem : gems) {
            joiner.add(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem)));
        }
        return joiner.toString();
    }

    private String formatIdList(final List<Integer> ids, final int maxCount) {
        if (ids.isEmpty()) return "-";
        final StringJoiner joiner = new StringJoiner(", ");
        final int limit = Math.min(ids.size(), maxCount);
        for (int i = 0; i < limit; i++) {
            joiner.add(String.valueOf(ids.get(i)));
        }
        if (ids.size() > maxCount) joiner.add("...");
        return joiner.toString();
    }

    private List<Gem> getAvailableDifferentGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) > 0) gems.add(gem);
        }
        return gems;
    }

    private List<Gem> getAvailableTwoSameGems(final Board board) {
        final List<Gem> gems = new ArrayList<>();
        for (final Gem gem : Gem.values()) {
            if (gem != Gem.GOLD && board.getGemCount(gem) >= 4) gems.add(gem);
        }
        return gems;
    }

    private boolean hasVisibleCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (!board.getAvailableCards(tier).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasAnyDeckCards(final Board board) {
        for (int tier = 1; tier <= 3; tier++) {
            if (board.getDeckSize(tier) > 0) return true;
        }
        return false;
    }

    private String reserveVisibleReason(final Player player, final Board board) {
        if (!player.canReserveCard()) return "Reserve limit reached (3)";
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Not available";
    }

    private String reserveDeckReason(final Player player, final Board board) {
        if (!player.canReserveCard()) return "Reserve limit reached (3)";
        if (!hasAnyDeckCards(board)) return "Decks are empty";
        return "Not available";
    }

    private String buyVisibleReason(final Player player, final Board board) {
        if (!hasVisibleCards(board)) return "No visible cards";
        return "Need more tokens";
    }

    private String buyReservedReason(final Player player) {
        if (player.getReservedCards().isEmpty()) return "No reserved cards";
        return "Need more tokens";
    }

    private List<Integer> getAffordableVisibleIds(final Player player, final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) {
                if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
            }
        }
        return ids;
    }

    private List<Integer> getAffordableReservedIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) {
            if (moveValidator.canPlayerAffordCard(player, card)) ids.add(card.getId());
        }
        return ids;
    }

    private List<Integer> getVisibleCardIds(final Board board) {
        final List<Integer> ids = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            for (final Card card : board.getAvailableCards(tier)) ids.add(card.getId());
        }
        return ids;
    }

    private List<Integer> getReservedCardIds(final Player player) {
        final List<Integer> ids = new ArrayList<>();
        for (final Card card : player.getReservedCards()) ids.add(card.getId());
        return ids;
    }

    private void executeMove(final Move move, final Player player) throws SplendorException {
        final TurnController turnController = new TurnController(game, gameView);
        turnController.executeMove(move, player);
        game.addRecentMove(formatMoveEntry(player, move));
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

    private String formatMoveEntry(final Player player, final Move move) {
        final StringBuilder sb = new StringBuilder();
        sb.append(player.getName()).append(": ").append(move.getMoveType().getDisplayName());
        if (move.hasGemSelection()) sb.append(" ").append(formatGemCounts(move.getSelectedGems()));
        if (move.hasCardSelection()) {
            sb.append(" Card ").append(move.getCardId());
            if (move.isReservedCard()) sb.append(" (Res)");
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
                if (sb.length() > 0) sb.append(" ");
                sb.append(Colors.colorize(gemLabel(gem), Colors.getGemColor(gem))).append(count);
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private String gemLabel(final Gem gem) {
        if (gem == Gem.WHITE) return "W";
        if (gem == Gem.BLUE) return "B";
        if (gem == Gem.GREEN) return "G";
        if (gem == Gem.RED) return "R";
        if (gem == Gem.BLACK) return "K";
        if (gem == Gem.GOLD) return "Au";
        return "";
    }
}