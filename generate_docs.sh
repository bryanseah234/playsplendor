#!/bin/bash
# ==============================================================================
# Splendor Javadoc Generator (Unix/macOS)
# ==============================================================================
# This script generates comprehensive HTML API documentation from Java source
# files using the native javadoc tool. It enforces strict documentation standards
# and produces cross-referenced, searchable documentation.
#
# Usage:
#   ./generate_docs.sh [--watch] [--clean]
#
# Options:
#   --watch    Continuously monitor for changes and regenerate docs
#   --clean    Remove existing documentation before generating
#
# Requirements:
#   - Java Development Kit (JDK) 11 or higher
#   - Bash shell
#   - Standard Unix utilities (mkdir, rm, date)
#
# Exit Codes:
#   0 - Success
#   1 - Javadoc generation failed
#   2 - Invalid arguments
# ==============================================================================

set -euo pipefail

# Configuration
readonly SOURCE_DIR="src"
readonly OUTPUT_DIR="docs/javadoc"
readonly PACKAGE="com.splendor"
readonly SCRIPT_NAME="$(basename "$0")"

# Colors for output (ANSI)
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Flags
WATCH_MODE=false
CLEAN_BUILD=false

# ------------------------------------------------------------------------------
# Utility Functions
# ------------------------------------------------------------------------------

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

usage() {
    cat << EOF
Usage: $SCRIPT_NAME [OPTIONS]

Generate Javadoc documentation for the Splendor project.

Options:
    --watch     Continuously monitor for changes and regenerate documentation
    --clean     Remove existing documentation before generating
    --help      Display this help message

Examples:
    $SCRIPT_NAME                  # Generate documentation
    $SCRIPT_NAME --clean          # Clean build
    $SCRIPT_NAME --watch          # Watch mode for development

EOF
    exit 0
}

check_requirements() {
    if ! command -v javadoc &> /dev/null; then
        print_error "javadoc command not found. Please install JDK 11 or higher."
        exit 1
    fi

    if ! command -v java &> /dev/null; then
        print_error "java command not found. Please install JDK 11 or higher."
        exit 1
    fi

    if [[ ! -d "$SOURCE_DIR" ]]; then
        print_error "Source directory '$SOURCE_DIR' not found. Run this script from project root."
        exit 1
    fi
}

# ------------------------------------------------------------------------------
# Core Functions
# ------------------------------------------------------------------------------

generate_javadoc() {
    print_info "Generating Javadoc for Splendor project..."
    print_info "Source: $SOURCE_DIR"
    print_info "Output: $OUTPUT_DIR"

    # Create output directory
    mkdir -p "$OUTPUT_DIR"

    # Build timestamp for footer
    local timestamp
    timestamp=$(date "+%Y-%m-%d %H:%M:%S %Z")

    # Run javadoc with comprehensive settings
    # Note: Using -Xdoclint:-missing to allow gradual documentation improvement
    # In production, you might want to use -Xdoclint:all to enforce strict compliance
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
        -footer "Generated on $timestamp" \
        -link https://docs.oracle.com/en/java/javase/17/docs/api/ \
        -Xdoclint:all \
        -Xdoclint:-missing \
        -quiet

    local exit_code=$?

    if [[ $exit_code -ne 0 ]]; then
        print_error "Javadoc generation failed with exit code $exit_code"
        return 1
    fi

    print_success "Javadoc generation complete!"
    print_info "Documentation available at: $OUTPUT_DIR/index.html"

    return 0
}

clean_docs() {
    if [[ -d "$OUTPUT_DIR" ]]; then
        print_info "Removing existing documentation..."
        rm -rf "$OUTPUT_DIR"
        print_success "Clean complete."
    fi
}

watch_mode() {
    print_info "Starting watch mode... (Press Ctrl+C to stop)"
    print_info "Monitoring $SOURCE_DIR for changes..."

    local last_run=0

    while true; do
        # Check for changes in Java files
        local current_hash
        current_hash=$(find "$SOURCE_DIR" -name "*.java" -type f -exec md5sum {} \; 2>/dev/null | md5sum)

        if [[ "$current_hash" != "$last_run" ]]; then
            print_info "Changes detected, regenerating documentation..."
            if generate_javadoc; then
                last_run="$current_hash"
            fi
        fi

        sleep 2
    done
}

# ------------------------------------------------------------------------------
# Main Execution
# ------------------------------------------------------------------------------

main() {
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --watch)
                WATCH_MODE=true
                shift
                ;;
            --clean)
                CLEAN_BUILD=true
                shift
                ;;
            --help|-h)
                usage
                ;;
            *)
                print_error "Unknown option: $1"
                usage
                ;;
        esac
    done

    # Check requirements
    check_requirements

    # Clean if requested
    if [[ "$CLEAN_BUILD" == true ]]; then
        clean_docs
    fi

    # Generate documentation
    if [[ "$WATCH_MODE" == true ]]; then
        watch_mode
    else
        generate_javadoc
    fi
}

# Run main function with all arguments
main "$@"
