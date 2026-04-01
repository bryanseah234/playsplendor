# Documentation Standards

This document outlines the mandatory rules for Javadoc documentation within the Splendor Java codebase. All developers and AI agents must adhere to these standards to ensure 100% high-quality documentation coverage.

## 1. General Javadoc Requirements

- **100% Coverage:** All `public` and `protected` classes, interfaces, methods, and fields MUST have Javadoc comments.
- **Location:** Place Javadoc comments immediately above the declaration they describe.
- **Format:** Start with `/**` and end with `*/`. Each intermediate line must start with a `*`.

## 2. Class and Interface Documentation

- **Purpose:** Describe the architectural intent, purpose, and responsibilities of the class/interface.
- **Required Tags:**
  - `@author` - The creator or main contributor.
  - `@version` - The version of the class (e.g., `1.0`).
  - `@since` - The version in which the class was added.

```java
/**
 * Represents a playable card in the Splendor game.
 * Contains information about the card's cost, prestige points, and gem bonus.
 *
 * @author Splendor Dev Team
 * @version 1.0
 * @since 1.0
 */
public class Card { ... }
```

## 3. Method Documentation

- **Description:** Clearly describe what the method does, not how it does it.
- **Parameters:** Use `@param` for every parameter. Include the parameter name and a description of its purpose.
- **Return Value:** Use `@return` if the method does not return `void`. Describe the return value.
- **Exceptions:** Use `@throws` for every exception (checked and unchecked) the method might throw under specific conditions.

```java
/**
 * Purchases the specified card for the player.
 * Deducts the required gems from the player's inventory and adds the card to their collection.
 *
 * @param player The player attempting to purchase the card.
 * @param card The card to be purchased.
 * @return true if the purchase was successful, false otherwise.
 * @throws IllegalArgumentException if the player or card is null.
 * @throws InsufficientFundsException if the player cannot afford the card.
 */
public boolean purchaseCard(Player player, Card card) throws InsufficientFundsException { ... }
```

## 4. Field Documentation

- Document the purpose and constraints of public or protected fields.

```java
/** The maximum number of gems a player can hold at any time. */
public static final int MAX_GEMS = 10;
```

## 5. Cross-References

- Use `@see` to link to related classes, methods, or external resources.
- Use `{@link}` inline within descriptions to reference other APIs.

## 6. Enforcement

- **Pre-commit Hook:** A Git pre-commit hook automatically checks for Javadoc errors and potential missing documentation on modified files.
- **CI/CD Pipeline:** Javadoc generation is executed in the pipeline; errors will fail the build.
