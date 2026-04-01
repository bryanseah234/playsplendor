#!/usr/bin/env bash
set -eu
if (set -o pipefail) 2>/dev/null; then
  set -o pipefail
fi

cd "$(dirname "$0")/../.."
rm -rf docs/javadoc
mkdir -p docs/javadoc

javadoc -d docs/javadoc -sourcepath src $(find src -name "*.java")
echo "Generated Javadoc at docs/javadoc/index.html"
