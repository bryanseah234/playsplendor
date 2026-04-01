# Destructive Test Cases for Splendor

Run these tests in the **interactive game** to ensure the application handles invalid input and edge cases gracefully **without crashing**.

Notes:
- These are **manual tests** (you type inputs when the program prompts you).
- When this document says “Option X”, it refers to the **number shown in the move menu** on your screen. If your menu numbering differs, choose the option with the **same label text** (e.g., “Take 3 different gems”).

## 1. The "Fat Finger" Crash Test
- **Goal**: Verify input handling for non-integer values in numeric menus.
- **Action**: When prompted to enter a move number (e.g., 1–6), type `garbage_text`, `!!!`, or just press Enter on an empty input.
- **Expected Outcome**: The game should **not** crash or print a stack trace. It should display an error like “Invalid number format. Please enter a valid integer.” and then re-prompt.

## 2. The "Greedy Token" Limit Test
- **Goal**: Verify that moves are blocked when the token limit is reached.
- **Action**:
  1. Accumulate **10 total tokens** (any combination; e.g., repeatedly “Take 3 different gems”).
  2. Attempt to take more tokens by selecting the menu option labeled “Take 3 different gems”.
- **Expected Outcome**:
  - The option should be visually disabled/grayed out (if your terminal supports color), **and/or**
  - The game should reject the move with “Move unavailable” (or similar), and **no tokens should be added**.

## 3. The "Illegal Take 2" Test
- **Goal**: Verify the rule: taking 2 identical gems requires a pile of **at least 4**.
- **Action**:
  1. Find a gem pile with only **2 or 3** gems remaining.
  2. Select the menu option labeled “Take 2 same gems”.
  3. Enter the color of that small pile.
- **Expected Outcome**: The game should reject the move with a message like “Need at least 4 [Color] gems available”, and the gem counts should remain unchanged.

## 4. The "Broke Buyer" Test
- **Goal**: Verify that buying validation works correctly (including discounts and gold/wild if applicable).
- **Action**:
  1. Identify a card you clearly cannot afford (e.g., an expensive card while you have 0 tokens).
  2. Select the menu option labeled “Buy a card”.
  3. Enter that card’s number/id as shown on screen.
- **Expected Outcome**: The game should compute the total cost and reject the purchase if funds are insufficient. It should **not** deduct tokens (or change ownership) when the purchase fails.

## 5. The "Oversized Input" Test
- **Goal**: Verify string input constraints (player names).
- **Action**:
  1. Start a new game.
  2. When asked for a player name, paste a very long string (e.g., 50+ characters).
- **Expected Outcome**: `InputResolver` should reject it and re-prompt for a shorter name (e.g., “Input too long. Maximum length is 20 characters.”).
