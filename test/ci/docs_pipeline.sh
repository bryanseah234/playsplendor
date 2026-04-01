#!/usr/bin/env bash
set -eu
if (set -o pipefail) 2>/dev/null; then
  set -o pipefail
fi

cd "$(dirname "$0")/../.."

if command -v mmdc >/dev/null 2>&1; then
  node render_diagrams.js
else
  echo "[docs_pipeline] mmdc not found; skipping diagram render and continuing with existing PNGs"
fi
bash test/ci/generate_javadoc.sh
node test/ci/verify_javadoc_index.js
bash test/ci/docs_guard.sh

echo "[docs_pipeline] OK"
