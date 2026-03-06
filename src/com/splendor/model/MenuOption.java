package com.splendor.model;

/**
 * A standard Java class representing an available menu option to present to the user.
 * Built by the Controller to instruct the View on what to render, adhering to MVC.
 */
public class MenuOption {
    private final int number;
    private final MenuAction action;
    private final boolean available;
    private final String label;
    private final String detail;
    private final String reason;

    public MenuOption(final int number, final MenuAction action, final boolean available, 
                      final String label, final String detail, final String reason) {
        this.number = number;
        this.action = action;
        this.available = available;
        this.label = label;
        this.detail = detail;
        this.reason = reason;
    }

    public int getNumber() {
        return number;
    }

    public MenuAction getAction() {
        return action;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getLabel() {
        return label;
    }

    public String getDetail() {
        return detail;
    }

    public String getReason() {
        return reason;
    }
}
