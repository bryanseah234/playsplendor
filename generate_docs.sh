#!/bin/bash
# Splendor Javadoc Generator
# Generates HTML documentation from Java source files

set -e  # Exit on any error

SOURCE_DIR="src"
OUTPUT_DIR="docs/javadoc"
PACKAGE="com.splendor"

echo "Generating Javadoc for Splendor project..."

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Run javadoc with strict settings
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

echo "Javadoc generation complete. Documentation available at: $OUTPUT_DIR/index.html"
