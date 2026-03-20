#!/bin/bash
echo "=== Splendor Test Suite ==="
echo ""

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
java -jar lib/junit-platform-console-standalone-1.10.2.jar \
  --class-path "test-classes;classes" \
  --scan-class-path test-classes

echo ""
echo "=== Test run complete ==="
