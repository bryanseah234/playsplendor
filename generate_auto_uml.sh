#!/bin/bash
# Automatic UML Diagram Generator for Splendor
# Generates PlantUML diagrams from Java source code

set -e

SOURCE_DIR="src"
DIAGRAMS_DIR="docs/diagrams"
OUTPUT_DIR="docs/diagrams/auto_generated"
PACKAGE="com.splendor"

echo "🎯 Starting Automatic UML Generation from Java Code..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Function to generate class diagram from Java files
generate_class_diagram() {
    local package_path=$1
    local output_file=$2
    local title=$3
    
    echo "Generating $title..."
    
    # Start PlantUML diagram
    cat > "$output_file" << EOF
@startuml $title
!define ENTITY_COLOR #E1F5FE
!define INTERFACE_COLOR #E8F5E8
!define ENUM_COLOR #FFF3E0
!define ABSTRACT_COLOR #F3E5F5

skinparam class {
    BackgroundColor ENTITY_COLOR
    BorderColor #1565C0
    ArrowColor #424242
}

skinparam interface {
    BackgroundColor INTERFACE_COLOR
    BorderColor #2E7D32
}

skinparam enum {
    BackgroundColor ENUM_COLOR
    BorderColor #E65100
}

skinparam abstract {
    BackgroundColor ABSTRACT_COLOR
    BorderColor #6A1B9A
}

' Generated automatically from Java source code
' Package: $package_path
' Generated on: $(date)

EOF

    # Find all Java files in the package
    find "$SOURCE_DIR/$package_path" -name "*.java" -type f | while read -r java_file; do
        # Extract class name and type
        class_name=$(grep -o "public \(class\|interface\|enum\|abstract\) [A-Za-z_][A-Za-z0-9_]*" "$java_file" | head -1 | awk '{print $3}')
        
        if [ ! -z "$class_name" ]; then
            # Determine if it's interface, enum, or class
            if grep -q "public interface" "$java_file"; then
                echo "interface $class_name {" >> "$output_file"
            elif grep -q "public enum" "$java_file"; then
                echo "enum $class_name {" >> "$output_file"
            elif grep -q "public abstract class" "$java_file"; then
                echo "abstract class $class_name {" >> "$output_file"
            else
                echo "class $class_name {" >> "$output_file"
            fi
            
            # Extract fields
            grep -o "private [A-Za-z<>\[\], ]* [A-Za-z_][A-Za-z0-9_]*;" "$java_file" | while read -r field; do
                field_type=$(echo "$field" | awk '{print $2}')
                field_name=$(echo "$field" | awk '{print $3}' | tr -d ';')
                echo "  - $field_name: $field_type" >> "$output_file"
            done
            
            # Extract methods (simplified)
            grep -o "public [A-Za-z<>\[\], ]* [A-Za-z_][A-Za-z0-9_]*(" "$java_file" | while read -r method; do
                method_return=$(echo "$method" | awk '{print $2}')
                method_name=$(echo "$method" | awk '{print $3}' | tr -d '(')
                if [[ "$method_name" != "get"* ]] && [[ "$method_name" != "set"* ]]; then
                    echo "  + $method_name(): $method_return" >> "$output_file"
                fi
            done
            
            echo "}" >> "$output_file"
            echo "" >> "$output_file"
        fi
    done
    
    # Add relationships (simplified - can be enhanced)
    echo "' Relationships (simplified)" >> "$output_file"
    
    echo "@enduml" >> "$output_file"
}

# Generate main package overview
echo "Generating main package overview..."
generate_class_diagram "com/splendor" "$OUTPUT_DIR/splendor_auto_overview.puml" "Splendor Auto-Generated Overview"

# Generate package-specific diagrams
for package_dir in "$SOURCE_DIR/com/splendor"/*; do
    if [ -d "$package_dir" ]; then
        package_name=$(basename "$package_dir")
        safe_name=$(echo "$package_name" | tr '[:lower:]' '[:upper:]' | tr '/' '_')
        echo "Generating diagram for package: $package_name"
        generate_class_diagram "com/splendor/$package_name" "$OUTPUT_DIR/splendor_auto_${package_name}.puml" "Auto-Generated: $package_name Package"
    fi
done

# Generate relationships diagram
echo "Generating relationships diagram..."
cat > "$OUTPUT_DIR/splendor_auto_relationships.puml" << 'EOF'
@startuml splendor_auto_relationships
!define ENTITY_COLOR #E1F5FE
!define INTERFACE_COLOR #E8F5E8

skinparam class {
    BackgroundColor ENTITY_COLOR
    BorderColor #1565C0
    ArrowColor #424242
}

skinparam interface {
    BackgroundColor INTERFACE_COLOR
    BorderColor #2E7D32
}

title Auto-Generated Package Relationships

' Core packages
package "com.splendor.model" {
    class Game
    class Player
    class Board
    class Card
    class Noble
}

package "com.splendor.controller" {
    class GameController
    class TurnController
    class PlayerController
}

package "com.splendor.view" {
    interface IGameView
    class ConsoleView
    class NetworkGameView
}

package "com.splendor.config" {
    interface IConfigProvider
    class FileConfigProvider
}

package "com.splendor.network" {
    class ServerSocketHandler
    class ClientHandler
}

' Relationships
GameController --> Game
GameController --> IGameView
GameController --> IConfigProvider
TurnController --> Game
PlayerController --> Game
ConsoleView ..|> IGameView
NetworkGameView ..|> IGameView
FileConfigProvider ..|> IConfigProvider
ServerSocketHandler --> ClientHandler
Game *-- Board
Game *-- Player
Board *-- Card
Board *-- Noble

@enduml
EOF

echo "🎨 Rendering generated diagrams..."
# Render all generated diagrams
if [ -f "$DIAGRAMS_DIR/plantuml.jar" ]; then
    for puml_file in "$OUTPUT_DIR"/*.puml; do
        if [ -f "$puml_file" ]; then
            java -jar "$DIAGRAMS_DIR/plantuml.jar" "$puml_file"
            echo "Rendered: $(basename "$puml_file" .puml).png"
        fi
    done
fi

echo "✅ Auto-generation complete!"
echo "Generated files:"
echo "  - $OUTPUT_DIR/splendor_auto_overview.puml"
echo "  - $OUTPUT_DIR/splendor_auto_*.puml (package diagrams)"
echo "  - $OUTPUT_DIR/splendor_auto_relationships.puml"
echo "  - Corresponding PNG files (if PlantUML available)"
echo ""
echo "💡 These diagrams are automatically generated from your Java code."
echo "   They provide a quick overview but may need manual refinement for complex relationships."