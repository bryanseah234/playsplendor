#!/usr/bin/env bash
set -eu
if (set -o pipefail) 2>/dev/null; then
  set -o pipefail
fi

cd "$(dirname "$0")/../.."

echo "[docs_guard] Verifying Javadoc class index"
node test/ci/verify_javadoc_index.js

echo "[docs_guard] Scanning markdown for dead internal links"
python - <<'PY'
import re, pathlib, sys
root = pathlib.Path('.').resolve()
missing = []
for md in root.rglob('*.md'):
    text = md.read_text(errors='ignore')
    for m in re.finditer(r'\[[^\]]+\]\(([^)]+)\)', text):
        target = m.group(1).split('#')[0].strip()
        if not target or '://' in target or target.startswith('mailto:'):
            continue
        p = (md.parent / target).resolve()
        if not p.exists():
            missing.append((str(md.relative_to(root)), target))

if missing:
    print("Dead internal links found:")
    for src, target in missing:
        print(f" - {src} -> {target}")
    sys.exit(2)

print("No dead internal links found.")
PY

echo "[docs_guard] Checking for inline mermaid blocks (should be externalized)"
if command -v rg >/dev/null 2>&1; then
  if rg -n '```mermaid' --glob '*.md' . >/dev/null; then
    echo "Inline mermaid blocks detected. Move source to docs/diagrams/mermaid/src and reference PNG paths."
    rg -n '```mermaid' --glob '*.md' .
    exit 3
  fi
else
  if find . -name '*.md' -type f -print0 | xargs -0 grep -n '```mermaid' >/dev/null 2>&1; then
    echo "Inline mermaid blocks detected. Move source to docs/diagrams/mermaid/src and reference PNG paths."
    find . -name '*.md' -type f -print0 | xargs -0 grep -n '```mermaid'
    exit 3
  fi
fi

echo "[docs_guard] Verifying diagram source/output pairs"
python test/ci/verify_diagram_assets.py

echo "[docs_guard] OK"
