#!/usr/bin/env bash

set -euo pipefail

echo "🔧 Splendor environment requirements check (Unix)"
echo

check_cmd() {
  local cmd="$1"
  local label="$2"
  if command -v "$cmd" >/dev/null 2>&1; then
    echo "✅ $label: $(command -v "$cmd")"
  else
    echo "❌ $label: missing"
    MISSING=1
  fi
}

MISSING=0

check_cmd java "Java runtime"
check_cmd javac "Java compiler"
check_cmd javadoc "Javadoc tool"
check_cmd node "Node.js"
check_cmd npm "npm"
check_cmd python3 "Python 3"
check_cmd pip3 "pip3"
check_cmd dot "Graphviz dot (for PlantUML PNG rendering)"

if [ -f "docs/diagrams/plantuml.jar" ]; then
  echo "✅ PlantUML jar: docs/diagrams/plantuml.jar"
else
  echo "❌ PlantUML jar: missing at docs/diagrams/plantuml.jar"
  MISSING=1
fi

echo
if [ "$MISSING" -eq 0 ]; then
  echo "🎉 All required tooling is present."
  exit 0
fi

echo "Some dependencies are missing."
echo "Install guidance:"
echo "  - Ubuntu/Debian: sudo apt-get install openjdk-17-jdk nodejs npm python3 python3-pip graphviz"
echo "  - macOS (Homebrew): brew install openjdk@17 node python graphviz"
echo "  - PlantUML jar: place plantuml.jar under docs/diagrams/"
exit 1
