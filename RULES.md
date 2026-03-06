# Splendor Game Rules

## Objective
The goal of Splendor is to be the first player to reach **15 prestige points**. Points are earned by purchasing development cards and attracting nobles.

## Setup
The game scales based on the number of players (2-4):
- **2 Players:** 4 gems of each color, 3 nobles.
- **3 Players:** 5 gems of each color, 4 nobles.
- **4 Players:** 7 gems of each color, 5 nobles.
- **Gold Tokens:** Always 5.

## Gameplay
Players take turns in a clockwise order. On your turn, you must perform **exactly one** of the following actions. The game menu will automatically **gray out** actions that are currently impossible (e.g., taking 2 same gems when there aren't enough left).

### 1. Take 3 Different Gem Tokens
You can take 1 gem of 3 **different** colors.
- You cannot take Gold tokens with this action.
- Select "1" from the menu, then type the names of the 3 colors (e.g., `RED GREEN BLUE`).

### 2. Take 2 Same Gem Tokens
You can take 2 gems of the **same** color.
- This action is only allowed if there are **4 or more** tokens of that color available in the bank.
- Select "2" from the menu, then type the color (e.g., `RED`).

### 3. Reserve a Card
You can reserve a development card from the board (or draw one from the top of a deck) and put it into your hand.
- You receive **1 Gold token** (if available).
- You can have a maximum of **3 reserved cards** in your hand.
- Reserved cards can be purchased on a later turn.
- Select "3" from the menu, then enter the **Card Number** displayed on the card (e.g., `5`).

### 4. Buy a Development Card
You can purchase a card from the board.
- **Cost:** Pay the required gem tokens shown on the card.
- **Discounts:** Your purchased cards provide permanent gem bonuses. If you have a Blue card, it reduces the cost of future purchases requiring Blue gems by 1.
- **Gold:** Gold tokens are "jokers" and can replace any color gem.
- **Return Tokens:** Spent tokens (including Gold) are returned to the bank.
- Select "4" from the menu, then enter the **Card Number** displayed on the card.

### 5. Buy a Reserved Card
You can purchase a card that you previously reserved.
- Select "5" from the menu.
- The game will show your reserved cards. Enter the **Index** (1, 2, or 3) of the card you want to buy.

## Nobles
At the end of your turn, if you have enough gem bonuses (from purchased cards) to satisfy a Noble's requirements, that Noble automatically visits you.
- You earn the Noble's prestige points (usually 3).
- You can only be visited by **one** Noble per turn.

## Winning the Game
The game ends when a player reaches **15 or more prestige points**.
- The current round is completed so that all players have had an equal number of turns.
- The player with the highest score wins.
- **Tie-breaker:** The player with the fewest purchased development cards wins.

## Token Limit
At the end of your turn, if you have more than **10 tokens** (including Gold), you must discard down to 10.

## Undo Move
At the end of your action, when the game confirms your move is executed, you may type `Z` or `UNDO` (and press Enter) to revert your turn and try a different action. This undoes any gem collection, card purchases, reservations, and noble visits that occurred during that turn.
