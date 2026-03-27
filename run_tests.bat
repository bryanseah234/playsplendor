@echo off
echo === Splendor Test Suite ===
echo.

REM Compile main sources first
echo 1. Compiling main sources...
if not exist classes mkdir classes
javac -d classes -sourcepath src ^
  src/com/splendor/*.java ^
  src/com/splendor/config/*.java ^
  src/com/splendor/controller/*.java ^
  src/com/splendor/exception/*.java ^
  src/com/splendor/model/*.java ^
  src/com/splendor/model/validator/*.java ^
  src/com/splendor/network/*.java ^
  src/com/splendor/util/*.java ^
  src/com/splendor/view/*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Main source compilation failed!
    exit /b 1
)
echo    Main sources compiled OK.

REM Copy resources
if exist src\resources (
    xcopy /s /y /q src\resources\* classes\ >nul 2>&1
)

REM Compile test sources
echo 2. Compiling test sources...
if not exist test-classes mkdir test-classes

REM Find all test Java files
setlocal enabledelayedexpansion
set "TEST_FILES="
for /r test %%f in (*.java) do (
    set "TEST_FILES=!TEST_FILES! %%f"
)

if "!TEST_FILES!"=="" (
    echo    No test files found in test/
    exit /b 0
)

javac -d test-classes ^
  -cp "classes;lib/junit-platform-console-standalone-1.10.2.jar" ^
  -sourcepath test ^
  !TEST_FILES!

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Test compilation failed!
    exit /b 1
)
echo    Test sources compiled OK.

REM Run tests
echo 3. Running tests...
echo.
java -jar lib/junit-platform-console-standalone-1.10.2.jar ^
  --class-path "test-classes;classes" ^
  --scan-class-path test-classes

echo.
echo === Test run complete ===
