# Destructive Test Cases for Splendor

This document outlines comprehensive test cases to verify the robustness and correctness of the Splendor implementation. Run these tests interactively or through the automated test suite.

## Quick Start

```bash
# Run automated tests
test/run_tests.sh        # Unix/macOS
test\run_tests.bat       # Windows

# Run specific test category
test/run_tests.sh --category com.splendor.test.model
```

## Automated Test Coverage

The test suite uses JUnit 5 and covers:

### Model Layer Tests
- **GameTest**: Game state transitions, undo/redo, win conditions
- **PlayerTest**: Token management, card purchases, noble visits
- **BoardTest**: Gem bank operations, card dealing, noble setup
- **CardTest**: Cost calculation, discount application

### Validator Tests
- **MoveValidatorTest**: All move types, edge cases, rule enforcement
- **GameRuleValidatorTest**: Player count validation, configuration checks

### Controller Tests
- **GameControllerTest**: Turn flow, game initialization, error handling
- **TurnControllerTest**: Move execution, state updates

### Integration Tests
- **FullGameTest**: Complete 2-player game from start to finish
- **NetworkTest**: Server-client communication, message parsing

### Test Coverage Targets
- **Line Coverage**: 85% minimum
- **Branch Coverage**: 80% minimum  
- **Method Coverage**: 90% minimum
- **Class Coverage**: 95% minimum

### Test Execution Commands

```bash
# Run all tests with coverage report
test\run_tests.bat --coverage    # Windows
test/run_tests.sh --coverage     # Unix/macOS

# Run specific test categories
test/run_tests.sh --category com.splendor.model
test/run_tests.sh --category com.splendor.controller
test/run_tests.sh --category com.splendor.validator
test/run_tests.sh --category com.splendor.network

# Run tests with verbose output
test/run_tests.sh --verbose

# Generate HTML coverage report
# Coverage is integrated within run_tests.sh --coverage if JaCoCo is set up
```

### Coverage Report Generation

The test suite automatically generates coverage reports in multiple formats:
- **HTML**: `coverage/html/index.html` - Interactive coverage dashboard
- **XML**: `coverage/coverage.xml` - CI/CD integration
- **CSV**: `coverage/coverage.csv` - Data analysis
- **Console**: Real-time coverage feedback during test execution

## Interactive Test Cases

### 1. The "Fat Finger" Crash Test

**Goal**: Verify input handling for non-integer values in numeric menus.

**Steps**:
1. Start game with 2 players
2. When prompted for number of players, enter: `abc`, `!@#`, `3.14`, ` `
3. When prompted for gem selection, enter: `xyz`, `123`, `R G B` (valid)

**Expected**: 
- All invalid inputs show "Invalid input" message
- Game continues without crashing
- Valid inputs are accepted

### 2. The "Greedy Token" Limit Test

**Goal**: Verify that moves are blocked when the token limit is reached.

**Steps**:
1. Start game with 2 players
2. Player 1 takes 3 gems (e.g., R, G, B)
3. Player 1 takes 3 more gems (now has 6)
4. Player 1 takes 3 more gems (now has 9)
5. Player 1 takes 3 more gems (now has 12 - exceeds limit)

**Expected**:
- After step 5, game prompts to discard down to 10 tokens
- Player must choose which 2 tokens to return
- Game continues after discard

### 3. The "Illegal Take 2" Test

**Goal**: Verify the rule: taking 2 identical gems requires a pile of at least 4.

**Steps**:
1. Set up game with only 3 red gems available (modify config or deplete bank)
2. Try to take 2 red gems

**Expected**:
- Move is rejected with message: "Cannot take 2 gems - only 3 available"
- Player must choose different action

### 4. The "Broke Buyer" Test

**Goal**: Verify that buying validation works correctly (including discounts and gold/wild if applicable).

**Steps**:
1. Player has 0 gems and no discounts
2. Try to buy a card that costs 3 red gems
3. Player acquires 2 red gems through taking
4. Try to buy the same card again

