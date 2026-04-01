# 🎲 Splendor Documentation System

This documentation system ensures that Javadoc and UML diagrams are automatically updated when developers make changes to the codebase. It provides tools for AI agents, IDEs, and human developers to maintain comprehensive, up-to-date documentation.

## 🚀 Quick Start

### For Human Developers

1. **Generate Documentation:**
   ```bash
   # Linux/Mac
   ./generate_docs_enhanced.sh
   
   # Windows
   generate_docs_enhanced.bat
   ```

2. **View Documentation:**
   - Open `docs/index.html` in your browser
   - Navigate through Javadoc and UML diagrams
   - Check for any documentation issues

3. **After Making Code Changes:**
   ```bash
   # Rebuild your code
   ./compile.sh && ./run_tests.sh
   
   # Update documentation
   ./generate_docs_enhanced.sh
   ```

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
│   ├── splendor-class-light.puml
│   ├── splendor-class-light.png
│   ├── splendor-dependency.puml
│   ├── splendor-dependency.png
│   ├── splendor-functional.puml
│   ├── splendor-functional.png
│   ├── splendor-inheritance.puml
│   ├── splendor-inheritance.png
│   └── plantuml.jar             # PlantUML renderer
└── DOCUMENTATION_GUIDELINES.md   # Detailed documentation guide
```

## 🛠️ Tools and Scripts

### Enhanced Documentation Generator
- **File:** `generate_docs_enhanced.sh` / `generate_docs_enhanced.bat`
- **Purpose:** Complete documentation generation (Javadoc + UML)
- **Features:**
  - Automatic Javadoc generation with strict validation
  - UML diagram rendering (if PlantUML available)
  - Beautiful HTML documentation index
  - Cross-platform support (Windows/Linux/Mac)

### Automatic UML Generator
- **File:** `generate_auto_uml.sh`
- **Purpose:** Generate UML diagrams from Java source code
- **Features:**
  - Automatic class extraction
  - Relationship detection
  - Package-specific diagrams
  - Integration with PlantUML

### VS Code Integration
- **File:** `.vscode/settings.json`
- **Features:**
  - Documentation generation tasks
  - PlantUML integration
  - Javadoc templates and snippets
  - Automatic formatting

## 🎯 Documentation Tasks (VS Code)

Press `Ctrl+Shift+P` and run:

- **"Tasks: Run Task"** → **"Generate Documentation"**
- **"Tasks: Run Task"** → **"Generate Auto UML"**
- **"Tasks: Run Task"** → **"Validate Documentation"**

## 📋 Documentation Standards

### Javadoc Requirements

All public APIs must include:
- ✅ Class-level documentation with purpose
- ✅ Method documentation with descriptions
- ✅ Parameter documentation (`@param`)
- ✅ Return value documentation (`@return`)
- ✅ Exception documentation (`@throws`)
- ✅ Usage examples for complex methods
- ✅ Thread safety notes where applicable

### UML Diagram Standards

- ✅ Consistent color coding (interfaces, enums, classes)
- ✅ Clear relationship arrows
- ✅ Package organization
- ✅ Updated with code changes
- ✅ Both editable (.puml) and rendered (.png) versions

## 🤖 AI Agent Configuration

The system includes AI agent configuration in `.ai-documentation-config.yml`:

### Automatic Triggers
- Java file modifications
- Public API changes
- Class structure changes
- New class additions
- Method signature changes

### Required Actions
- Update Javadoc immediately
- Generate UML diagrams
- Validate documentation
- Update documentation index

### Validation Rules
- 95% Javadoc coverage minimum
- All public methods documented
- UML diagram accuracy
- Link integrity checks

## 🔄 CI/CD Integration

### GitHub Actions Workflow

The system can be integrated with GitHub Actions for automatic documentation updates:

```yaml
name: Documentation Update
on:
  push:
    branches: [ main, develop ]
    paths: [ 'src/**/*.java' ]

jobs:
  update-docs:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Generate Documentation
      run: |
        chmod +x generate_docs_enhanced.sh
        ./generate_docs_enhanced.sh
    - name: Commit Updates
      run: |
        git config --local user.email "action@github.com"
        git config --local user.name "GitHub Action"
        git add docs/
        git diff --staged --quiet || git commit -m "docs: auto-update documentation"
        git push
```

### Git Hooks

Set up pre-commit hooks to validate documentation:

```bash
#!/bin/bash
# .git/hooks/pre-commit

# Check if Java files were modified
if git diff --cached --name-only | grep -q "\.java$"; then
    echo "🔍 Checking documentation..."
    ./generate_docs_enhanced.sh
    
    if [ -n "$(git status --porcelain docs/)" ]; then
        echo "⚠️  Documentation changes detected. Please review and commit them."
        exit 1
    fi
fi
```

## 🧪 Testing Documentation

Run documentation validation tests:

```bash
# Run specific documentation tests
javac -cp lib/junit-platform-console-standalone-1.10.2.jar test/com/splendor/test/DocumentationTest.java
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path classes:test-classes --select-class com.splendor.test.DocumentationTest
```

## 🆘 Troubleshooting

### Common Issues

#### Javadoc Generation Fails
```bash
# Check Java installation
java -version
javadoc -help

# Check source files
ls -la src/com/splendor/

# Run with verbose output
javadoc -verbose -d docs/javadoc -sourcepath src -subpackages com.splendor
```

#### UML Diagrams Not Updating
```bash
# Check PlantUML installation
ls -la docs/diagrams/plantuml.jar

# Test PlantUML directly
java -jar docs/diagrams/plantuml.jar docs/diagrams/splendor.puml

# Check diagram syntax
java -jar docs/diagrams/plantuml.jar -checkonly docs/diagrams/*.puml
```

#### Documentation Links Broken
```bash
# Check file permissions
ls -la docs/

# Validate HTML
# Use browser developer tools or online validators

# Check relative paths
# Ensure all links use relative paths
```

### Getting Help

1. **Check existing documentation**: Review `DOCUMENTATION_GUIDELINES.md`
2. **Run validation tests**: Use the provided test suite
3. **Check build logs**: Look for specific error messages
4. **Verify file permissions**: Ensure scripts are executable
5. **Test incrementally**: Generate documentation for specific packages first

## 📚 Additional Resources

- [📖 Detailed Documentation Guidelines](DOCUMENTATION_GUIDELINES.md)
- [🔧 VS Code Settings](.vscode/settings.json)
- [🤖 AI Agent Configuration](.ai-documentation-config.yml)
- [🎨 PlantUML Documentation](https://plantuml.com/)
- [📖 Oracle Javadoc Guide](https://docs.oracle.com/en/java/javase/17/docs/specs/javadoc/javadoc-spec.html)

## 💡 Tips for Maintainers

1. **Keep Documentation Updated**: Always run documentation generation after code changes
2. **Review Generated Docs**: Check that generated documentation is accurate and complete
3. **Maintain Standards**: Follow Javadoc and UML diagram standards
4. **Test Documentation**: Run validation tests regularly
5. **Update Guidelines**: Keep documentation guidelines current with best practices

---

**Remember**: Good documentation is as important as good code. This system ensures your documentation stays synchronized with your codebase automatically! 🚀