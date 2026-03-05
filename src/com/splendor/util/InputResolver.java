/**
 * Safe input parsing utility for handling user input.
 * Provides robust input validation and parsing to prevent crashes.
 * 
 * @author Splendor Development Team
 * @version 1.0
 */
package com.splendor.util;

import java.util.Scanner;

/**
 * Utility class for safe input parsing and validation.
 * Prevents crashes from invalid user input and provides helpful error messages.
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
     * @param prompt Prompt message to display
     * @param minValue Minimum allowed value
     * @param maxValue Maximum allowed value
     * @return Valid integer within range
     */
    public int promptForInt(final String prompt, final int minValue, final int maxValue) {
        while (true) {
            try {
                System.out.print(prompt);
                final String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
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
     * @param prompt Prompt message to display
     * @param minLength Minimum string length
     * @param maxLength Maximum string length
     * @return Valid string within length constraints
     */
    public String promptForString(final String prompt, final int minLength, final int maxLength) {
        while (true) {
            try {
                System.out.print(prompt);
                final String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty. Please try again.");
                    continue;
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
     * Prompts for a yes/no confirmation.
     * 
     * @param prompt Prompt message to display
     * @return true for yes, false for no
     */
    public boolean promptForConfirmation(final String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (y/n): ");
                final String input = scanner.nextLine().trim().toLowerCase();
                
                if (input.isEmpty()) {
                    System.out.println("Please enter 'y' for yes or 'n' for no.");
                    continue;
                }
                
                if (input.equals("y") || input.equals("yes")) {
                    return true;
                }
                
                if (input.equals("n") || input.equals("no")) {
                    return false;
                }
                
                System.out.println("Please enter 'y' for yes or 'n' for no.");
                
            } catch (final Exception e) {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }
    
    /**
     * Parses an integer from a string with error handling.
     * 
     * @param input String to parse
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    public int parseInt(final String input, final int defaultValue) {
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(input.trim());
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Validates that a string contains only letters and spaces.
     * 
     * @param input String to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidName(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        return input.matches("^[a-zA-Z\\s]+$");
    }
    
    /**
     * Closes the scanner and releases resources.
     */
    public void close() {
        scanner.close();
    }
}