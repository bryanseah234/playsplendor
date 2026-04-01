const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'src/com/splendor');
const javaFiles = [];

function walkDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            walkDir(fullPath);
        } else if (file.endsWith('.java')) {
            javaFiles.push(file.replace('.java', ''));
        }
    }
}
walkDir(srcDir);

const javadocFile = path.join(__dirname, 'docs/javadoc/allclasses-index.html');
const content = fs.readFileSync(javadocFile, 'utf8');

const missingClasses = [];
const missingDescriptions = [];

for (const className of javaFiles) {
    // skip package-info if it exists
    if (className === 'package-info') continue;
    
    if (!content.includes('>' + className + '<')) {
        missingClasses.push(className);
    } else {
        // Find the description div next to it
        // Format: <div class="col-first..."><a href="...">ClassName</a></div>\n<div class="col-last...">\n<div class="block">Description text</div>\n</div>
        const rowRegex = new RegExp('<div class="col-first [^"]+"><a href="[^"]+" title="[^"]+">' + className + '</a></div>\\s*<div class="col-last [^"]+">(?:\\s*<div class="block">)?([^<]*)(?:</div>)?\\s*</div>', 'i');
        const match = content.match(rowRegex);
        if (!match || !match[1].trim() || match[1].trim() === '&nbsp;') {
            missingDescriptions.push(className);
        }
    }
}

console.log('Missing Classes:', missingClasses);
console.log('Missing Descriptions:', missingDescriptions);
