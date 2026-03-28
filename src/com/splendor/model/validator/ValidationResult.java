package com.splendor.model.validator;

/**
 * Immutable value object that carries the outcome of a rule-explanation check.
 *
 * Unlike throwing an exception, ValidationResult lets the caller receive a
 * human-readable message even for invalid moves, so the view can display
 * exactly why an action is disallowed rather than a bare exception class name.
 *
 * Use the static factory methods ok() and fail() rather than the constructor.
 */
public class ValidationResult {

    private final boolean valid;   // true when the move passed all rule checks
    private final String message;  // explanation; "Action valid." for ok(), rule-violation text for fail()

    /**
     * Creates a ValidationResult with an explicit validity flag and message.
     * Prefer the static helpers ok() and fail() for readability at call sites.
     *
     * @param valid   true if the move is permitted; false if it violates a rule.
     * @param message Human-readable explanation of the result.
     */
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * Returns whether the move passed validation.
     *
     * @return true if the move is legal; false otherwise.
     */
    public boolean isValid() { return valid; }

    /**
     * Returns the human-readable result message.
     * This is displayed directly to the player, so it should be clear and concise.
     *
     * @return "Action valid." on success, or the specific rule-violation reason on failure.
     */
    public String getMessage() { return message; }

    /**
     * Factory method for a successful validation result.
     * Use this when no rule was violated.
     *
     * @return A ValidationResult indicating the move is legal.
     */
    public static ValidationResult ok() { return new ValidationResult(true, "Action valid."); }

    /**
     * Factory method for a failed validation result.
     *
     * @param reason Human-readable description of the rule that was violated.
     * @return A ValidationResult indicating why the move is illegal.
     */
    public static ValidationResult fail(String reason) { return new ValidationResult(false, reason); }
}
