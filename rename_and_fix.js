const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'diagrams/src');
const pngDir = path.join(__dirname, 'diagrams/png');
const targetSrcDir = path.join(__dirname, 'docs/diagrams/mermaid/src');
const targetPngDir = path.join(__dirname, 'docs/diagrams/mermaid/png');

if (!fs.existsSync(targetSrcDir)) fs.mkdirSync(targetSrcDir, { recursive: true });
if (!fs.existsSync(targetPngDir)) fs.mkdirSync(targetPngDir, { recursive: true });

const names = {
    1: 'system_architecture',
    2: 'model_package',
    3: 'controller_package',
    4: 'view_package',
    5: 'data_package',
    6: 'validator_package',
    7: 'config_package',
    8: 'network_package',
    9: 'utility_package',
    10: 'exception_hierarchy',
    11: 'package_dependencies'
};

for (let i = 1; i <= 11; i++) {
    const oldSrc = path.join(srcDir, `splendor-class-diagram_${i}.mmd`);
    const newSrc = path.join(targetSrcDir, `${names[i]}.mmd`);
    if (fs.existsSync(oldSrc)) {
        let content = fs.readFileSync(oldSrc, 'utf8');
        // Fix direction LR
        if (!content.includes('direction LR')) {
            content = content.replace('classDiagram', 'classDiagram\n    direction LR');
        } else {
            // Already has it, or maybe it has direction TB
            content = content.replace(/direction \w+/, 'direction LR');
        }
        // Fix generics Map<A, B> -> Map~A, B~
        content = content.replace(/<([^>]+)>/g, '~$1~');
        
        fs.writeFileSync(newSrc, content);
    }
}
