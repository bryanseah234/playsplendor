# Splendor (Java Implementation)

![Java Version](https://img.shields.io/badge/Java-17%2B-blue)
![License](https://img.shields.io/badge/License-Educational-green)

A modular, strictly MVC-based implementation of the board game Splendor in Java.

## Quick Start

```bash
# Build the project
./compile.sh    # Unix/macOS
.\compile.bat   # Windows

# Run the game (console mode)
./run.sh        # Unix/macOS
.\run.bat       # Windows

# Run in server mode (network multiplayer)
java -cp classes com.splendor.Main --server
```

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Architecture Overview](#architecture-overview)
- [Gameplay Flow](#gameplay-flow)
- [How to Play](#how-to-play)
- [Getting Started](#getting-started)
- [Network Multiplayer](#network-multiplayer)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [AI Attribution](#ai-attribution)
- [License](#license)

## Features

- **MVC Architecture**: Strict separation of concerns between Model, View, and Controller.
- **Custom Exception Handling**: Robust error management using the `SplendorException` hierarchy.
- **Configurable**: Game rules and setup parameters loaded from `src/resources/config.properties`.
- **Console Interface**:
  - **ASCII Dashboard**: Cards and game state are rendered in a clean, frame-based dashboard.
  - **Color Support**: Gems and player info are color-coded (Red, Green, Blue, White, Black, Gold).
  - **Smart Menus**: Options are dynamically enabled or disabled based on game state.
  - **Interactive Prompts**: Intuitive sub-menus for selecting gems and cards.
  - **Undo Feature**: Allows players to undo their last turn by typing `Z` or `UNDO`.
- **Network Support**: Multiplayer capabilities via TCP sockets.
- **Bot/CPU Players**: Name a player with "bot" in the name to enable computer-controlled opponents.
- **Automated Documentation**: Javadoc generation with pre-commit validation.

## Architecture Overview

The project follows a strict MVC pattern to ensure separation of concerns. The Controller layer orchestrates the game logic by delegating specific tasks to specialized sub-controllers and validators.

![Architecture Diagram](docs/diagrams/architecture-overview.png)

<details>
<summary>📋 View Architecture Diagram Source</summary>

```mermaid
flowchart LR
    subgraph Main["Main Entry Point"]
        M[Main.java]
    end
    
    subgraph Controller["Controller Layer"]
        GC[GameController]
        TC[TurnController]
        GFC[GameFlowController]
        PC[PlayerController]
        MB[MenuBuilder]
    end
    
    subgraph Model["Model Layer"]
        GM[Game, Board, Player]
        MV[MoveValidator]
        GRV[GameRuleValidator]
    end
    
    subgraph View["View Layer"]
        IV[IGameView Interface]
        CV[ConsoleView]
        RV[RemoteView]
        NV[NetworkGameView]
    end
    
    subgraph Config["Configuration"]
        ICP[IConfigProvider Interface]
        FCP[FileConfigProvider]
    end
    
    subgraph Network["Network Layer"]
        SSH[ServerSocketHandler]
    end
    
    subgraph Util["Utilities"]
        UT[InputResolver, GameLogger]
    end
    
    subgraph Exception["Exceptions"]
        EX[SplendorException Hierarchy]
    end

    M -->|creates| GC
    GC -->|orchestrates| TC
    GC -->|orchestrates| GFC
    GC -->|orchestrates| PC
    GC -->|orchestrates| MB
    GC -->|renders via| IV
    GC -->|reads config via| ICP
    GC -->|validates via| MV
    GC -->|validates rules via| GRV
    
    CV -->|implements| IV
    RV -->|implements| IV
    NV -->|implements| IV
    
    FCP -->|implements| ICP
    SSH -->|bridges| IV
    
    Model -->|throws| EX
    
    Controller -->|uses| Model
    Controller -->|uses| View
    Controller -->|uses| Config
    Controller -->|uses| Util
```

</details>

<details>
<summary>🎲 Game State Lifecycle — ONGOING → FINAL_ROUND → FINISHED</summary>

![Game State Diagram](docs/diagrams/game-state-lifecycle.png)

<details>
<summary>📋 View State Diagram Source</summary>

```mermaid
stateDiagram-v2
    [*] --> ONGOING: GameController.startGame()
    ONGOING --> ONGOING: processTurn() [points < 15]
    ONGOING --> FINAL_ROUND: processTurn() [player reaches 15+ pts]
    FINAL_ROUND --> FINAL_ROUND: processTurn() [round not complete]
    FINAL_ROUND --> FINISHED: All players took equal turns
    FINISHED --> [*]: determineWinner() called
```

</details>

</details>

## Gameplay Flow

The following sequence diagram illustrates the standard turn lifecycle, including validation and special post-turn checks for noble visits or token limits.

![Turn Sequence Diagram](docs/diagrams/turn-sequence.png)

<details>
<summary>📋 View Sequence Diagram Source</summary>

```mermaid
sequenceDiagram
    participant P as Player
    participant GC as GameController
    participant MB as MenuBuilder
    participant GV as IGameView
    participant MV as MoveValidator
    participant TC as TurnController
    participant PC as PlayerController
    participant GFC as GameFlowController

    GC->>MB: buildMenuOptions()
    GC->>GV: promptForMove()
    P->>GV: submit move
    GV->>GC: move details
    GC->>MV: validate(move)
    
    alt Invalid Move
        MV-->>GV: rejection
        GV-->>P: show error / re-prompt
    else Valid Move
        GC->>TC: executeMove()
        TC->>GC: updated state
        GC->>PC: checkNobleVisits()
        opt Noble Visit
            PC->>P: assign noble (+3pts)
        end
        GC->>PC: handleTokenDiscard()
        opt Token Limit Exceeded
            PC->>GV: trigger discard flow
        end
        GC->>GFC: updateState()
        opt Final Round Triggered
            GFC->>GC: transition to FINAL_ROUND
        end
        GFC->>GC: next turn
    end
```

</details>

</details>

## How to Play

### Objective

The goal is to be the first player to reach 15 prestige points (configurable). Points are earned by purchasing development cards and attracting noble tiles.

### Setup

The game scales based on the number of players (2-4):

| Players | Gem Tokens per Color | Nobles Available |
|---------|---------------------|------------------|
| 2       | 4                   | 3                |
| 3       | 5                   | 4                |
| 4       | 7                   | 5                |

### Actions (one per turn)

1. **Take 3 Different Gems**: Pick 1 gem each of 3 different colors (no Gold).
2. **Take 2 Same Gems**: Pick 2 gems of the same color (only if ≥4 available).
3. **Reserve a Card**: Take a face-up card or draw from a deck (max 3 reserved).
4. **Buy a Card**: Purchase a face-up card using your gems and discounts.
5. **Buy Reserved Card**: Purchase a card you previously reserved.

### Nobles

At end of turn, if your gem bonuses (from purchased cards) meet a Noble's requirements, that Noble visits you automatically (+3 points).

### Winning

Game ends when a player reaches 15+ points. The current round finishes so all players get equal turns. Tiebreaker: fewest purchased cards.

### Token Limit

Max 10 tokens (including Gold). Must discard down to 10 at end of turn.

### Undo

After your move executes, type `Z` or `UNDO` to revert your turn and try again.

### Bot Players

Include "bot" in a player's name (e.g., "Bot1", "AngryBot") to make them a computer-controlled opponent.

## Getting Started

### Prerequisites

- Java JDK 17 or higher
- Maven (optional, for dependency management)

### Building

**Windows:**
```batch
.\compile.bat
```

**Unix/macOS:**
```bash
./compile.sh
```

### Running the Game

**Console Mode (Single Player or Local Multiplayer):**

Windows:
```batch
.\run.bat
```

Unix/macOS:
```bash
./run.sh
```

**Server Mode (Network Multiplayer):**

```bash
java -cp classes com.splendor.Main --server
```

## Network Multiplayer

Multiplayer is supported via a custom TCP protocol. The server manages the game state and broadcasts updates to all connected clients.

### 1. Start the Server

The server auto-discovers a free port and displays connection addresses.

```bash
java -cp classes com.splendor.Main --server
```

### 2. Connect as Client

**Netcat (WSL/Linux/macOS):**
```bash
nc <server-ip> <port>
```

**PowerShell (Windows):**
```powershell
powershell -Command "(New-Object System.Net.Sockets.TcpClient).Connect('<server-ip>', <port>)"
```

### 3. Network Commands

- `MOVE:TAKE_GEMS:R,G,B` - Take 3 different gems
- `MOVE:BUY_CARD:5` - Buy card with ID 5
- `MOVE:RESERVE_CARD:2` - Reserve card from tier 2
- `UNDO` - Undo last turn
- `QUIT` - Disconnect from server

## Configuration

Game settings in `src/resources/config.properties`:

| Setting | Default | Description |
|---------|---------|-------------|
| `game.points.win` | 15 | Points required to win |
| `game.tokens.max` | 10 | Maximum tokens per player |
| `game.tokens.2p` | 4 | Gem tokens per color (2 players) |
| `game.tokens.3p` | 5 | Gem tokens per color (3 players) |
| `game.tokens.4p` | 7 | Gem tokens per color (4 players) |
| `game.nobles.base` | 3 | Base number of nobles |
| `game.nobles.add` | 1 | Additional nobles per player |
| `game.reserved.max` | 3 | Maximum reserved cards |

## Testing

### Running Tests

**Windows:**
```batch
.\run_tests.bat
```

**Unix/macOS:**
```bash
./run_tests.sh
```

### Test Coverage

The test suite uses JUnit 5 and covers:

- **Model Layer**: Game state, player actions, card mechanics
- **Validators**: Move validation, rule enforcement
- **Controllers**: Turn logic, game flow
- **Edge Cases**: Invalid inputs, boundary conditions

## Project Structure

```
splendor/
├── compile.bat / compile.sh      # Build scripts
├── run.bat / run.sh              # Run scripts
├── generate_docs.bat / generate_docs.sh  # Documentation generator
├── README.md                      # This file
├── PRD.md                         # Product Requirements Document
├── RULES.md                       # Game Rules
├── DOCUMENTATION_STANDARDS.md    # Javadoc Standards
├── src/
│   └── com/splendor/
│       ├── Main.java             # Entry point
│       ├── config/               # Configuration management
│       ├── model/                # Game logic and state
│       │   ├── Game.java
│       │   ├── Player.java
│       │   ├── Board.java
│       │   ├── Card.java
│       │   ├── Gem.java
│       │   └── validator/        # Move and rule validation
│       ├── view/                 # User interface
│       │   ├── IGameView.java
│       │   ├── ConsoleView.java
│       │   └── RemoteView.java
│       ├── controller/           # Game orchestration
│       │   ├── GameController.java
│       │   ├── TurnController.java
│       │   └── PlayerController.java
│       ├── network/              # Network multiplayer
│       │   ├── ServerSocketHandler.java
│       │   └── ClientHandler.java
│       ├── data/                 # Data loading (CSV)
│       │   └── CardLoader.java
│       ├── util/                 # Utilities
│       │   ├── InputResolver.java
│       │   ├── GameLogger.java
│       │   └── GemParser.java
│       └── exception/            # Custom exceptions
│           └── SplendorException.java
├── classes/                      # Compiled classes (generated)
├── docs/
│   ├── javadoc/                  # Generated API documentation
│   └── diagrams/                 # Mermaid diagram exports
├── resources/
│   ├── config.properties         # Game configuration
│   └── card_data.csv             # Card data
└── test/                         # Unit tests
```

## Contributing

### Getting Started

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Conventions

- **Architecture**: Strict MVC. Model has no I/O, View is interface-based, Controller orchestrates.
- **Documentation**: All public APIs must have Javadoc comments.
- **Testing**: Write unit tests for new functionality.
- **Style**: Follow Java naming conventions and Google Java Style Guide.

### CI/CD

The repository uses GitHub Actions for:
- Automated testing on push
- Javadoc generation and validation
- Code quality checks

### Reporting Issues

Open a GitHub Issue with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Java version and OS

## AI Attribution

This project was developed with the assistance of AI tools for code generation, documentation, and testing.

## License

This project is for educational purposes.

---

**Last Updated:** April 1, 2026  
**Documentation Status:** ✅ Complete with automated generation
