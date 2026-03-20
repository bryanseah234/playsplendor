package com.splendor.view.tui;

final class HelpBarRenderer {
    String render(final ScreenState screenState, final int width) {
        final String text = switch (screenState) {
            case PLAYING -> "↑/↓ Navigate • Enter Select • Z Undo • Q Quit • PgUp/PgDn Scroll";
            case NOBLE_CHOICE -> "↑/↓ Navigate • Enter Select";
            case SETUP_PLAYER_COUNT, SETUP_PLAYER_NAME,
                    PROMPT_TAKE_THREE, PROMPT_TAKE_TWO,
                    PROMPT_CARD_ID, PROMPT_DECK_TIER,
                    DISCARD_TOKENS -> "Type Input • Enter Confirm • Esc Back";
            case MESSAGE, ERROR, WELCOME, GAME_OVER -> "Enter Continue • Q Quit";
            case NOTIFICATION -> "Waiting for next update...";
        };
        return fit(text, width);
    }

    private String fit(final String text, final int width) {
        final int w = Math.max(20, width);
        if (text.length() >= w) {
            return text.substring(0, Math.max(1, w - 1)) + "…";
        }
        return text + " ".repeat(w - text.length());
    }
}
