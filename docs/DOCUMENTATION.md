# 🎲 Splendor Documentation System

This documentation system ensures that Javadoc and UML diagrams are automatically updated when developers make changes to the codebase. It provides tools for AI agents, IDEs, and human developers to maintain comprehensive, up-to-date documentation.

## 🚀 Quick Start

### For Human Developers

1. **Before making code changes:**
   ```bash
   # Generate current documentation
   ./generate_docs_enhanced.sh  # Linux/Mac
   # or
   generate_docs_enhanced.bat   # Windows
   ```

2. **After making code changes:**
   ```bash
   # Rebuild and test your changes
   ./compile.sh && test/run_tests.sh
   
   # Update documentation
   ./generate_docs_enhanced.sh
   ```

3. **View Documentation:**
   - Open `docs/index.html` in your browser
   - Navigate through Javadoc and UML diagrams
   - Check for any documentation issues

### For AI Agents/IDEs

**Always run documentation generation after:**
- ✅ Adding new classes or interfaces
- ✅ Modifying public method signatures  
- ✅ Changing class relationships
- ✅ Adding new packages
- ✅ Updating configuration classes

## 📁 Documentation Structure

```
docs/
├── index.html                    # Main documentation portal
├── javadoc/                      # Generated Javadoc HTML
│   ├── com/splendor/
│   ├── index.html               # Javadoc entry point
│   └── ...
├── diagrams/                     # UML diagrams
│   ├── splendor.puml            # Main class diagram (editable)
│   ├── splendor.png             # Main class diagram (rendered)
│   └── plantuml.jar             # PlantUML renderer
└── DOCUMENTATION.md              # This documentation guide
```

## 🛠️ Tools and Scripts

### Enhanced Documentation Generator
- **File:** `generate_docs_enhanced.sh` / `generate_docs_enhanced.bat`
- **Purpose:** Complete documentation generation (Javadoc + UML)

### Automatic UML Generator
- **File:** `generate_auto_uml.sh`
- **Purpose:** Generate UML diagrams from Java source code

### VS Code Integration
- **File:** `.vscode/settings.json`
- **Tasks:**
  - "Tasks: Run Task" → "Generate Documentation"
  - "Tasks: Run Task" → "Generate Auto UML"

## 📚 Documentation Standards

This section outlines the mandatory rules for Javadoc documentation within the Splendor Java codebase.

### 1. General Javadoc Requirements
- **100% Coverage:** All `public` and `protected` classes, interfaces, methods, and fields MUST have Javadoc comments.
- **Location:** Place Javadoc comments immediately above the declaration they describe.
- **Format:** Start with `/**` and end with `*/`. Each intermediate line must start with a `*`.

### 2. Class and Interface Documentation
- **Purpose:** Describe the architectural intent, purpose, and responsibilities of the class/interface.
- **Required Tags:** `@author`, `@version`, `@since`.

```java
/**
 * Represents a playable card in the Splendor game.
 *
 * @author Splendor Dev Team
 * @version 1.0
 * @since 1.0
 */
public class Card { ... }
```

### 3. Method Documentation
- **Description:** Clearly describe what the method does, not how it does it.
- **Tags:** Use `@param` for every parameter, `@return` for return value, and `@throws` for exceptions.

```java
/**
 * Purchases the specified card for the player.
 *
 * @param player The player attempting to purchase the card.
 * @param card The card to be purchased.
 * @return true if the purchase was successful, false otherwise.
 * @throws InsufficientFundsException if the player cannot afford the card.
 */
public boolean purchaseCard(Player player, Card card) throws InsufficientFundsException { ... }
```

### 4. Field Documentation
- Document the purpose and constraints of public or protected fields.

## 🔄 Automated Documentation Workflow

### Change Lifecycle (What happens when classes are added/edited/removed)
Run this sequence whenever Java code changes:

1. Regenerate diagrams from external sources:
   - `node render_diagrams.js`
2. Regenerate Javadoc:
   - `bash test/ci/generate_javadoc.sh`
3. Verify class index completeness/descriptions:
   - `node test/ci/verify_javadoc_index.js`
4. Run repository docs guard (links + lifecycle checks):
   - `bash test/ci/docs_guard.sh`
5. (Optional explicit diagram check):
   - `python test/ci/verify_diagram_assets.py`

If any step fails, fix sources first (JavaDoc comments, diagram source, or links), then rerun the sequence.

### Binary PR limitation (important)
If your PR tool does not support binary files (PNG/JAR), do **not** include regenerated binaries in that PR.
Instead:
- Commit text/source changes first (`.java`, `.md`, `.mmd`, scripts/workflows).
- Run diagram/Javadoc generation in CI or a follow-up artifact-producing pipeline.
- Keep diagram source in `docs/diagrams/mermaid/src/` as source of truth.

### Verification Steps
After documentation generation:
1. **Check Javadoc Quality:** No broken links, all public methods/parameters documented.
2. **Check UML Diagrams:** All classes visible, relationships accurate.
3. **Check Documentation Index:** Links work correctly.

## 🆘 Troubleshooting

### Javadoc Generation Fails
```bash
# Check Java installation
java -version
javadoc -help
# Run with verbose output
javadoc -verbose -d docs/javadoc -sourcepath src -subpackages com.splendor
```

### UML Diagrams Not Updating
```bash
# Test PlantUML directly
java -jar docs/diagrams/plantuml.jar docs/diagrams/splendor.puml
```

## 📚 Additional Resources
- [Oracle Javadoc Guide](https://docs.oracle.com/en/java/javase/17/docs/specs/javadoc/javadoc-spec.html)
- [PlantUML Documentation](https://plantuml.com/)

---
**Remember**: Good documentation is as important as good code. Keep it updated, accurate, and accessible! 🚀
