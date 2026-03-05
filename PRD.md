Vibe Coding PRD: Splendor (Java)
1. Purpose
Build a strictly modular, multiplayer Java implementation of Splendor that prioritizes architectural purity (MVC), external configuration, and stability. The core deliverable is a Console Application, with an optional Network layer to support remote clients (Bonus).
2. Non-Goals
No "GUI First" Logic: The core game loop must run entirely in the console to secure the main 5 marks.
No Logic in Client: The Frontend/Client must be "dumb"; it only displays what the Server (Java) sends.
No Database: State is in-memory.
No Complex AI: Use simple random/greedy bots if AI is needed.
3. Technical Constraints (Strict Grading Criteria)
Language: Java 17+ (Standard Edition).
Architecture: Strict MVC (Model-View-Controller).
Model: Pure Data. NO I/O.
View: Interface-based. ConsoleView is mandatory. NetworkView is for bonus.
Controller: Orchestrates turns.
Configuration (Crucial):
Player Scaling: Token counts must be calculated based on player count (Configurable).
Paths: All file paths defined in config.properties.
Exception Handling: Custom Exception Hierarchy (SplendorException).
Code Style:
SRP: Separate Data Loading (CSV Parsing) from Game Logic.
Safety: try-catch blocks around all user inputs. No crashes.
Transparency: AI usage must be documented in comments.

4. Project Structure
Root Directory:
├── compile.bat               # Windows Batch script to compile the project
├── compile.sh                # Shell script to compile the project
├── run.bat                   # Windows Batch script to run the project
├── run.sh                    # Shell script to run the project
├── PRD.md                    # Project Requirements Document
├── RULES.md                  # Game Rules
├── README.md                 # Project Documentation
├── src/                      # Java source files
├── classes/                  # Compiled class files (empty on submission)
├── media/                    # Media files (empty for console app)
├── lib/                      # External libraries (empty)
└── src/com/splendor/
    ├── Main.java                 # Entry point. Checks args for "--server" mode.
    ├── config/
    │   ├── IConfigProvider.java  # Interface for configuration.
    │   ├── FileConfigProvider.java # Implementation loading from properties.
    │   └── ConfigKeys.java       # Constants (e.g., "game.tokens.4p").
    ├── model/
    │   ├── Game.java             # Holds List<Player>, Board, TurnState.
    │   ├── Board.java            # Holds GemBank, CardDecks (L1,L2,L3), Nobles.
    │   ├── Player.java           # Inventory, Hand (Reserved), Tableau (Purchased).
    │   ├── Card.java             # Data Class (Points, Cost, Tier).
    │   ├── Gem.java              # Enum (RED, GREEN, BLUE, WHITE, BLACK, GOLD).
    │   └── validator/
    │       ├── MoveValidator.java # Logic: Can take 2 same color? Can buy?
    │       └── GameRuleValidator.java # General rule enforcement.
    ├── view/
    │   ├── IGameView.java        # Interface: displayBoard(), showMsg().
    │   ├── ConsoleView.java      # Implementation: ANSI ASCII Dashboard & Input Handling.
    │   └── RemoteView.java       # (Bonus) Implementation: Sends JSON/Text to Socket.
    ├── controller/
    │   ├── GameController.java   # Main Loop (Switch Player -> Get Move -> Validate).
    │   ├── TurnController.java   # Manages individual turn logic.
    │   └── PlayerController.java # Manages player-specific actions.
    ├── exception/
    │   ├── SplendorException.java # Base custom exception.
    │   └── [Various subclasses]   # InvalidMoveException, etc.
    ├── util/
    │   ├── InputResolver.java    # Wraps Scanner. Handles "int" parsing safety.
    │   └── GameLogger.java       # Logging utility.
    └── network/ (Bonus)
        ├── ServerSocketHandler.java # Listens for connections.
        └── ClientHandler.java       # Handles individual client connections.

5. Functional Requirements (Video-Verified Rules)
...
Console Output (Usability Focus)
Dashboard UI:
- Clear "ASCII Art" cards with IDs for easy selection.
- Colorized gems (Red, Green, Blue, White, Black, Gold) and player names.
- Smart Menu: Options are dynamically enabled/disabled (grayed out) based on game state.
- Interactive Prompts: Sub-menus guide the user through complex moves (e.g., selecting specific gems).
- Frame-based rendering: The screen clears between turns to reduce clutter.