**Expected**:
- First attempt: Rejected with "Insufficient gems" message
- Second attempt: Rejected with "Insufficient gems" message (still short 1 red)

### 5. The "Oversized Input" Test

**Goal**: Verify string input constraints (player names).

**Steps**:
1. Start game
2. When prompted for player name, enter:
   - Empty string
   - 50 character string
   - String with special characters: `!@#$%^&*()`
   - Valid name: `Alice`

**Expected**:
- Empty string: Rejected with "Name cannot be empty"
- 50 character string: Rejected with "Name too long (max 20 characters)"
- Special characters: Rejected with "Name can only contain letters and spaces"
- Valid name: Accepted

### 6. The "Noble Visit" Test

**Goal**: Verify nobles visit automatically when conditions are met.

**Steps**:
1. Player purchases cards that provide gem bonuses
2. Accumulate bonuses to meet a noble's requirements
3. End turn

**Expected**:
- Noble automatically visits at end of turn
- Player receives +3 points
- Noble is removed from available nobles

### 7. The "Undo Mania" Test

**Goal**: Verify undo functionality works correctly and has limits.

**Steps**:
1. Player makes a move
2. Types `Z` or `UNDO`
3. Next player makes a move
4. Previous player tries to undo again

**Expected**:
- Step 2: Move is undone, state reverts to before the move
- Step 4: Cannot undo - message "Cannot undo - other players have taken turns"

### 8. The "Final Round" Test

**Goal**: Verify final round logic and win condition.

**Steps**:
1. Set up game state where Player 1 has 14 points
2. Player 1 makes a move that gives them 1+ points (now 15+)
3. Continue turns until all players have played in the final round
4. Game ends

**Expected**:
- After step 2: Game enters FINAL_ROUND state
- After step 4: Game ends, winner is determined
- Tiebreaker applied if needed (fewest purchased cards)

### 9. The "Bot Behavior" Test

**Goal**: Verify computer players make valid moves.

**Steps**:
1. Start game with 1 human, 1 bot (name contains "bot")
2. Let bot take several turns
3. Verify bot's moves are legal

**Expected**:
- Bot always makes legal moves
- Bot doesn't crash or hang
- Bot respects all game rules

### 10. The "Network Disconnect" Test

**Goal**: Verify network handling when client disconnects.

**Steps**:
1. Start server
2. Connect client
3. Begin game
4. Disconnect client abruptly (Ctrl+C, close terminal)

**Expected**:
- Server detects disconnect
- Game handles gracefully (removes player or ends game)
- No crash or hang

## Edge Cases to Test

### Configuration Edge Cases
- Minimum players (2)
- Maximum players (4)
- Winning points = 1 (instant win)
- Winning points = 100 (very long game)
- Token limit = 0 (cannot hold any tokens)

### Game State Edge Cases
- All gems depleted
- All cards of a tier depleted
- All nobles claimed
- Player reserves 3 cards, then tries to reserve more

### Input Edge Cases
- Unicode characters in player names
- Very long input strings (1000+ characters)
- Null bytes in input
- ANSI escape sequences in input

## Running Tests

### Automated Test Suite

```bash
# Compile and run all tests
test/run_tests.sh

# Run specific test class
test/run_tests.sh --class com.splendor.model.validator.MoveValidatorTest
```

### Manual Testing

1. Start the game: `./run.sh`
2. Follow the test case steps above
3. Verify expected behavior
4. Report any discrepancies

## Test Reporting

If you find a bug or unexpected behavior:

1. **Document the issue**:
   - Test case name/number
   - Steps to reproduce
   - Expected result
   - Actual result
   - Screenshots (if applicable)

2. **Create a GitHub Issue** with:
   - Title: `[TEST] <Test Case Name> - <Brief Description>`
   - Labels: `bug`, `testing`
   - Detailed description

## Continuous Integration

Tests are automatically run on:
- Every push to main branch
- Every pull request
- Nightly build (full test suite)

Test results are available in GitHub Actions logs.

---

**Last Updated:** April 1, 2026  
**Test Status:** ✅ Comprehensive coverage with automated and manual tests
