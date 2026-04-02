const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'src');
const javaFiles = [];

function findJavaFiles(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            findJavaFiles(fullPath);
        } else if (file.endsWith('.java')) {
            javaFiles.push(file.replace('.java', ''));
        }
    }
}
findJavaFiles(srcDir);

const pumlFile = path.join(__dirname, 'docs/diagrams/splendor-class-light.puml');
const pumlContent = fs.readFileSync(pumlFile, 'utf8');

const pumlClasses = [];
const regex = /(?:class|interface|enum)\s+(\w+)/g;
let match;
while ((match = regex.exec(pumlContent)) !== null) {
    pumlClasses.push(match[1]);
}

const missingInPuml = javaFiles.filter(c => !pumlClasses.includes(c));
const extraInPuml = pumlClasses.filter(c => !javaFiles.includes(c) && c !== 'NetworkProtocol'); // NetworkProtocol might be a conceptual class or internal class? Let's see.

console.log('Missing in PUML:', missingInPuml);
console.log('Extra in PUML:', extraInPuml);