Board: Use formatted columns.
[Level 3 Cards]
┌────────────┐  ┌────────────┐
│ #1   (R)   │  │ #2   (G)   │
│ Pts: 4     │  │ Pts: 0     │
│ Cost:      │  │ Cost:      │
│ R3 G2 B1   │  │ W2 B2      │
└────────────┘  └────────────┘
...
[Gems Available]
RED: 4 | GREEN: 2 | BLUE: 0 | ...

7. Bonus Implementation: Frontend (Client-Server)
Architecture:
Server (Java): Run Main.java --server. It starts GameController but swaps ConsoleView for RemoteView.
Client (Web/Python/Java): Connects via TCP/WebSockets. Sends commands (BUY 1, TAKE R G B). Receives Board String/JSON.
Protocol: Simple Text Protocol.
Request: ACTION:BUY:CARD_ID
Response: STATUS:OK or ERROR:Insufficient Funds
Constraint: The logic MUST remain in the Java Server. The frontend is just a display.

8. Quality Bar
Crash Proof: If I type "take two reds" instead of "2", the app must say "Invalid input" not NumberFormatException.
Clean Code: No method > 30 lines. No "God Classes".
Config: If I change game.points.win to 5 in the file, the game must end at 5 points without recompiling.
9. Final Instruction
Implement the core Console functionality first using the strict MVC structure. Once the Console version plays a full 2-player game correctly, implement the ServerSocketHandler to expose the exact same game logic to a remote client for the bonus points.

































Useful Functions
1. Core Gameplay Helpers (Highly Recommended)
These make your main game loop much cleaner.
🔹 canAffordCard(Player p, Card c)
Checks if a player can buy a card considering:
Gems they hold


Permanent bonuses from purchased cards


Gold (joker) tokens


This avoids duplicating logic everywhere.

🔹 calculateMissingGems(Player p, Card c)
Returns which gems (and how many) the player is short of.
Useful for:
Validating gold token usage


AI decision-making


UI feedback (“You need 2 red gems”)



🔹 purchaseCard(Player p, Card c)
Handles everything:
Deducts tokens


Uses gold tokens if needed


Adds the card to player’s tableau


Returns tokens to the bank


Updates bonuses and points


This should be the only place where buying logic happens.

2. Turn & Rule Enforcement (Very Important)
These prevent illegal moves.
🔹 isValidMove(Move move, Player p)
Checks:
Token limit (max 10)


Taking 3 different gems


Taking 2 same gems only if ≥4 available


Reserving only if <3 reserved cards



🔹 endTurn()
Handles:
Move to next player


Check win condition (≥15 points)


Trigger final round if needed



🔹 checkTokenLimit(Player p)
Automatically forces token discard if >10.
You can later add:
Player choice for discarding


AI discard logic



3. Noble Handling (Often Forgotten)
🔹 checkNobleVisit(Player p)
Checks if a player qualifies for any noble after buying a card.
Important rule detail:
Noble visits are automatic


Only one noble per turn



🔹 assignNoble(Player p, Noble n)
Adds noble to player and updates score.

4. Game State & Flow Control
These help a LOT when debugging or extending.
🔹 getGameState()
Returns:
Current player


Token counts


Available cards


Player scores


Very useful for:
Debug output


Save/load


UI rendering



🔹 isGameOver()
Encapsulates the logic:
Someone reached ≥15 points


All players have finished the round



5. AI / Bot Support (Even If You Don’t Add AI Yet)
Designing for this early is smart.
🔹 getAvailableMoves(Player p)
Returns a list of legal moves:
Take tokens


Buy card


Reserve card


This lets you:
Plug in AI later


Reuse logic for hints or tutorials



🔹 evaluateMove(Move m)
Scores how “good” a move is (simple heuristic).
Optional but powerful.

6. Quality-of-Life / Debug Functions (Underrated)
🔹 printBoardState()
Prints:
Token bank


Available cards


Nobles


Helps massively while testing.

🔹 printPlayerState(Player p)
Shows:
Tokens


Bonuses


Reserved cards


Score



7. Save / Load (Advanced but Impressive)
If this is for a project or portfolio:
🔹 serializeGame()
🔹 loadGame(String saveData)
Shows:
Object modeling skills


State management


Clean architecture


