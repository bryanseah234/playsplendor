package com.splendor.model;

/**
 * An AI-controlled player in the Splendor game.
 * Extends Player with no additional state; the bot decision logic
 * lives in BotStrategy in the controller layer.
 */
public class ComputerPlayer extends Player {

    /**
     * Creates a new computer-controlled player with the given name.
     *
     * @param name Display name for this bot player
     */
    public ComputerPlayer(final String name) {
        super(name);
    }
}
