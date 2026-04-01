const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const markdownFile = path.join(__dirname, 'docs/diagrams/splendor-class-diagram.md');
const srcDir = path.join(__dirname, 'diagrams/src');
const pngDir = path.join(__dirname, 'diagrams/png');

let content = fs.readFileSync(markdownFile, 'utf8');
const mermaidRegex = /```mermaid\r?\n([\s\S]*?)```/g;

let match;
let count = 1;
let replacements = [];

while ((match = mermaidRegex.exec(content)) !== null) {
    const diagramCode = match[1];
    const srcFile = path.join(srcDir, `splendor-class-diagram_${count}.mmd`);
    const pngFile = path.join(pngDir, `splendor-class-diagram_${count}.png`);
    const pngRelPath = `../../diagrams/png/splendor-class-diagram_${count}.png`;
    
    fs.writeFileSync(srcFile, diagramCode);
    console.log(`Saved ${srcFile}`);
    
    try {
        console.log(`Rendering ${srcFile}...`);
        execSync(`mmdc -i "${srcFile}" -o "${pngFile}" -t default -b transparent`, { stdio: 'inherit' });
        console.log(`Rendered ${pngFile}`);
    } catch (e) {
        console.error(`Failed to render ${srcFile}`);
        console.error(e);
    }
    
    replacements.push({
        old: match[0],
        new: `![Diagram ${count}](${pngRelPath})`
    });
    
    count++;
}

// Replace backwards so indices don't shift
for (let i = replacements.length - 1; i >= 0; i--) {
    content = content.replace(replacements[i].old, replacements[i].new);
}

fs.writeFileSync(markdownFile, content);
console.log('Done.');
