# Splendor Game Rules

## Quick Reference

**Objective:** Be the first player to reach **15 prestige points**.  
**Players:** 2-4  
**Time:** 30-45 minutes  

## Setup

The game scales based on the number of players:

| Players | Gem Tokens per Color | Nobles Available |
|---------|---------------------|------------------|
| 2       | 4                   | 3                |
| 3       | 5                   | 4                |
| 4       | 7                   | 5                |

### Initial Board Setup

1. **Gem Bank**: Place gem tokens in 6 piles (5 colors + gold)
2. **Cards**: Deal 4 face-up cards for each tier (I, II, III)
3. **Nobles**: Place noble tiles face-up (number = players + 1)

## Gameplay

Players take turns in clockwise order. On your turn, you must perform **exactly one** of the following actions:

### 1. Take 3 Different Gem Tokens

- Take 1 gem of 3 **different** colors
- **Cannot** take gold tokens with this action
- All 3 chosen gem piles must have **at least 4 tokens** remaining

**Example:** Take 1 red, 1 green, 1 blue

### 2. Take 2 Same Gem Tokens

- Take 2 gems of the **same** color
- Only allowed if **at least 4 tokens** of that color remain
- **Cannot** take gold tokens with this action

**Example:** Take 2 red gems (only if ≥4 red tokens available)

### 3. Reserve a Card

- Take a face-up card from the board **or** draw the top card from a deck
- Place it face-down in your reserve area
- **Maximum 3 reserved cards** at any time
- Reserved cards can be bought later (see action 5)

**Note:** If all decks are empty and no face-up cards available, you cannot reserve.

### 4. Buy a Development Card

- Purchase a face-up card from the board
- Pay the cost shown on the card (in gems)
- **Discounts apply**: Each purchased card provides a permanent discount for its gem color
- **Gold tokens** can be used as wild cards to cover any missing gems

**Cost Calculation:**
```
Final Cost = Card Cost - (Your Discounts) - (Gold Tokens Used)
```

### 5. Buy a Reserved Card

- Purchase a card you previously reserved
- Pay the cost (with discounts and gold)
- No other player can buy your reserved cards

## Nobles

At the **end of your turn**, if your gem bonuses (from purchased cards) meet a Noble's requirements, that Noble **automatically visits** you.

- Each Noble is worth **3 prestige points**
- Only **one Noble** can visit per turn
- If multiple Nobles qualify, you choose which one visits

**Noble Requirements Example:**
```
Noble: Requires 3 Red gem bonuses, 3 Green gem bonuses
```

## Winning the Game

The game ends when a player reaches **15 or more prestige points**.

1. The current round finishes so all players get **equal turns**
2. Final scores are calculated:
   - Points from purchased cards
   - Points from noble tiles (3 each)
3. **Tiebreaker**: Player with **fewest purchased cards** wins
4. If still tied, players share the victory

## Token Limit

At the **end of your turn**, if you have **more than 10 tokens** (including gold), you must **discard down to 10**.

- Choose which tokens to return to the bank
- This happens **after** all other actions and noble visits

## Undo Move

At the **end of your action**, when the game confirms your move is executed, you may type `Z` or `UNDO` to revert your turn and try again.

**Limitations:**
- Only works for your **most recent turn**
- Cannot undo after the next player has started their turn

## Special Rules

### Gold Tokens (Wild Cards)

- Gold tokens are obtained **only** when reserving a card
- Can be used as **any gem color** when purchasing cards
- Count toward the 10-token limit

### Permanent Discounts

- Each purchased card provides a **permanent discount** for its gem color
- Discount applies to **all future purchases**
- Stacks with multiple cards of the same color

### Card Tiers

- **Tier I** (Level 1): 0-3 points, low cost
- **Tier II** (Level 2): 3-4 points, medium cost
- **Tier III** (Level 3): 4-5 points, high cost

### Reserved Card Limits

- Maximum **3 reserved cards** at any time
- If you have 3 reserved cards, you **cannot reserve** more
- You can still **buy** reserved cards to free up space

## Game Variants

### Quick Game (Optional)

- Reduce winning points to **10** (configurable)
- Use fewer nobles (players - 1)

### Advanced Rules (Optional)

- **Negative scoring**: Some cards have negative points
- **Special abilities**: Certain cards provide unique effects

---

**Note:** These rules implement the standard Splendor board game. Custom rules can be configured in `src/resources/config.properties`.

**Last Updated:** April 1, 2026
