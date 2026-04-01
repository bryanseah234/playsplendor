const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const srcDir = path.join(__dirname, 'diagrams/src');
const pngDir = path.join(__dirname, 'diagrams/png');

const files = fs.readdirSync(srcDir).filter(f => f.endsWith('.mmd'));

for (const file of files) {
    const srcFile = path.join(srcDir, file);
    const pngFile = path.join(pngDir, file.replace('.mmd', '.png'));
    
    try {
        console.log(`Rendering ${srcFile}...`);
        execSync(`mmdc -i "${srcFile}" -o "${pngFile}" -t default -b white -s 3`, { stdio: 'inherit' });
        console.log(`Rendered ${pngFile}`);
    } catch (e) {
        console.error(`Failed to render ${srcFile}`);
        console.error(e);
    }
}
console.log('Done rendering.');
