package com.splendor.model;

/**
 * An immutable data object that describes a single entry in the player's action menu.
 *
 * The controller builds a list of these each turn and hands it to the view, keeping
 * presentation details (numbering, labels, disabled-reason text) out of both the model
 * and the view logic. The view simply renders what it receives.
 */
public class MenuOption {

    private final int number;       // the 1-based selection number shown to the player
    private final MenuAction action; // the game action this option maps to
    private final boolean available; // false when the rule conditions for this action are not met
    private final String label;     // short human-readable name displayed in the menu
    private final String detail;    // supplementary context (e.g. available card IDs or gem colors)
    private final String reason;    // non-empty explanation shown when available == false

    /**
     * Creates a fully specified menu option.
     *
     * @param number    The 1-based index displayed to the player.
     * @param action    The game action this option represents.
     * @param available Whether the player is currently allowed to take this action.
     * @param label     Short display name for the action (e.g. "Take 3 different").
     * @param detail    Extra context string rendered alongside the label (e.g. list of card IDs).
     * @param reason    Human-readable explanation of why this option is unavailable; empty when available.
     */
    public MenuOption(final int number, final MenuAction action, final boolean available,
                      final String label, final String detail, final String reason) {
        this.number = number;
        this.action = action;
        this.available = available;
        this.label = label;
        this.detail = detail;
        this.reason = reason;
    }

    /**
     * Returns the 1-based menu index used by the player to select this option.
     *
     * @return Menu selection number.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the game action that this menu entry maps to.
     *
     * @return The corresponding MenuAction enum value.
     */
    public MenuAction getAction() {
        return action;
    }

    /**
     * Returns whether the player is currently allowed to choose this action.
     * When false, the view should render the option as greyed-out and show getReason().
     *
     * @return true if the action is legal for the player right now.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Returns the short display name for this menu entry.
     *
     * @return Human-readable action label (e.g. "Buy visible card").
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns supplementary context rendered alongside the label.
     * Examples: a list of affordable card IDs, available gem colors.
     *
     * @return Detail string; may be empty but never null.
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Returns the explanation shown when this option is unavailable.
     * Empty string when the option is currently available.
     *
     * @return Reason text (e.g. "Need 3 colors in bank").
     */
    public String getReason() {
        return reason;
    }
}
