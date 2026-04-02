#!/bin/bash
# Enhanced Splendor Documentation Generator
# Automatically generates Javadoc and UML diagrams

set -e  # Exit on any error

SOURCE_DIR="src"
OUTPUT_DIR="docs/javadoc"
DIAGRAMS_DIR="docs/diagrams"
PACKAGE="com.splendor"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting Splendor Documentation Generation...${NC}"

# Function to print colored output
print_status() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if Java is available
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH"
    exit 1
fi

# Check if javadoc is available
if ! command -v javadoc &> /dev/null; then
    print_error "javadoc is not available. Please install JDK."
    exit 1
fi

# Create output directories
print_status "Creating output directories..."
mkdir -p "$OUTPUT_DIR"
mkdir -p "$DIAGRAMS_DIR"

# Generate Javadoc
print_status "Generating Javadoc..."
javadoc \
    -d "$OUTPUT_DIR" \
    -sourcepath "$SOURCE_DIR" \
    -subpackages "$PACKAGE" \
    -encoding UTF-8 \
    -charset UTF-8 \
    -docencoding UTF-8 \
    -version \
    -author \
    -use \
    -splitindex \
    -windowtitle "Splendor Game API Documentation" \
    -doctitle "Splendor Board Game - API Documentation" \
    -header "<b>Splendor v1.0</b>" \
    -footer "Generated on $(date)" \
    -link "https://docs.oracle.com/en/java/javase/17/docs/api/" \
    -Xdoclint:all \
    -Xdoclint:-missing \
    -quiet

if [ $? -eq 0 ]; then
    print_success "Javadoc generated successfully at: $OUTPUT_DIR/index.html"
else
    print_error "Javadoc generation failed"
    exit 1
fi

# Generate UML diagrams if PlantUML is available
print_status "Checking for PlantUML..."
if [ -f "$DIAGRAMS_DIR/plantuml.jar" ]; then
    print_status "Generating UML diagrams..."
    
    # Generate all PlantUML diagrams
    for puml_file in "$DIAGRAMS_DIR"/*.puml; do
        if [ -f "$puml_file" ]; then
            filename=$(basename "$puml_file" .puml)
            print_status "Processing $filename.puml..."
            
            java -jar "$DIAGRAMS_DIR/plantuml.jar" -SbackgroundColor=#FFFFFF -o "auto_generated" "$puml_file"
            
            if [ $? -eq 0 ]; then
                print_success "Generated $filename.png in auto_generated directory"
            else
                print_error "Failed to generate $filename.png"
            fi
        fi
    done
    
    print_success "UML diagram generation complete"
else
    print_status "PlantUML not found. Skipping UML diagram generation."
    print_status "To enable UML generation, ensure plantuml.jar is in $DIAGRAMS_DIR"
fi

# Generate Mermaid diagrams
print_status "Generating Mermaid diagrams..."
if command -v node &> /dev/null; then
    node render_diagrams.js
    if [ $? -eq 0 ]; then
        print_success "Mermaid diagrams rendered successfully"
    else
        print_error "Mermaid diagram rendering failed (non-fatal)"
    fi
else
    print_status "Node.js not found. Skipping Mermaid diagram generation."
    print_status "To enable Mermaid rendering, install Node.js and run: npm install"
fi

# Generate documentation index
print_status "Creating documentation index..."
cat > "docs/index.html" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Splendor Game Documentation</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
        }
        .section {
            margin: 30px 0;
            padding: 20px;
            border-left: 4px solid #3498db;
            background-color: #f8f9fa;
        }
        .section h2 {
            color: #2c3e50;
            margin-top: 0;
        }
        .link-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .link-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            text-decoration: none;
            color: inherit;
            transition: transform 0.2s;
        }
        .link-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
        }
        .link-card h3 {
            color: #3498db;
            margin-top: 0;
        }
        .diagram-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .diagram-card {
            background: white;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            text-align: center;
        }
        .diagram-card img {
            max-width: 100%;
            height: auto;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .update-info {
            text-align: center;
            color: #7f8c8d;
            font-style: italic;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ecf0f1;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎲 Splendor Game Documentation</h1>
        
        <div class="section">
            <h2>📚 API Documentation</h2>
            <div class="link-grid">
                <a href="javadoc/index.html" class="link-card">
                    <h3>📖 Java API Documentation</h3>
                    <p>Complete Javadoc for all classes, methods, and packages in the Splendor game implementation.</p>
                </a>
                <a href="diagrams/" class="link-card">
                    <h3>🏗️ Architecture Diagrams</h3>
                    <p>UML diagrams showing class relationships, dependencies, and system architecture.</p>
                </a>
            </div>
        </div>

        <div class="section">
            <h2>🎯 UML Diagrams</h2>
            <div class="diagram-grid">
                <div class="diagram-card">
                    <h3>Class Diagram</h3>
                    <a href="diagrams/splendor.png" target="_blank">
                        <img src="diagrams/splendor.png" alt="Main Class Diagram" onerror="this.style.display='none'">
                    </a>
                    <p>Complete class structure and relationships</p>
                </div>
                <div class="diagram-card">
                    <h3>Lightweight Class Diagram</h3>
                    <a href="diagrams/splendor_class_light.png" target="_blank">
                        <img src="diagrams/splendor_class_light.png" alt="Lightweight Class Diagram" onerror="this.style.display='none'">
                    </a>
                    <p>Simplified view of main classes</p>
                </div>
                <div class="diagram-card">
                    <h3>Dependency Diagram</h3>
                    <a href="diagrams/splendor_dependency.png" target="_blank">
                        <img src="diagrams/splendor_dependency.png" alt="Dependency Diagram" onerror="this.style.display='none'">
                    </a>
                    <p>Package and class dependencies</p>
                </div>
                <div class="diagram-card">
                    <h3>Functional Flow</h3>
                    <a href="diagrams/splendor_functional.png" target="_blank">
                        <img src="diagrams/splendor_functional.png" alt="Functional Flow Diagram" onerror="this.style.display='none'">
                    </a>
                    <p>Game flow and sequence diagrams</p>
                </div>
                <div class="diagram-card">
                    <h3>Inheritance Diagram</h3>
                    <a href="diagrams/splendor_inheritance.png" target="_blank">
                        <img src="diagrams/splendor_inheritance.png" alt="Inheritance Diagram" onerror="this.style.display='none'">
                    </a>
                    <p>Class inheritance and interface hierarchy</p>
                </div>
            </div>
        </div>

        <div class="update-info">
            <p>Documentation last generated on: <strong>$(date)</strong></p>
            <p>🔄 Run <code>./generate_docs_enhanced.sh</code> to regenerate documentation</p>
        </div>
    </div>
</body>
</html>
EOF

print_success "Documentation index created at: docs/index.html"

# Summary
echo -e "\n${GREEN}🎉 Documentation generation complete!${NC}"
echo -e "📖 Javadoc: file://$(pwd)/$OUTPUT_DIR/index.html"
echo -e "🎯 Documentation Index: file://$(pwd)/docs/index.html"
echo -e "🏗️  UML Diagrams: file://$(pwd)/$DIAGRAMS_DIR/"
echo -e "\n${YELLOW}💡 Tip:${NC} Add this script to your CI/CD pipeline or git hooks for automatic updates!"
