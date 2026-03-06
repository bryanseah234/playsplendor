/**
 * Network-based implementation of the game view.
 * Provides remote client display and input handling for network gameplay.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.view;

import com.splendor.model.*;
import com.splendor.network.NetworkProtocol;
import com.splendor.util.GameLogger;
import java.util.HashMap;
import java.util.Map;

/**
 * Network-based implementation of IGameView for remote clients.
 * Sends game state and receives moves via network protocol.
 */
public class RemoteView implements IGameView {
    
    private final String clientId;
    private final NetworkMessageHandler messageHandler;
    
    /**
     * Creates a new RemoteView for the specified client.
     * 
     * @param clientId Client identifier
     * @param messageHandler Network message handler
     */
    public RemoteView(final String clientId, final NetworkMessageHandler messageHandler) {
        this.clientId = clientId;
        this.messageHandler = messageHandler;
    }
    
    @Override
    public void displayGameState(final Game game) {
        final String gameState = formatGameState(game);
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("STATE", gameState));
    }
    
    @Override
    public void displayPlayerTurn(final Player player) {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("TURN", player.getName()));
    }
    
    @Override
    public void displayMessage(final String message) {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("MESSAGE", message));
    }
    
    @Override
    public void displayError(final String errorMessage) {
        messageHandler.sendToClient(clientId, NetworkProtocol.createErrorResponse(errorMessage));
    }

    @Override
    public String promptForCommand(final Player player, final Game game) {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("PROMPT_COMMAND", player.getName()));
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        return response == null ? "" : response.trim();
    }
    
    @Override
    public Move promptForMove(final Player player, final Game game) {
        // Request move from client
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("PROMPT_MOVE", player.getName()));
        
        // Wait for and parse client response
        final String response = messageHandler.waitForClientResponse(clientId, 30000); // 30 second timeout
        
        if (response == null) {
            displayError("Timeout waiting for move");
            return createDefaultMove();
        }
        
        return parseMoveFromResponse(response);
    }
    
    @Override
    public Move promptForTokenDiscard(final Player player, final int excessCount) {
        // Request discard from client
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("PROMPT_DISCARD", 
            player.getName(), String.valueOf(excessCount)));
        
        // Wait for and parse client response
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        
        if (response == null) {
            displayError("Timeout waiting for discard selection");
            return createDefaultDiscardMove(player, excessCount);
        }
        
        return parseDiscardMoveFromResponse(response);
    }
    
    @Override
    public void displayWinner(final Player winner, final Map<String, Integer> finalScores) {
        final String scores = formatFinalScores(finalScores);
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("WINNER", winner.getName(), scores));
    }
    
    @Override
    public void clearDisplay() {
        // Network clients handle their own display clearing
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("CLEAR"));
    }
    
    @Override
    public void displayAvailableMoves(final Player player, final Game game) {
        final String availableMoves = formatAvailableMoves(player, game);
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("AVAILABLE_MOVES", availableMoves));
    }
    
    @Override
    public String promptForPlayerName(final int playerNumber, final int totalPlayers) {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("PROMPT_NAME", 
            String.valueOf(playerNumber), String.valueOf(totalPlayers)));
        
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        
        if (response == null || response.trim().isEmpty()) {
            return "Player" + playerNumber; // Default name
        }
        
        return response.trim();
    }
    
    @Override
    public int promptForPlayerCount() {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("PROMPT_PLAYER_COUNT"));
        
        final String response = messageHandler.waitForClientResponse(clientId, 30000);
        
        try {
            return Integer.parseInt(response.trim());
        } catch (final NumberFormatException e) {
            displayError("Invalid player count received, defaulting to 2");
            return 2;
        }
    }
    
    @Override
    public void displayWelcomeMessage() {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("WELCOME", 
            "Welcome to Splendor Network Game!"));
    }
    
    @Override
    public void close() {
        messageHandler.sendToClient(clientId, NetworkProtocol.createMessage("DISCONNECT"));
        GameLogger.info("Remote view closed for client: " + clientId);
    }
    
    /**
     * Formats the game state for network transmission.
     * 
     * @param game Current game state
     * @return Formatted game state string
     */
    private String formatGameState(final Game game) {
        final StringBuilder state = new StringBuilder();
        
        // Add current player
        state.append("CURRENT_PLAYER:").append(game.getCurrentPlayer().getName()).append(";");
        
        // Add game state
        state.append("STATE:").append(game.getCurrentState()).append(";");
        
        // Add player scores
        for (final Player player : game.getPlayers()) {
            state.append("PLAYER:").append(player.getName())
                 .append(":").append(player.getTotalPoints())
                 .append(":").append(player.getTotalTokenCount())
                 .append(";");
        }
        
        return state.toString();
    }
    
    /**
     * Formats available moves for the player.
     * 
     * @param player Current player
     * @param game Current game state
     * @return Formatted available moves string
     */
    private String formatAvailableMoves(final Player player, final Game game) {
        final StringBuilder moves = new StringBuilder();
        
        // Basic moves always available
        moves.append("TAKE_3_DIFFERENT;TAKE_2_SAME;");
        
        // Conditional moves
        if (player.canReserveCard()) {
            moves.append("RESERVE_CARD;");
        }
        
        if (player.getTotalTokenCount() > 10) {
            moves.append("DISCARD_TOKENS;");
        }
        
        return moves.toString();
    }
    
    /**
     * Formats final scores for display.
     * 
     * @param finalScores Map of player names to scores
     * @return Formatted scores string
     */
    private String formatFinalScores(final Map<String, Integer> finalScores) {
        final StringBuilder scores = new StringBuilder();
        
        for (final Map.Entry<String, Integer> entry : finalScores.entrySet()) {
            if (scores.length() > 0) {
                scores.append(";");
            }
            scores.append(entry.getKey()).append(":").append(entry.getValue());
        }
        
        return scores.toString();
    }
    
    /**
     * Creates a default move when client doesn't respond.
     * 
     * @return Default move
     */
    private Move createDefaultMove() {
        // Default to taking 3 different gems (safest move)
        return new Move(MoveType.TAKE_THREE_DIFFERENT);
    }
    
    /**
     * Creates a default discard move when client doesn't respond.
     * 
     * @param player Player with excess tokens
     * @param excessCount Number of tokens to discard
     * @return Default discard move
     */
    private Move createDefaultDiscardMove(final Player player, final int excessCount) {
        // Default to discarding gold tokens first
        final Map<com.splendor.model.Gem, Integer> discard = new HashMap<>();
        discard.put(com.splendor.model.Gem.GOLD, Math.min(player.getTokenCount(com.splendor.model.Gem.GOLD), excessCount));
        return new Move(MoveType.DISCARD_TOKENS, discard);
    }
    
    /**
     * Parses a move from client response.
     * 
     * @param response Client response
     * @return Parsed move
     */
    private Move parseMoveFromResponse(final String response) {
        // Simple parsing - would be more sophisticated in real implementation
        if (response.contains("TAKE_3")) {
            return new Move(MoveType.TAKE_THREE_DIFFERENT);
        } else if (response.contains("TAKE_2")) {
            return new Move(MoveType.TAKE_TWO_SAME);
        } else if (response.contains("RESERVE")) {
            return new Move(MoveType.RESERVE_CARD);
        } else if (response.contains("BUY")) {
            return new Move(MoveType.BUY_CARD);
        } else {
            return createDefaultMove();
        }
    }
    
    /**
     * Parses a discard move from client response.
     * 
     * @param response Client response
     * @return Parsed discard move
     */
    private Move parseDiscardMoveFromResponse(final String response) {
        // Simple parsing for discard move
        return createDefaultDiscardMove(null, 0); // Would be more sophisticated
    }
    
    /**
     * Network message handler interface for communication.
     */
    public interface NetworkMessageHandler {
        void sendToClient(String clientId, String message);
        String waitForClientResponse(String clientId, int timeoutMs);
    }
}
