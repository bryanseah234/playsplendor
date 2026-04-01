# Splendor Class Diagram (UML Class Diagram)

This document contains the complete UML class diagrams for the Splendor board game implementation, showing the refactored package structure and relationships between classes.

## System Architecture Overview

![Diagram 1](./splendor-class-diagram_diagram_1.png)

## Detailed Class Diagrams

### 1. Core Model Package (com.splendor.model)

![Diagram 2](./splendor-class-diagram_diagram_3.png)

### 2. Controller Package (com.splendor.controller)

![Diagram 3](./splendor-class-diagram_diagram_3.png)

### 3. View Package (com.splendor.view)

![Diagram 4](./splendor-class-diagram_diagram_4.png)

### 4. Data Loading Package (com.splendor.data)

![Diagram 5](./splendor-class-diagram_diagram_5.png)

### 5. Validator Package (com.splendor.model.validator)

![Diagram 6](./splendor-class-diagram_diagram_6.png)

### 6. Config Package (com.splendor.config)

![Diagram 7](./splendor-class-diagram_diagram_7.png)

### 7. Network Package (com.splendor.network)

![Diagram 8](./splendor-class-diagram_diagram_8.png)

### 8. Utility Package (com.splendor.util)

![Diagram 9](./splendor-class-diagram_diagram_9.png)

### 9. Exception Hierarchy (com.splendor.exception)

![Diagram 10](./splendor-class-diagram_diagram_10.png)

## Package Dependencies

![Diagram 11](./splendor-class-diagram_diagram_11.png)

## Design Patterns

This implementation uses the following design patterns:

1. **MVC Pattern**: Clear Model-View-Controller separation
2. **Strategy Pattern**: `BotStrategy` for AI player decision-making
3. **Factory Pattern**: `MenuBuilder` builds menu options
4. **Singleton Pattern**: `GameState` uses predefined instances
5. **Dependency Injection**: Through `IConfigProvider` and `IGameView` interfaces
6. **Template Method**: `CardDataProvider` defines data loading contract
7. **Facade Pattern**: `CardLoader` simplifies data loading API
8. **Validator Pattern**: `MoveValidator` and `GameRuleValidator` separate validation logic

## Data Flow

### Game Initialization Flow

```
Main.main()
  → FileConfigProvider.loadConfiguration()
  → ConfigValidator.validateAll()
  → GameController.initializeGame()
    → Prompt for player count
    → Create Player/ComputerPlayer instances
    → CardLoader.loadCards() / loadNobles()
    → Initialize Board
  → GameController.startGame()
```

### Turn Execution Flow

```
GameController.startGame()
  → Loop until game ends:
    → TurnController.executeTurn()
      → ConsoleView.promptForMove()
      → MoveValidator.validateMove()
      → Execute action
      → Check noble cards
      → Check win condition
```

---

*Documentation generated: 2026-03-31*
*Based on refactored Splendor codebase*
