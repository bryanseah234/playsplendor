/**
 * Safe input parsing utility for handling user input.
 * Provides robust input validation and parsing to prevent crashes.
 */
package com.splendor.util;

import java.util.Scanner;

/**
 * Robust input parsing and validation utility for console-based user interaction.
 * 
 * This class provides a safe wrapper around {@link Scanner} to handle user input
 * with comprehensive validation, error recovery, and helpful error messages. It
 * prevents common input-related crashes such as {@link java.util.InputMismatchException}
 * and {@link java.util.NoSuchElementException}.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Range-bounded integer input with automatic re-prompting</li>
 *   <li>String length validation with min/max constraints</li>
 *   <li>Undo/back signal support via "Z" or "UNDO" keywords</li>
 *   <li>Invisible Unicode control character sanitization</li>
 *   <li>Optional callback on invalid input for UI refresh</li>
 * </ul>
 *
 * <p>All prompting methods handle the full input loop internally, continuing to
 * prompt until valid input is received or an undo signal is given.
 *
 * <p>Example usage:
 * <pre>
 *   InputResolver resolver = new InputResolver();
 *   int choice = resolver.promptForInt("Enter choice (1-4): ", 1, 4);
 *   String name = resolver.promptForString("Enter name: ", 1, 20);
 * </pre>
 */
public class InputResolver {

    private final Scanner scanner;

    /**
     * Creates a new InputResolver with a default scanner.
     */
    public InputResolver() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Prompts for and parses an integer input.
     * 
     * @param prompt   Prompt message to display
     * @param minValue Minimum allowed value
     * @param maxValue Maximum allowed value
     * @return Valid integer within range
     */
    public int promptForInt(final String prompt, final int minValue, final int maxValue) {
        return promptForInt(prompt, minValue, maxValue, null);
    }

    /**
     * Prompts for and parses an integer with an optional callback invoked each time
     * the user enters an out-of-range or non-numeric value.
     *
     * Entering "Z" or "UNDO" (case-insensitive) returns -1 immediately so callers
     * can treat it as a back/undo signal without looping.
     *
     * @param prompt     Prompt text printed before every input attempt.
     * @param minValue   Inclusive lower bound for accepted values.
     * @param maxValue   Inclusive upper bound for accepted values.
     * @param onInvalid  Optional Runnable called before re-printing the prompt after
     *                   bad input (e.g., to re-render the game screen). May be null.
     * @return Valid integer in [minValue, maxValue], or -1 for undo/back.
     */
    public int promptForInt(final String prompt, final int minValue, final int maxValue, final Runnable onInvalid) {
        boolean firstPrompt = true;
        while (true) {
            try {
                if (firstPrompt) {
                    System.out.print(prompt);
                    firstPrompt = false;
                } else {
                    if (onInvalid != null) {
                        onInvalid.run();
                    }
                    System.out.print(prompt);
                }
                final String raw;
                try {
                    raw = scanner.nextLine();
                } catch (final java.util.NoSuchElementException e) {
                    return -1;
                }
                final String input = sanitizeInput(raw).trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
                }

                if (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO")) {
                    return -1;
                }

                final int value = Integer.parseInt(input);

                if (value < minValue || value > maxValue) {
                    System.out.printf("Value must be between %d and %d. Please try again.%n",
                            minValue, maxValue);
                    continue;
                }

                return value;

            } catch (final NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Prompts for and parses a string input.
     * 
     * @param prompt    Prompt message to display
     * @param minLength Minimum string length
     * @param maxLength Maximum string length
     * @return Valid string within length constraints
     */
    public String promptForString(final String prompt, final int minLength, final int maxLength) {
        boolean firstPrompt = true;
        while (true) {
            try {
                if (firstPrompt) {
                    System.out.print(prompt);
                    firstPrompt = false;
                } else {
                    System.out.print(prompt);
                }
                final String input = sanitizeInput(scanner.nextLine()).trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
                }

                if (input.equalsIgnoreCase("Z") || input.equalsIgnoreCase("UNDO")) {
                    return "Z";
                }

                if (input.length() < minLength) {
                    System.out.printf("Input too short. Minimum length is %d characters.%n", minLength);
                    continue;
                }

                if (input.length() > maxLength) {
                    System.out.printf("Input too long. Maximum length is %d characters.%n", maxLength);
                    continue;
                }

                return input;

            } catch (final Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    /**
     * Closes the scanner and releases resources.
     */
    public void close() {
        scanner.close();
    }

    /**
     * Strips invisible Unicode control characters (Unicode category Cc) from raw
     * terminal input. This prevents issues caused by ANSI escape codes, null bytes,
     * or other non-printable characters that some terminals inject into the input
     * stream, which would otherwise break Integer.parseInt() and similar calls.
     *
     * @param input Raw string from the Scanner; may be null.
     * @return Sanitized string with all control characters removed, or "" if null.
     */
    private String sanitizeInput(final String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\p{C}", "");
    }
}
