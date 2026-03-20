package com.splendor.view.tui;

import com.splendor.model.Game;
import com.splendor.model.MenuOption;
import com.splendor.model.Noble;
import com.splendor.model.Player;
import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.QuitMessage;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.WindowSizeMessage;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class TuiAppModel implements Model {
    private final BlockingQueue<TuiRequest> requestQueue;
    private final BlockingQueue<String> responseQueue;
    private final StatusBarRenderer statusBarRenderer;
    private final HelpBarRenderer helpBarRenderer;
    private final CompactBoardRenderer compactBoardRenderer;

    private ScreenState screenState;
    private String boardRender;
    private String messageText;
    private String inputBuffer;
    private String promptLabel;
    private int menuCursor;
    private List<MenuOption> menuOptions;
    private List<Noble> nobleChoices;
    private Player currentPlayer;
    private Game currentGame;
    private int discardCount;
    private int terminalWidth;
    private int terminalHeight;
    private int boardVerticalOffset;

    public TuiAppModel(final BlockingQueue<TuiRequest> requestQueue,
                       final BlockingQueue<String> responseQueue) {
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        this.statusBarRenderer = new StatusBarRenderer();
        this.helpBarRenderer = new HelpBarRenderer();
        this.compactBoardRenderer = new CompactBoardRenderer();
        this.screenState = ScreenState.WELCOME;
        this.inputBuffer = "";
        this.promptLabel = "";
        this.menuCursor = 0;
        this.menuOptions = List.of();
        this.nobleChoices = List.of();
        this.messageText = "";
        this.boardRender = "";
        this.terminalWidth = 160;
        this.terminalHeight = 48;
        this.boardVerticalOffset = 0;
    }

    @Override
    public Command init() {
        return TuiAppModel::pollRequest;
    }

    @Override
    public UpdateResult<? extends Model> update(final Message msg) {
        if (msg instanceof QuitMessage) {
            return UpdateResult.from(this, Command.quit());
        }

        if (msg instanceof WindowSizeMessage ws) {
            terminalWidth = Math.max(60, ws.width());
            terminalHeight = Math.max(12, ws.height());
            refreshBoardRender();
            return UpdateResult.from(this);
        }

        if (msg instanceof RequestMessage rm) {
            return handleRequest(rm.request());
        }

        if (msg instanceof KeyPressMessage key) {
            return handleKey(key);
        }

        return UpdateResult.from(this);
    }

    @Override
    public String view() {
        return statusBarRenderer.render(currentGame, terminalWidth)
                + "\n"
                + mainContent()
                + "\n"
                + helpBarRenderer.render(screenState, terminalWidth);
    }

    private String mainContent() {
        return switch (screenState) {
            case WELCOME -> viewWelcome();
            case SETUP_PLAYER_COUNT, SETUP_PLAYER_NAME -> viewSetupPrompt();
            case PLAYING -> viewPlaying();
            case PROMPT_TAKE_THREE, PROMPT_TAKE_TWO, PROMPT_CARD_ID, PROMPT_DECK_TIER -> viewSubPrompt();
            case DISCARD_TOKENS -> viewDiscard();
            case NOBLE_CHOICE -> viewNobleChoice();
            case MESSAGE, ERROR -> viewMessage();
            case NOTIFICATION -> viewPlaying();
            case GAME_OVER -> viewGameOver();
        };
    }

    private UpdateResult<? extends Model> handleRequest(final TuiRequest req) {
        inputBuffer = "";
        switch (req.type()) {
            case DISPLAY_WELCOME -> {
                screenState = ScreenState.WELCOME;
                messageText = "Welcome to Splendor!";
                respond("");
                return UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case PROMPT_PLAYER_COUNT -> {
                screenState = ScreenState.SETUP_PLAYER_COUNT;
                promptLabel = "Number of players (2-4): ";
            }
            case PROMPT_PLAYER_NAME -> {
                screenState = ScreenState.SETUP_PLAYER_NAME;
                promptLabel = req.payload() != null ? req.payload() : "Player name: ";
            }
            case DISPLAY_GAME_STATE -> {
                if (req.game() != null) {
                    currentGame = req.game();
                    boardVerticalOffset = 0;
                    refreshBoardRender();
                }
                screenState = ScreenState.PLAYING;
                respond("");
                return UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case DISPLAY_AVAILABLE_MOVES -> {
                if (req.game() != null) {
                    currentGame = req.game();
                    boardVerticalOffset = 0;
                    refreshBoardRender();
                }
                menuOptions = req.menuOptions() != null ? req.menuOptions() : List.of();
                menuCursor = 0;
                screenState = ScreenState.PLAYING;
                respond("");
                return UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case PROMPT_MOVE -> {
                menuOptions = req.menuOptions() != null ? req.menuOptions() : List.of();
                menuCursor = firstAvailableIndex();
                screenState = ScreenState.PLAYING;
                if (req.game() != null) {
                    currentGame = req.game();
                    boardVerticalOffset = 0;
                    refreshBoardRender();
                }
                currentPlayer = req.player();
                promptLabel = "Select action: ";
            }
            case PROMPT_SUB_INPUT -> {
                promptLabel = req.payload() != null ? req.payload() : "> ";
                screenState = mapSubPromptState(req);
            }
            case PROMPT_TOKEN_DISCARD -> {
                discardCount = req.intValue();
                currentPlayer = req.player();
                screenState = ScreenState.DISCARD_TOKENS;
                promptLabel = "Discard tokens (" + discardCount + " remaining): ";
            }
            case PROMPT_NOBLE_CHOICE -> {
                nobleChoices = req.nobles() != null ? req.nobles() : List.of();
                currentPlayer = req.player();
                screenState = ScreenState.NOBLE_CHOICE;
                menuCursor = 0;
            }
            case DISPLAY_MESSAGE -> {
                messageText = req.payload() != null ? req.payload() : "";
                screenState = ScreenState.MESSAGE;
                promptLabel = "Press Enter to continue...";
            }
            case DISPLAY_ERROR -> {
                messageText = req.payload() != null ? req.payload() : "";
                screenState = ScreenState.ERROR;
                promptLabel = "Press Enter to continue...";
            }
            case DISPLAY_NOTIFICATION -> {
                messageText = req.payload() != null ? req.payload() : "";
                respond("");
                return UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case DISPLAY_WINNER -> {
                messageText = req.payload() != null ? req.payload() : "Game Over!";
                screenState = ScreenState.GAME_OVER;
            }
            case WAIT_FOR_ENTER -> {
                screenState = ScreenState.MESSAGE;
                messageText = req.payload() != null ? req.payload() : "";
                promptLabel = "Press Enter to continue...";
            }
            case DISPLAY_PLAYER_TURN -> {
                if (req.player() != null) {
                    currentPlayer = req.player();
                }
                respond("");
                return UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case CLOSE -> {
                return UpdateResult.from(this, Command.quit());
            }
        }
        return UpdateResult.from(this);
    }

    private UpdateResult<? extends Model> handleKey(final KeyPressMessage key) {
        final String k = key.key();

        if ("ctrl+c".equals(k)) {
            respond("EXIT");
            return UpdateResult.from(this, Command.quit());
        }

        if ("pgup".equals(k) || "ctrl+u".equals(k)) {
            boardVerticalOffset = Math.max(0, boardVerticalOffset - 4);
            refreshBoardRender();
            return UpdateResult.from(this);
        }
        if ("pgdown".equals(k) || "ctrl+d".equals(k)) {
            boardVerticalOffset = Math.min(boardVerticalOffset + 4, compactBoardRenderer.maxVerticalOffset());
            refreshBoardRender();
            return UpdateResult.from(this);
        }

        return switch (screenState) {
            case PLAYING -> handlePlayingKey(k);
            case NOBLE_CHOICE -> handleNobleChoiceKey(k);
            case SETUP_PLAYER_COUNT, SETUP_PLAYER_NAME,
                    PROMPT_TAKE_THREE, PROMPT_TAKE_TWO,
                    PROMPT_CARD_ID, PROMPT_DECK_TIER,
                    DISCARD_TOKENS -> handleTextInputKey(k);
            case MESSAGE, ERROR -> handleEnterKey(k);
            case GAME_OVER -> handleGameOverKey(k);
            default -> UpdateResult.from(this);
        };
    }

    private UpdateResult<? extends Model> handlePlayingKey(final String k) {
        if (menuOptions.isEmpty()) {
            return UpdateResult.from(this);
        }
        return switch (k) {
            case "up", "k" -> {
                moveCursorUp();
                yield UpdateResult.from(this);
            }
            case "down", "j" -> {
                moveCursorDown();
                yield UpdateResult.from(this);
            }
            case "enter" -> {
                final MenuOption selected = getSelectedOption();
                if (selected != null && selected.isAvailable()) {
                    respond(String.valueOf(selected.getNumber()));
                    yield UpdateResult.from(this, TuiAppModel::pollRequest);
                }
                yield UpdateResult.from(this);
            }
            case "z", "Z" -> {
                respond("Z");
                yield UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            case "q", "Q" -> {
                respond("EXIT");
                yield UpdateResult.from(this, Command.quit());
            }
            default -> UpdateResult.from(this);
        };
    }

    private UpdateResult<? extends Model> handleNobleChoiceKey(final String k) {
        return switch (k) {
            case "up", "k" -> {
                if (menuCursor > 0) {
                    menuCursor--;
                }
                yield UpdateResult.from(this);
            }
            case "down", "j" -> {
                if (menuCursor < nobleChoices.size() - 1) {
                    menuCursor++;
                }
                yield UpdateResult.from(this);
            }
            case "enter" -> {
                respond(String.valueOf(menuCursor + 1));
                yield UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            default -> UpdateResult.from(this);
        };
    }

    private UpdateResult<? extends Model> handleTextInputKey(final String k) {
        return switch (k) {
            case "enter" -> {
                final String value = inputBuffer.trim();
                if (!value.isEmpty()) {
                    respond(value);
                    inputBuffer = "";
                    yield UpdateResult.from(this, TuiAppModel::pollRequest);
                }
                yield UpdateResult.from(this);
            }
            case "backspace" -> {
                if (!inputBuffer.isEmpty()) {
                    inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
                }
                yield UpdateResult.from(this);
            }
            case "escape" -> {
                respond("Z");
                inputBuffer = "";
                yield UpdateResult.from(this, TuiAppModel::pollRequest);
            }
            default -> {
                if (k.length() == 1) {
                    inputBuffer += k;
                }
                yield UpdateResult.from(this);
            }
        };
    }

    private UpdateResult<? extends Model> handleEnterKey(final String k) {
        if ("enter".equals(k)) {
            respond(inputBuffer.trim());
            inputBuffer = "";
            return UpdateResult.from(this, TuiAppModel::pollRequest);
        }
        if ("z".equals(k) || "Z".equals(k)) {
            respond("Z");
            inputBuffer = "";
            return UpdateResult.from(this, TuiAppModel::pollRequest);
        }
        return UpdateResult.from(this);
    }

    private UpdateResult<? extends Model> handleGameOverKey(final String k) {
        if ("enter".equals(k) || "q".equals(k) || "Q".equals(k)) {
            respond("");
            return UpdateResult.from(this, Command.quit());
        }
        return UpdateResult.from(this);
    }

    private String viewWelcome() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                           SPLENDOR                           ║\n");
        sb.append("║                        TUI4J Edition                         ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");
        if (!messageText.isEmpty()) {
            sb.append(messageText).append("\n");
        }
        return sb.toString().trim();
    }

    private String viewSetupPrompt() {
        final StringBuilder sb = new StringBuilder();
        sb.append("── Game Setup ──\n\n");
        if (screenState == ScreenState.SETUP_PLAYER_COUNT) {
            sb.append("How many players will be playing?\n");
            sb.append("Include 'bot' in a name to make a CPU player.\n\n");
        }
        sb.append(inputPanel(promptLabel, inputBuffer));
        return sb.toString();
    }

    private String viewPlaying() {
        final int reserved = 6 + Math.max(0, menuOptions.size());
        boardRender = renderBoard(reserved);
        final StringBuilder sb = new StringBuilder();
        sb.append(boardRender);
        if (!messageText.isEmpty()) {
            sb.append("\n").append(messageText);
            messageText = "";
        }
        if (!menuOptions.isEmpty()) {
            sb.append("\n").append(inputDivider("Actions")).append("\n");
            for (int i = 0; i < menuOptions.size(); i++) {
                final MenuOption opt = menuOptions.get(i);
                final String prefix = i == menuCursor ? " ▶ " : "   ";
                final String label = opt.getNumber() + ") " + opt.getLabel() + ": " + opt.getDetail();
                if (!opt.isAvailable()) {
                    sb.append(prefix).append(label);
                    if (!opt.getReason().isBlank()) {
                        sb.append(" (").append(opt.getReason()).append(")");
                    }
                } else {
                    sb.append(prefix).append(label);
                }
                sb.append("\n");
            }
            sb.append(inputDivider("Select with ↑/↓ then Enter")).append("\n");
        }
        return sb.toString().trim();
    }

    private String viewSubPrompt() {
        boardRender = renderBoard(8);
        return boardRender + "\n" + inputPanel(promptLabel, inputBuffer);
    }

    private String viewDiscard() {
        boardRender = renderBoard(10);
        return boardRender
                + "\n!!! TOKEN LIMIT EXCEEDED !!!"
                + "\nYou must discard " + discardCount + " token(s)."
                + "\nFormat: COLOR QUANTITY (e.g., R 1 or Red 1)\n"
                + inputPanel(promptLabel, inputBuffer);
    }

    private String viewNobleChoice() {
        boardRender = renderBoard(6 + nobleChoices.size());
        final StringBuilder sb = new StringBuilder();
        sb.append(boardRender).append("\n").append(inputDivider("Choose a Noble")).append("\n");
        for (int i = 0; i < nobleChoices.size(); i++) {
            final Noble noble = nobleChoices.get(i);
            final String prefix = i == menuCursor ? " ▶ " : "   ";
            final String label = "Noble " + noble.getId() + " - " + noble.getPoints() + " pts - " + noble.getRequirements();
            sb.append(prefix).append(label).append("\n");
        }
        return sb.toString().trim();
    }

    private String viewMessage() {
        boardRender = renderBoard(8);
        final StringBuilder sb = new StringBuilder();
        if (!boardRender.isEmpty()) {
            sb.append(boardRender).append("\n");
        }
        sb.append(inputDivider("Message")).append("\n");
        sb.append(messageText).append("\n\n");
        sb.append(promptLabel);
        return sb.toString().trim();
    }

    private String viewGameOver() {
        return "\n"
                + "══════════════════════════════════════════════════\n"
                + "                   GAME OVER\n"
                + "══════════════════════════════════════════════════\n\n"
                + messageText;
    }

    private void refreshBoardRender() {
        if (currentGame == null) {
            return;
        }
        boardRender = compactBoardRenderer.render(currentGame, terminalWidth, Math.max(8, terminalHeight - 6), boardVerticalOffset);
        if (boardVerticalOffset > compactBoardRenderer.maxVerticalOffset()) {
            boardVerticalOffset = compactBoardRenderer.maxVerticalOffset();
            boardRender = compactBoardRenderer.render(currentGame, terminalWidth, Math.max(8, terminalHeight - 6), boardVerticalOffset);
        }
    }

    private String renderBoard(final int reservedLines) {
        if (currentGame == null) {
            return "";
        }
        final int boardHeight = Math.max(8, terminalHeight - reservedLines);
        return compactBoardRenderer.render(currentGame, terminalWidth, boardHeight, boardVerticalOffset);
    }

    private static Message pollRequest() {
        return RequestMessage.poll();
    }

    private void respond(final String value) {
        responseQueue.offer(value);
    }

    private void moveCursorUp() {
        if (menuOptions.isEmpty()) {
            return;
        }
        int next = menuCursor - 1;
        while (next >= 0 && !menuOptions.get(next).isAvailable()) {
            next--;
        }
        if (next >= 0) {
            menuCursor = next;
        }
    }

    private void moveCursorDown() {
        if (menuOptions.isEmpty()) {
            return;
        }
        int next = menuCursor + 1;
        while (next < menuOptions.size() && !menuOptions.get(next).isAvailable()) {
            next++;
        }
        if (next < menuOptions.size()) {
            menuCursor = next;
        }
    }

    private int firstAvailableIndex() {
        for (int i = 0; i < menuOptions.size(); i++) {
            if (menuOptions.get(i).isAvailable()) {
                return i;
            }
        }
        return 0;
    }

    private MenuOption getSelectedOption() {
        if (menuCursor >= 0 && menuCursor < menuOptions.size()) {
            return menuOptions.get(menuCursor);
        }
        return null;
    }

    private ScreenState mapSubPromptState(final TuiRequest req) {
        if (req.payload() == null) {
            return ScreenState.PROMPT_CARD_ID;
        }
        final String p = req.payload().toLowerCase();
        if (p.contains("3 color") || p.contains("pick 3")) {
            return ScreenState.PROMPT_TAKE_THREE;
        }
        if (p.contains("1 color") || p.contains("pick 1")) {
            return ScreenState.PROMPT_TAKE_TWO;
        }
        if (p.contains("tier")) {
            return ScreenState.PROMPT_DECK_TIER;
        }
        return ScreenState.PROMPT_CARD_ID;
    }

    private String inputDivider(final String title) {
        final int w = Math.max(30, terminalWidth - 2);
        final String text = " " + title + " ";
        final int side = Math.max(0, (w - text.length()) / 2);
        final String left = "-".repeat(side);
        final String right = "-".repeat(Math.max(0, w - text.length() - side));
        return left + text + right;
    }

    private String inputPanel(final String prompt, final String value) {
        final int w = Math.max(30, terminalWidth - 4);
        final String top = "+" + "-".repeat(w + 2) + "+";
        final String title = "| " + padRight("INPUT", w) + " |";
        final String sep = "+" + "-".repeat(w + 2) + "+";
        final String content = "| " + padRight((prompt == null ? "" : prompt) + (value == null ? "" : value) + "▌", w) + " |";
        return top + "\n" + title + "\n" + sep + "\n" + content + "\n" + top;
    }

    private String padRight(final String s, final int width) {
        final String text = s == null ? "" : s;
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }
}
