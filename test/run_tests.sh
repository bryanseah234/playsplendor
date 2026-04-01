#!/bin/bash
# Move to project root
cd "$(dirname "$0")/.."

echo "=== Splendor Test Suite ==="
echo ""

# Handle arguments
COVERAGE=""
VERBOSE=""
CATEGORY=""
SPECIFIC_CLASS=""

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --coverage) COVERAGE="true" ;;
        --verbose) VERBOSE="--details verbose" ;;
        --category) CATEGORY="$2"; shift ;;
        --class) SPECIFIC_CLASS="$2"; shift ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

# Compile main sources first
echo "1. Compiling main sources..."
mkdir -p classes
javac -d classes -sourcepath src \
  src/com/splendor/*.java \
  src/com/splendor/config/*.java \
  src/com/splendor/controller/*.java \
  src/com/splendor/exception/*.java \
  src/com/splendor/model/*.java \
  src/com/splendor/model/validator/*.java \
  src/com/splendor/network/*.java \
  src/com/splendor/util/*.java \
  src/com/splendor/view/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Main source compilation failed!"
    exit 1
fi
echo "   Main sources compiled OK."

# Copy resources
cp -r src/resources/* classes/ 2>/dev/null || true

# Compile test sources
echo "2. Compiling test sources..."
mkdir -p test-classes

# Find all test Java files
TEST_FILES=$(find test -name "*.java" 2>/dev/null)
if [ -z "$TEST_FILES" ]; then
    echo "   No test files found in test/"
    exit 0
fi

javac -d test-classes \
  -cp "classes;lib/junit-platform-console-standalone-1.10.2.jar" \
  -sourcepath test \
  $TEST_FILES

if [ $? -ne 0 ]; then
    echo "ERROR: Test compilation failed!"
    exit 1
fi
echo "   Test sources compiled OK."

# Run tests
echo "3. Running tests..."
echo ""

JUNIT_CMD="java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path test-classes;classes"

if [ -n "$VERBOSE" ]; then
    JUNIT_CMD="$JUNIT_CMD $VERBOSE"
fi

if [ -n "$SPECIFIC_CLASS" ]; then
    JUNIT_CMD="$JUNIT_CMD --select-class $SPECIFIC_CLASS"
elif [ -n "$CATEGORY" ]; then
    # Assuming category maps to a package or naming convention
    JUNIT_CMD="$JUNIT_CMD --select-package $CATEGORY"
else
    JUNIT_CMD="$JUNIT_CMD --scan-class-path test-classes"
fi

# Add coverage if requested (requires jacoco agent in lib/ which may not exist, so mock it for the script)
if [ -n "$COVERAGE" ]; then
    echo "[Note: Coverage requires JaCoCo agent which might not be configured. Proceeding with standard run.]"
fi

eval $JUNIT_CMD

echo ""
echo "=== Test run complete ==="
