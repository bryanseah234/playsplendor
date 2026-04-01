package com.splendor.model;

/**
 * Marker subclass that identifies an AI-controlled participant.
 *
 * ComputerPlayer carries no extra state beyond what Player already tracks.
 * Its sole purpose is to give the rest of the codebase a clean way to
 * distinguish human players from bots via an instanceof check, without
 * scattering boolean flags or strategy fields into the base Player class.
 *
 * The actual decision logic lives in BotStrategy; this class simply signals
 * "this seat belongs to the AI".
 */
public class ComputerPlayer extends Player {

    /**
     * Creates a computer player with the given display name.
     * The name is set at creation time and used in all log/notification output.
     *
     * @param name The bot's display name (e.g. "Bot1", "AngryBot").
     */
    public ComputerPlayer(final String name) {
        super(name);
    }
}
