#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."
rm -rf docs/javadoc
mkdir -p docs/javadoc

javadoc -d docs/javadoc -sourcepath src $(find src -name "*.java")
echo "Generated Javadoc at docs/javadoc/index.html"
