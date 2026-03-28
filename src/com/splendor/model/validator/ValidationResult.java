package com.splendor.model.validator;

public class ValidationResult {
    private final boolean valid;
    private final String message;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() { return valid; }
    public String getMessage() { return message; }

    // Static helpers for quick returns
    public static ValidationResult ok() { return new ValidationResult(true, "Action valid."); }
    public static ValidationResult fail(String reason) { return new ValidationResult(false, reason); }
}