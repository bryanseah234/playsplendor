# Splendor Game — Documentation Guide

## Overview

This directory contains all generated and authored documentation for the Splendor board game Java implementation.

## Structure

```
docs/
├── javadoc/                        # Generated Javadoc API documentation
│   └── index.html                  # Entry point
├── diagrams/
│   ├── plantuml.jar                # PlantUML renderer
│   ├── splendor.puml               # Full architecture diagram source
│   ├── splendor-class-light.puml   # Lightweight class overview source
│   ├── splendor-dependency.puml    # Package dependency diagram source
│   ├── splendor-functional.puml    # Functional flow / sequence diagram source
│   ├── splendor-inheritance.puml   # Inheritance & interface hierarchy source
│   ├── auto_generated/             # PNG outputs from the above .puml files
│   ├── plantuml/
│   │   ├── src/                    # Detailed per-package PlantUML sources
│   │   └── png/                    # Detailed per-package PNG outputs
│   └── mermaid/
│       ├── src/                    # Mermaid diagram sources (.mmd)
│       └── png/                    # Mermaid PNG outputs
├── index.html                      # Documentation landing page
└── DOCUMENTATION.md                # This file
```

## Generating Documentation

### Javadoc

```bash
javadoc -d docs/javadoc -sourcepath src -subpackages com.splendor \
  -encoding UTF-8 -charset UTF-8 -version -author -use -splitindex \
  -windowtitle "Splendor Game API Documentation" \
  -Xdoclint:all -Xdoclint:-missing -quiet
```

### UML Diagrams (PlantUML)

```bash
# Generate all diagrams to auto_generated/
java -jar docs/diagrams/plantuml.jar -SbackgroundColor=#FFFFFF -tpng \
  docs/diagrams/splendor.puml -o auto_generated

java -jar docs/diagrams/plantuml.jar -SbackgroundColor=#FFFFFF -tpng \
  docs/diagrams/splendor-class-light.puml -o auto_generated

java -jar docs/diagrams/plantuml.jar -SbackgroundColor=#FFFFFF -tpng \
  docs/diagrams/splendor-dependency.puml -o auto_generated

java -jar docs/diagrams/plantuml.jar -SbackgroundColor=#FFFFFF -tpng \
  docs/diagrams/splendor-functional.puml -o auto_generated

java -jar docs/diagrams/plantuml.jar -SbackgroundColor=#FFFFFF -tpng \
  docs/diagrams/splendor-inheritance.puml -o auto_generated
```

Or use the generation script:

```bash
# Unix/Mac
./generate_docs_enhanced.sh

# Windows
generate_docs_enhanced.bat
```

### Mermaid Diagrams

```bash
mmdc -i docs/diagrams/mermaid/src/splendor_architecture.mmd \
     -o docs/diagrams/mermaid/png/splendor_architecture.png \
     -b white --width 3200
```

## Packages

| Package | Description |
|---------|-------------|
| `com.splendor` | Application entry point (`Main`) |
| `com.splendor.config` | Configuration loading and validation |
| `com.splendor.exception` | Custom exception hierarchy |
| `com.splendor.model` | Game domain model (Board, Game, Player, Card, Noble, etc.) |
| `com.splendor.model.validator` | Move and game-rule validation |
| `com.splendor.controller` | Game flow orchestration and AI strategy |
| `com.splendor.view` | Console and network view implementations |
| `com.splendor.network` | TCP server and client handler for multiplayer |
| `com.splendor.data` | CSV card/noble data loading |
| `com.splendor.util` | ANSI utilities, logging, input parsing, move formatting |

## Diagram Descriptions

| Diagram | Description |
|---------|-------------|
| `splendor.puml` | Full architecture — all packages, classes, and cross-layer arrows |
| `splendor-class-light.puml` | Lightweight overview — class names and key relationships only |
| `splendor-dependency.puml` | Package-level dependency arrows |
| `splendor-functional.puml` | Sequence diagram of a single game turn |
| `splendor-inheritance.puml` | Inheritance tree and interface implementations |
