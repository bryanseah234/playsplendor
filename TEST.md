# Destructive Test Cases for Splendor

Run these tests to ensure the application handles invalid input and edge cases gracefully without crashing.

## 1. The "Fat Finger" Crash Test
*   **Goal**: Verify input handling for non-integer values in numeric menus.
*   **Action**: When prompted for a move (1-6), type `garbage_text`, `!!!`, or leave it empty and press Enter.
*   **Expected Outcome**: The game should NOT crash with a stack trace. It should display "Invalid number format. Please enter a valid integer." and re-prompt.

## 2. The "Greedy Token" Limit Test
*   **Goal**: Verify that the UI restricts actions when the token limit is reached.
*   **Action**: 
    1. Accumulate 10 tokens (e.g., by taking 3 gems multiple times).
    2. Try to select Option 1 ("Take 3 different gems") by typing `1`.
*   **Expected Outcome**: 
    *   The option should be visually grayed out in the menu (if color is supported).
    *   The game should respond with "Move unavailable" or similar validation error, preventing the move.

## 3. The "Illegal Take 2" Test
*   **Goal**: Verify the rule that taking 2 identical gems requires a pile of at least 4.
*   **Action**: 
    1. Find a gem pile with only 2 or 3 gems remaining.
    2. Select Option 2 ("Take 2 same gems").
    3. Enter the color of that small pile.
*   **Expected Outcome**: The game should reject the move with a message like "Need at least 4 [Color] gems available".

## 4. The "Broke Buyer" Test
*   **Goal**: Verify that purchasing validation works correctly.
*   **Action**: 
    1. Look at a card you clearly cannot afford (e.g., a Tier 3 card when you have 0 tokens).
    2. Select Option 4 ("Buy a card").
    3. Enter the card number.
*   **Expected Outcome**: The game should calculate costs (including any discounts/gold) and reject the move if funds are insufficient. It should NOT deduct tokens if the purchase fails.

## 5. The "Oversized Input" Test
*   **Goal**: Verify string input constraints.
*   **Action**: 
    1. Start a new game.
    2. When asked for a player name, paste a very long string (e.g., 50+ characters).
*   **Expected Outcome**: `InputResolver` should catch this and ask for a shorter name (e.g., "Input too long. Maximum length is 20 characters.").
