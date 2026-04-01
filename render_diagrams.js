const fs = require('fs');
const path = require('path');
const { execSync, spawnSync } = require('child_process');

const srcDir = path.join(__dirname, 'diagrams/src');
const pngDir = path.join(__dirname, 'diagrams/png');

function resolveMmdcCommand() {
    const direct = spawnSync('mmdc', ['--version'], { stdio: 'ignore', shell: true });
    if (direct.status === 0) {
        return 'mmdc';
    }
    return null;
}

const mmdcCmd = resolveMmdcCommand();
if (!mmdcCmd) {
    console.warn('[render_diagrams] Mermaid CLI not found. Install it with: npm i -g @mermaid-js/mermaid-cli');
    console.warn('[render_diagrams] Skipping diagram rendering and keeping existing PNG outputs.');
    process.exit(0);
}

const files = fs.readdirSync(srcDir).filter(f => f.endsWith('.mmd'));

let failed = 0;
for (const file of files) {
    const srcFile = path.join(srcDir, file);
    const pngFile = path.join(pngDir, file.replace('.mmd', '.png'));
    
    try {
        console.log(`Rendering ${srcFile}...`);
        execSync(`${mmdcCmd} -i "${srcFile}" -o "${pngFile}" -t default -b white -s 3`, { stdio: 'inherit' });
        console.log(`Rendered ${pngFile}`);
    } catch (e) {
        failed += 1;
        console.error(`Failed to render ${srcFile}`);
        console.error(e.message);
    }
}
if (failed > 0) {
    console.error(`[render_diagrams] Completed with ${failed} failed render(s).`);
    process.exit(1);
}
console.log('Done rendering.');
