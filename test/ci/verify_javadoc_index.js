const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..', '..');
const srcDir = path.join(root, 'src');
const allClassesPath = path.join(root, 'docs', 'javadoc', 'allclasses-index.html');

if (!fs.existsSync(allClassesPath)) {
  console.error('Missing docs/javadoc/allclasses-index.html. Run test/ci/generate_javadoc.sh first.');
  process.exit(1);
}

function walk(dir, out = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, out);
    else if (entry.isFile() && entry.name.endsWith('.java')) out.push(full);
  }
  return out;
}

function parseTopLevelType(javaSource) {
  const sanitized = javaSource
    .replace(/\/\*[\s\S]*?\*\//g, ' ')
    .replace(/\/\/.*$/gm, ' ');
  const match = sanitized.match(/\b(public\s+)?(class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)\b/);
  return match ? { kind: match[2], name: match[3] } : null;
}

const typeNames = [];
for (const file of walk(srcDir)) {
  const src = fs.readFileSync(file, 'utf8');
  const t = parseTopLevelType(src);
  if (t) typeNames.push(t.name);
}

const indexHtml = fs.readFileSync(allClassesPath, 'utf8');
const missing = [];
const noDescription = [];

for (const name of typeNames) {
  const rowRegex = new RegExp(`<a\\s+href="[^"]+"[^>]*>${name}</a>[\\s\\S]*?<div class="col-last[^"]*">([\\s\\S]*?)<\\/div>`, 'i');
  const row = indexHtml.match(rowRegex);
  if (!row) {
    missing.push(name);
    continue;
  }

  const desc = row[1]
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  if (!desc) {
    noDescription.push(name);
  }
}

const result = {
  totalTypes: typeNames.length,
  missingCount: missing.length,
  missing,
  missingDescriptionCount: noDescription.length,
  missingDescription: noDescription,
};

console.log(JSON.stringify(result, null, 2));
if (missing.length || noDescription.length) process.exit(2);
