#!/usr/bin/env bash
set -eu
if (set -o pipefail) 2>/dev/null; then
  set -o pipefail
fi

cd "$(dirname "$0")/../.."

node render_diagrams.js
bash test/ci/generate_javadoc.sh
node test/ci/verify_javadoc_index.js
bash test/ci/docs_guard.sh

echo "[docs_pipeline] OK"
